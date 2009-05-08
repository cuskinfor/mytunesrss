package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.TranscoderConfig;
import de.codewave.utils.swing.FileExistsTextFieldValidation;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.NotEmptyTextFieldValidation;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * de.codewave.mytunesrss.settings.Transcoder
 */
public class Transcoder {
    private JTextField myNameInput;
    private JTextField mySuffixesInput;
    private JTextField myMp4CodecsInput;
    private JTextField myOptionsInput;
    private JTextField myBinaryInput;
    private JButton myBinarySelectButton;
    private JPanel myRootPanel;
    private JButton myDeleteButton;

    public Transcoder() {
        myBinarySelectButton.addActionListener(new SelectBinaryActionListener());
        JTextFieldValidation.setValidation(new FileExistsTextFieldValidation(myBinaryInput, false, false, MyTunesRssUtils.getBundleString(
                "error.transcoderBinaryFileMissing")));
        JTextFieldValidation.setValidation(new JTextFieldValidation(myNameInput, MyTunesRssUtils.getBundleString("error.transcoderNameInvalid")) {
            @Override
            protected boolean isValid(String text) {
                return StringUtils.isNotBlank(text) && StringUtils.isAlphanumeric(text) && text.length() <= 20;
            }
        });
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(mySuffixesInput, MyTunesRssUtils.getBundleString("error.transcoderSuffixesBlank")));
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public JButton getDeleteButton() {
        return myDeleteButton;
    }

    public String getName() {
        return myNameInput.getText();
    }

    public void init(TranscoderConfig tc) {
        myNameInput.setText(tc.getName());
        mySuffixesInput.setText(tc.getSuffixes());
        myMp4CodecsInput.setText(tc.getMp4Codecs());
        myOptionsInput.setText(tc.getOptions());
        myBinaryInput.setText(tc.getBinary());
    }

    public String updateTranscoderConfig(TranscoderConfig tc) {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages == null) {
            tc.setName(myNameInput.getText());
            tc.setSuffixes(mySuffixesInput.getText());
            tc.setMp4Codecs(myMp4CodecsInput.getText());
            tc.setOptions(myOptionsInput.getText());
            tc.setBinary(myBinaryInput.getText());
        }
        return messages;
    }

    public class SelectBinaryActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(MyTunesRssUtils.getBundleString("dialog.lookupTranscoderBinary"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showDialog(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("filechooser.approve.transcoding"));
            if (result == JFileChooser.APPROVE_OPTION) {
                myBinaryInput.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
}