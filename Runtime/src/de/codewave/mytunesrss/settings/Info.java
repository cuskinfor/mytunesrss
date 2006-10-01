/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.zip.*;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info {
    private static final Log LOG = LogFactory.getLog(Info.class);
    private JButton mySaveLogButton;
    private JPanel myRootPanel;

    public void init() {
        mySaveLogButton.addActionListener(new SaveLogButtonActionListener());
    }

    public class SaveLogButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            File targetFile = new File("MyTunesRSS-" + MyTunesRss.VERSION + "-LogFile.zip");
            FileDialog fileDialog = new FileDialog(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.saveLog"), FileDialog.SAVE);
            fileDialog.setDirectory(targetFile.getParent());
            fileDialog.setFile(targetFile.getName());
            fileDialog.setVisible(true);
            if (fileDialog.getFile() != null) {
                targetFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
                FileInputStream input = null;
                ZipOutputStream zipOutput = null;
                try {
                    input = new FileInputStream(new File("MyTunesRSS.log"));
                    zipOutput = new ZipOutputStream(new FileOutputStream(targetFile));
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
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.couldNotCreateLogArchive"));
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