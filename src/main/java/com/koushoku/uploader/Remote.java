package com.koushoku.uploader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Remote {
    private static final Logger logger = Logger.getLogger(Remote.class.getName());
    private Session session = null;
    private String utilCommand = App.getConfig().get("directory").getAsString() + "/util";
    private String username = App.getConfig().get("username").getAsString();
    private static String privateKey = Remote.class.getResource("/id_rsa").getPath();
    private String host = App.getConfig().get("host").getAsString();
    private int port = App.getConfig().get("port").getAsInt();

    public Remote() throws JSchException {
        connect();
    }

    private void connect() {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey);
            session = jsch.getSession(username, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
        } catch (JSchException e) {
            logger.log(Level.SEVERE, "", e);
        }
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public void checkConnection() {
        if (session == null) {
            connect();
        }
        if (!session.isConnected())
            try {
                session.connect();
            } catch (JSchException e) {
                logger.log(Level.SEVERE, "", e);
            }
    }

    public void publish() throws JSchException, IOException, InterruptedException {
        checkConnection();
        ChannelExec channel = null;
        try {

            channel = (ChannelExec) session.openChannel("exec");
            String command = utilCommand + " --index --publish-all --generate-thumbnails && systemctl restart koushoku";
            channel.setCommand(command);
            channel.connect();
            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream()) {
                channel.setOutputStream(responseStream);
                channel.connect();

                while (channel.isConnected()) {
                    Thread.sleep(100);
                }

                String responseString = new String(responseStream.toByteArray());
                logger.log(Level.INFO, responseString);
            }
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
}
