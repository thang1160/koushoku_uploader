package com.koushoku.uploader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.commons.io.IOUtils;

public class Remote {
    private static final Logger logger = Logger.getLogger(Remote.class.getName());
    private Session session = null;
    private String directory = App.getConfig().get("directory").getAsString();
    private String username = App.getConfig().get("username").getAsString();
    private String host = App.getConfig().get("host").getAsString();
    private int port = App.getConfig().get("port").getAsInt();
    JSch jsch = new JSch();

    public Remote() throws JSchException, IOException {
        InputStream privateKey = App.class.getClassLoader().getResourceAsStream("id_rsa");
        InputStream publicKey = App.class.getClassLoader().getResourceAsStream("id_rsa.pub");
        jsch.addIdentity("id_rsa", IOUtils.toByteArray(privateKey), IOUtils.toByteArray(publicKey), null);
        connect();
    }

    private void connect() throws JSchException {
        session = jsch.getSession(username, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public void checkConnection() throws JSchException {
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

    public int upload(File[] files) throws JSchException {
        checkConnection();
        ChannelSftp channelSftp = null;
        int i = 0;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            for (File file : files) {
                String remoteDir = directory + "data/" + file.getName();
                channelSftp.put(file.getAbsolutePath(), remoteDir);
                i++;
            }
        } catch (SftpException e) {
            logger.log(Level.SEVERE, "", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }
        return i;
    }
}
