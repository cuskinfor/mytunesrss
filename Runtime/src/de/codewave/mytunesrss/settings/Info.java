/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.zip.*;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info {
    private static final Log LOG = LogFactory.getLog(Info.class);
    private JPanel myRootPanel;
    private JButton mySaveLogButton;
    private Settings mySettingsForm;

    public void init(Settings settingsForm) {
        mySettingsForm = settingsForm;
        mySaveLogButton.addActionListener(new SaveLogButtonActionListener());
    }

    public class SaveLogButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(new File("MyTunesRSS-" + MyTunesRss.VERSION + "-LogFile.zip"));
            if (fileChooser.showSaveDialog(mySettingsForm.getFrame()) == JFileChooser.APPROVE_OPTION) {
                FileInputStream input = null;
                ZipOutputStream zipOutput = null;
                try {
                    input = new FileInputStream(new File("MyTunesRSS.log"));
                    zipOutput = new ZipOutputStream(new FileOutputStream(fileChooser.getSelectedFile()));
                    zipOutput.putNextEntry(new ZipEntry("MyTunesRSS.log"));
                    byte[] bytes = new byte[4096];
                    for (int bytesRead = input.read(bytes); bytesRead != -1; bytesRead = input.read(bytes)) {
                        if (bytesRead > 0) {
                            zipOutput.write(bytes, 0, bytesRead);
                        }
                    }
                    zipOutput.closeEntry();
                } catch (IOException e1) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not create zip archive with log file.", e1);
                    }
                    SwingUtils.showErrorMessage(mySettingsForm.getFrame(), "Could not create zip archive with log file. Please try again.");
                } finally {
                    if (zipOutput != null) {
                        try {
                            zipOutput.close();
                        } catch (IOException e1) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error("Could not close output file.", e1);
                            }
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e1) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error("Could not close input file.", e1);
                            }
                        }
                    }
                }
            }
        }
    }
}