package com.koushoku.uploader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Remote {
    private static final Logger logger = Logger.getLogger(Remote.class.getName());
    private Session session = null;
    private String directory = App.getConfig().get("directory").getAsString();
    private String username = App.getConfig().get("username").getAsString();
    private static String privateKey = Remote.class.getResource("/id_rsa").getPath();
    private String host = App.getConfig().get("host").getAsString();
    private int port = App.getConfig().get("port").getAsInt();
    JSch jsch = new JSch();

    public Remote() throws JSchException {
        jsch.addIdentity(privateKey);
        connect();
    }

    private void connect() {
        try {
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
        if (session == null || !session.isConnected()) {
            connect();
        }
    }

    public void publish() throws JSchException, IOException, InterruptedException {
        checkConnection();
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            String command = directory
                    + "util --index --publish-all --generate-thumbnails && systemctl restart koushoku";
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

    public void upload(String path, String fileName) throws JSchException, SftpException {
        checkConnection();
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            String remoteDir = directory + "data/" + fileName;
            channelSftp.put(path, remoteDir);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }
    }
}
