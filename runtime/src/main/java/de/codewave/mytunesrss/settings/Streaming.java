package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.settings.Streaming
 */
public class Streaming {
    private JPanel myRootPanel;
    private JTextField myLameBinaryInput;
    private JButton myLameBinaryLookupButton;
    private JTextField myCacheTimeout;
    private JTextField myCacheLimit;
    private JTextField myFaad2BinaryInput;
    private JButton myFaad2BinaryLookupButton;

    public void init() {
        myLameBinaryInput.setText(MyTunesRss.CONFIG.getLameBinary());
        myFaad2BinaryInput.setText(MyTunesRss.CONFIG.getFaad2Binary());
        myLameBinaryLookupButton.addActionListener(new SelectBinaryActionListener(myLameBinaryInput, MyTunesRssUtils.getBundleString(
                "dialog.lookupLameBinary")));
        myFaad2BinaryLookupButton.addActionListener(new SelectBinaryActionListener(myFaad2BinaryInput, MyTunesRssUtils.getBundleString(
                "dialog.lookupFaad2Binary")));
        myCacheTimeout.setText(Integer.toString(MyTunesRss.CONFIG.getStreamingCacheTimeout()));
        myCacheLimit.setText(Integer.toString(MyTunesRss.CONFIG.getStreamingCacheMaxFiles()));
        JTextFieldValidation.setValidation(new FileExistsTextFieldValidation(myLameBinaryInput, true, false, MyTunesRssUtils.getBundleString("error.lameBinaryFileMissing")));
        JTextFieldValidation.setValidation(new FileExistsTextFieldValidation(myFaad2BinaryInput, true, false, MyTunesRssUtils.getBundleString("error.faad2BinaryFileMissing")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myCacheTimeout, 0, 1440, true, MyTunesRssUtils.getBundleString("error.illegalCacheTimeout")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myCacheLimit, 0, 10000, true, MyTunesRssUtils.getBundleString("error.illegalCacheLimit")));
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            MyTunesRss.CONFIG.setLameBinary(myLameBinaryInput.getText());
            MyTunesRss.CONFIG.setFaad2Binary(myFaad2BinaryInput.getText());
            if (StringUtils.isNotEmpty(myCacheTimeout.getText())) {
                MyTunesRss.CONFIG.setStreamingCacheTimeout(Integer.parseInt(myCacheTimeout.getText()));
            } else {
                MyTunesRss.CONFIG.setStreamingCacheTimeout(0);
            }
            if (StringUtils.isNotEmpty(myCacheLimit.getText())) {
                MyTunesRss.CONFIG.setStreamingCacheMaxFiles(Integer.parseInt(myCacheLimit.getText()));
            } else {
                MyTunesRss.CONFIG.setStreamingCacheMaxFiles(0);
            }
        }
        return null;
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myLameBinaryInput, false);
                SwingUtils.enableElementAndLabel(myFaad2BinaryInput, false);
                SwingUtils.enableElementAndLabel(myCacheTimeout, false);
                SwingUtils.enableElementAndLabel(myCacheLimit, false);
                myLameBinaryLookupButton.setEnabled(false);
                myFaad2BinaryLookupButton.setEnabled(false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myLameBinaryInput, true);
                SwingUtils.enableElementAndLabel(myFaad2BinaryInput, true);
                SwingUtils.enableElementAndLabel(myCacheTimeout, true);
                SwingUtils.enableElementAndLabel(myCacheLimit, true);
                myLameBinaryLookupButton.setEnabled(true);
                myFaad2BinaryLookupButton.setEnabled(true);
                break;
        }
    }

    public class SelectBinaryActionListener implements ActionListener {
        private JTextField myInput;
        private String myDialogTitle;

        public SelectBinaryActionListener(JTextField input, String dialogTitle) {
            myInput = input;
            myDialogTitle = dialogTitle;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(myDialogTitle);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File file) {
                    return file.isFile();
                }

                public String getDescription() {
                    return null;
                }
            });
            int result = fileChooser.showOpenDialog(MyTunesRss.ROOT_FRAME);
            if (result == JFileChooser.APPROVE_OPTION) {
                myInput.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
}