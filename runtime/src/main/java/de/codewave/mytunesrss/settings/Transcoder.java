package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.TranscoderConfig;
import de.codewave.utils.swing.*;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * de.codewave.mytunesrss.settings.Transcoder
 */
public class Transcoder {
    private JTextField myNameInput;
    private JTextField myPatternInput;
    private JTextField myMp4CodecsInput;
    private JTextField myOptionsInput;
    private JTextField myBinaryInput;
    private JButton myBinarySelectButton;
    private JPanel myRootPanel;
    private JButton myDeleteButton;
    private JTextField myTargetSuffixInput;
    private JTextField myTargetContentTypeInput;

    public Transcoder() {
        myBinarySelectButton.addActionListener(new SelectBinaryActionListener());
        JTextFieldValidation.setValidation(new FileExistsTextFieldValidation(myBinaryInput, false, false, MyTunesRssUtils.getBundleString(
                "error.transcoderBinaryFileMissing")));
        JTextFieldValidation.setValidation(new JTextFieldValidation(myNameInput, MyTunesRssUtils.getBundleString("error.transcoderNameInvalid")) {
            @Override
            protected boolean isValid(String text) {
                return StringUtils.isNotBlank(text) && StringUtils.isAlphanumericSpace(text) && text.length() <= 40;
            }
        });
        JTextFieldValidation.setValidation(new CompositeTextFieldValidation(myPatternInput, new NotEmptyTextFieldValidation(myPatternInput, MyTunesRssUtils.getBundleString("error.transcoderPatternBlank")), new ValidRegExpTextFieldValidation(myPatternInput, MyTunesRssUtils.getBundleString("error.transcoderPatternInvalid"))));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myTargetSuffixInput, MyTunesRssUtils.getBundleString("error.transcoderTargetSuffixBlank")));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myTargetContentTypeInput, MyTunesRssUtils.getBundleString("error.transcoderTargetContentTypeBlank")));
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
        myPatternInput.setText(tc.getPattern());
        myMp4CodecsInput.setText(tc.getMp4Codecs());
        myOptionsInput.setText(tc.getOptions());
        myBinaryInput.setText(tc.getBinary());
        myTargetSuffixInput.setText(tc.getTargetSuffix());
        myTargetContentTypeInput.setText(tc.getTargetContentType());
    }

    public String updateTranscoderConfig(TranscoderConfig tc) {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages == null) {
            tc.setName(myNameInput.getText());
            tc.setPattern(myPatternInput.getText());
            tc.setMp4Codecs(myMp4CodecsInput.getText());
            tc.setOptions(myOptionsInput.getText());
            tc.setBinary(myBinaryInput.getText());
            tc.setTargetSuffix(myTargetSuffixInput.getText());
            tc.setTargetContentType(myTargetContentTypeInput.getText());
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