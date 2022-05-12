package com.koushoku.uploader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class Desktop {
    private static final Logger logger = Logger.getLogger(Desktop.class.getName());
    private Remote r;
    private JFrame f;

    public Desktop() {
        f = new JFrame();
        try {
            r = new Remote();
        } catch (JSchException e) {
            logger.log(Level.SEVERE, "", e);
            JOptionPane.showMessageDialog(f,
                    "Can't connect to remote server.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            r.disconnect();
            System.exit(0);
            return;
        }
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                r.disconnect();
                System.exit(0);
            }
        });
        f.setTitle("Koushoku uploader");
        f.add(initPublishButton());
        f.add(initBrowseButton());
        f.setSize(400, 200);
        f.setLayout(null);
        f.setVisible(true);
    }

    public JButton initPublishButton() {
        JButton publishButton = new JButton("Publish");
        publishButton.setBounds(130, 50, 100, 40);
        publishButton.addActionListener(arg0 -> {
            try {
                r.publish();
            } catch (JSchException | IOException e) {
                logger.log(Level.SEVERE, "", e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "", e);
                Thread.currentThread().interrupt();
            }
        });
        return publishButton;
    }

    public JButton initBrowseButton() {
        JButton browseButton = new JButton("Browse");
        browseButton.setBounds(258, 26, 105, 31);
        browseButton.addActionListener(arg0 -> {
            JFileChooser filedilg = new JFileChooser();
            filedilg.showOpenDialog(filedilg);
            File file = filedilg.getSelectedFile();
            if (file == null)
                return;
            String path = file.getAbsolutePath();
            JOptionPane.showMessageDialog(f,
                    "Uploading file: " + file.getName(),
                    "Uploading",
                    JOptionPane.INFORMATION_MESSAGE);
            try {
                r.upload(path, file.getName());
            } catch (JSchException | SftpException e) {
                logger.log(Level.SEVERE, "", e);
            }
        });
        return browseButton;
    }
}
