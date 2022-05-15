package com.koushoku.uploader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import com.jcraft.jsch.JSchException;

public class Desktop {
    private static final Logger logger = Logger.getLogger(Desktop.class.getName());
    private Remote r;
    private JFrame f;

    public Desktop() {
        f = new JFrame();
        try {
            r = new Remote();
        } catch (JSchException | IOException e) {
            logger.log(Level.SEVERE, "", e);
            JOptionPane.showMessageDialog(f,
                    "Can't connect to remote server.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            if (r != null)
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
        f.add(initUploadButton());
        f.setSize(400, 200);
        f.setLayout(null);
        f.setVisible(true);
    }

    public JButton initPublishButton() {
        JButton publishButton = new JButton("Publish");
        publishButton.setBounds(250, 50, 100, 40);
        publishButton.addActionListener(arg0 -> {
            try {
                r.publish();
                JOptionPane.showMessageDialog(f,
                        "Published successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (JSchException e) {
                logger.log(Level.SEVERE, "", e);
                JOptionPane.showMessageDialog(f,
                        "Can't connect to remote server.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "", e);
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "", e);
            }
        });
        return publishButton;
    }

    public JButton initUploadButton() {
        JButton uploadButton = new JButton("Upload");
        uploadButton.setBounds(50, 50, 100, 40);
        uploadButton.addActionListener(arg0 -> {
            JFileChooser filedilg = new JFileChooser();
            Action details = filedilg.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);
            filedilg.setMultiSelectionEnabled(true);
            filedilg.showOpenDialog(f);
            File[] files = filedilg.getSelectedFiles();
            if (files.length == 0)
                return;
            try {
                int result = r.upload(files);
                if (result < files.length) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = result; i < files.length; i++) {
                        sb.append(files[i].getName() + "\n");
                    }
                    JOptionPane.showMessageDialog(f,
                            "List upload failed:\n" + sb.toString(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(f,
                            files.length + " files upload successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (JSchException e) {
                logger.log(Level.SEVERE, "", e);
                JOptionPane.showMessageDialog(f,
                        "Can't connect to remote server.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        return uploadButton;
    }
}
