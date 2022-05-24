package com.koushoku.uploader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jcraft.jsch.JSchException;
import com.koushoku.uploader.DAO.ArchiveDAO;
import com.koushoku.uploader.classes.Archive;

public class Desktop {
    private static final Logger logger = Logger.getLogger(Desktop.class.getName());
    private Remote r;
    private JFrame f;
    private JButton publishButton;
    private JComboBox<Archive> comboBox;
    private JTextField sourceTextField;

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

        comboBox = new JComboBox<>();
        comboBox.setBounds(50, 150, 350, 25);
        reloadJCombobox();
        f.add(comboBox);

        sourceTextField = new JTextField();
        sourceTextField.setBounds(50, 175, 350, 25);
        f.add(sourceTextField);

        f.add(initUpdateButton());
        f.setSize(450, 400);
        f.setLayout(null);
        f.setVisible(true);
    }

    public JButton initPublishButton() {
        publishButton = new JButton("Publish");
        publishButton.setBounds(250, 50, 150, 50);
        publishButton.addActionListener(arg0 -> {
            publishButton.setText("Publishing...");
            publishButton.setEnabled(false);
            Thread t = new Thread(() -> {
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
                publishButton.setText("Publish");
                publishButton.setEnabled(true);
                reloadJCombobox();
            });
            t.start();
        });
        return publishButton;
    }

    public JButton initUploadButton() {
        JButton uploadButton = new JButton("Upload");
        uploadButton.setBounds(50, 50, 150, 50);
        uploadButton.addActionListener(arg0 -> {
            JFileChooser filedilg = new JFileChooser();
            Action details = filedilg.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);
            filedilg.setMultiSelectionEnabled(true);
            filedilg.addChoosableFileFilter(new FileNameExtensionFilter("*.zip", "zip"));
            filedilg.setAcceptAllFileFilterUsed(false);
            filedilg.showOpenDialog(f);
            File[] files = filedilg.getSelectedFiles();
            if (files.length == 0)
                return;
            uploadButton.setText("Uploading...");
            uploadButton.setEnabled(false);
            publishButton.setEnabled(false);
            Thread t = new Thread(
                    () -> {
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
                        uploadButton.setText("Upload");
                        uploadButton.setEnabled(true);
                        publishButton.setEnabled(true);
                    });
            t.start();
        });
        return uploadButton;
    }

    public JButton initUpdateButton() {
        JButton updateButton = new JButton("Update");
        updateButton.setBounds(50, 250, 150, 50);
        updateButton.addActionListener(arg0 -> {
            Archive archive = (Archive) comboBox.getSelectedItem();
            if (archive == null) {
                JOptionPane.showMessageDialog(f,
                        "Please select an archive.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (sourceTextField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(f,
                        "Please input a source path.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            archive.setSource(sourceTextField.getText());
            ArchiveDAO.UpdateArchive(archive);
            try {
                r.restart();
            } catch (JSchException e) {
                logger.log(Level.SEVERE, "", e);
                JOptionPane.showMessageDialog(f,
                        "Can't connect to remote server.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
            JOptionPane.showMessageDialog(f,
                    "Update successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        return updateButton;
    }

    public void reloadJCombobox() {
        comboBox.removeAllItems();
        List<Archive> archives = ArchiveDAO.getArchives();
        for (Archive a : archives) {
            comboBox.addItem(a);
        }
    }
}
