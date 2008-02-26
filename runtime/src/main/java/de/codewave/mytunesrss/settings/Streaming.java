package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.swing.*;
import org.apache.commons.lang.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.math.*;

/**
 * de.codewave.mytunesrss.settings.Streaming
 */
public class Streaming implements MyTunesRssEventListener {
    private JPanel myRootPanel;
    private JTextField myLameBinaryInput;
    private JButton myLameBinaryLookupButton;
    private JTextField myCacheTimeout;
    private JTextField myCacheLimit;
    private JTextField myFaad2BinaryInput;
    private JButton myFaad2BinaryLookupButton;
    private JCheckBox myLimitBandwidthCheckBox;
    private JTextField myBandwidthLimitInput;
    private JLabel myBandwidthLimitLabel;
    private JTextField myAlacBinaryInput;
    private JButton myAlacBinaryLookupButton;

    public void init() {
        initValues();
        myLameBinaryLookupButton.addActionListener(new SelectBinaryActionListener(myLameBinaryInput, MyTunesRssUtils.getBundleString(
                "dialog.lookupLameBinary")));
        myFaad2BinaryLookupButton.addActionListener(new SelectBinaryActionListener(myFaad2BinaryInput, MyTunesRssUtils.getBundleString(
                "dialog.lookupFaad2Binary")));
        myAlacBinaryLookupButton.addActionListener(new SelectBinaryActionListener(myAlacBinaryInput, MyTunesRssUtils.getBundleString(
                "dialog.lookupAlacBinary")));
        JTextFieldValidation.setValidation(new FileExistsTextFieldValidation(myLameBinaryInput, true, false, MyTunesRssUtils.getBundleString(
                "error.lameBinaryFileMissing")));
        JTextFieldValidation.setValidation(new FileExistsTextFieldValidation(myFaad2BinaryInput, true, false, MyTunesRssUtils.getBundleString(
                "error.faad2BinaryFileMissing")));
        JTextFieldValidation.setValidation(new FileExistsTextFieldValidation(myAlacBinaryInput, true, false, MyTunesRssUtils.getBundleString(
                "error.alacBinaryFileMissing")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myCacheTimeout, 0, 1440, true, MyTunesRssUtils.getBundleString(
                "error.illegalCacheTimeout")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myCacheLimit, 0, 10000, true, MyTunesRssUtils.getBundleString(
                "error.illegalCacheLimit")));
        MinMaxValueTextFieldValidation minMaxValidation = new MinMaxValueTextFieldValidation(myBandwidthLimitInput, 0, 5, true, null);
        JTextFieldValidation.setValidation(new CheckboxCheckedTextFieldValidation(myBandwidthLimitInput,
                                                                                  myLimitBandwidthCheckBox,
                                                                                  minMaxValidation,
                                                                                  MyTunesRssUtils.getBundleString("error.illegalBandwidthLimit")));
        myLimitBandwidthCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                myBandwidthLimitInput.setEnabled(myLimitBandwidthCheckBox.isSelected());
            }
        });
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event) {
                    case CONFIGURATION_CHANGED:
                        initValues();
                        break;
                    case DATABASE_UPDATE_STATE_CHANGED:
                        setGuiMode(GuiMode.DatabaseUpdating);
                        break;
                    case DATABASE_UPDATE_FINISHED:
                    case DATABASE_UPDATE_FINISHED_NOT_RUN:
                        setGuiMode(GuiMode.DatabaseIdle);
                        break;
                    case SERVER_STARTED:
                        setGuiMode(GuiMode.ServerRunning);
                        break;
                    case SERVER_STOPPED:
                        setGuiMode(GuiMode.ServerIdle);
                        break;
                }
            }
        });
    }

    private void initValues() {
        myLameBinaryInput.setText(MyTunesRss.CONFIG.getLameBinary());
        myFaad2BinaryInput.setText(MyTunesRss.CONFIG.getFaad2Binary());
        myAlacBinaryInput.setText(MyTunesRss.CONFIG.getAlacBinary());
        myCacheTimeout.setText(Integer.toString(MyTunesRss.CONFIG.getStreamingCacheTimeout()));
        myCacheLimit.setText(Integer.toString(MyTunesRss.CONFIG.getStreamingCacheMaxFiles()));
        if (MyTunesRss.CONFIG.getBandwidthLimitFactor().compareTo(BigDecimal.ZERO) > 0) {
            myBandwidthLimitInput.setText(MyTunesRss.CONFIG.getBandwidthLimitFactor().toPlainString());
        } else {
            myBandwidthLimitInput.setText("");
        }
        myLimitBandwidthCheckBox.setSelected(MyTunesRss.CONFIG.isBandwidthLimit());
        myBandwidthLimitInput.setEnabled(myLimitBandwidthCheckBox.isSelected());
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            MyTunesRss.CONFIG.setLameBinary(myLameBinaryInput.getText());
            MyTunesRss.CONFIG.setFaad2Binary(myFaad2BinaryInput.getText());
            MyTunesRss.CONFIG.setAlacBinary(myAlacBinaryInput.getText());
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
            MyTunesRss.CONFIG.setBandwidthLimit(myLimitBandwidthCheckBox.isSelected());
            if (myLimitBandwidthCheckBox.isSelected() && StringUtils.isNotEmpty(myBandwidthLimitInput.getText())) {
                MyTunesRss.CONFIG.setBandwidthLimitFactor(new BigDecimal(myBandwidthLimitInput.getText()));
            } else {
                MyTunesRss.CONFIG.setBandwidthLimitFactor(BigDecimal.ZERO);
            }
        }
        return null;
    }

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        SwingUtils.enableElementAndLabel(myLameBinaryInput, !serverActive);
        SwingUtils.enableElementAndLabel(myFaad2BinaryInput, !serverActive);
        SwingUtils.enableElementAndLabel(myAlacBinaryInput, !serverActive);
        SwingUtils.enableElementAndLabel(myCacheTimeout, !serverActive);
        SwingUtils.enableElementAndLabel(myCacheLimit, !serverActive);
        myLameBinaryLookupButton.setEnabled(!serverActive);
        myFaad2BinaryLookupButton.setEnabled(!serverActive);
        myAlacBinaryLookupButton.setEnabled(!serverActive);
        myLimitBandwidthCheckBox.setEnabled(!serverActive);
        myBandwidthLimitInput.setEnabled(myLimitBandwidthCheckBox.isSelected() && !serverActive);
        myBandwidthLimitLabel.setEnabled(!serverActive);
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
            int result = fileChooser.showDialog(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("filechooser.approve.transcoding"));
            if (result == JFileChooser.APPROVE_OPTION) {
                myInput.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
}