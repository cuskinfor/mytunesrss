package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.GridConstraints;

/**
 * de.codewave.mytunesrss.settings.Streaming
 */
public class Streaming implements MyTunesRssEventListener, SettingsForm {
    private JPanel myRootPanel;
    private JTextField myLameBinaryInput;
    private JButton myLameBinaryLookupButton;
    private JTextField myCacheTimeout;
    private JTextField myCacheLimit;
    private JCheckBox myLimitBandwidthCheckBox;
    private JTextField myBandwidthLimitInput;
    private JLabel myBandwidthLimitLabel;
    private JTextField myLameTargetOptions;
    private JScrollPane myScrollPane;
    private JPanel myTranscodersPanel;
    private JButton myAddTranscoderButton;
    private List<Transcoder> myTranscoders = new ArrayList<Transcoder>();

    public Streaming() {
        myScrollPane.getViewport().setOpaque(false);
        myLameBinaryLookupButton.addActionListener(new SelectBinaryActionListener(myLameBinaryInput, MyTunesRssUtils.getBundleString(
                "dialog.lookupLameBinary")));
        myAddTranscoderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Transcoder transcoder = new Transcoder();
                myTranscoders.add(transcoder);
                transcoder.getDeleteButton().addActionListener(new DeleteTranscoderActionListener(transcoder));
                refreshTranscoders();
                myTranscodersPanel.scrollRectToVisible(new Rectangle(0, myTranscodersPanel.getHeight() - 2, 1, 1));
            }
        });
        JTextFieldValidation.setValidation(new FileExistsTextFieldValidation(myLameBinaryInput, true, false, MyTunesRssUtils.getBundleString(
                "error.lameBinaryFileMissing")));
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
                }
            }
        });
    }

    public void initValues() {
        myLameBinaryInput.setText(MyTunesRss.CONFIG.getLameBinary());
        myCacheTimeout.setText(Integer.toString(MyTunesRss.CONFIG.getStreamingCacheTimeout()));
        myCacheLimit.setText(Integer.toString(MyTunesRss.CONFIG.getStreamingCacheMaxFiles()));
        if (MyTunesRss.CONFIG.getBandwidthLimitFactor().compareTo(BigDecimal.ZERO) > 0) {
            myBandwidthLimitInput.setText(MyTunesRss.CONFIG.getBandwidthLimitFactor().toPlainString());
        } else {
            myBandwidthLimitInput.setText("");
        }
        myLimitBandwidthCheckBox.setSelected(MyTunesRss.CONFIG.isBandwidthLimit());
        myBandwidthLimitInput.setEnabled(myLimitBandwidthCheckBox.isSelected());
        myLameTargetOptions.setText(MyTunesRss.CONFIG.getLameTargetOptions());
        myTranscoders.clear();
        for (TranscoderConfig tc : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            Transcoder transcoder = new Transcoder();
            myTranscoders.add(transcoder);
            transcoder.getDeleteButton().addActionListener(new DeleteTranscoderActionListener(transcoder));
            transcoder.init(tc);
        }
        refreshTranscoders();
    }

    private void refreshTranscoders() {
        myTranscodersPanel.removeAll();
        int row = 0;
        myTranscodersPanel.setLayout(new GridLayoutManager(myTranscoders.size() + 1, 1));
        for (Transcoder transcoder : myTranscoders) {
            addTranscoder(transcoder, row++);
        }
        addPanelComponent(new JLabel(""), new GridConstraints(row,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                null,
                null,
                null));
        myTranscodersPanel.validate();
        myScrollPane.validate();
    }

    private void addTranscoder(Transcoder transcoder, int row) {
        GridConstraints gbcTc = new GridConstraints(row,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null);
        addPanelComponent(transcoder.getRootPanel(), gbcTc);
    }

    private void addPanelComponent(JComponent component, GridConstraints gridConstraints) {
        myTranscodersPanel.add(component, gridConstraints);
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            Set<String> transcoderNames = new HashSet<String>();
            List<TranscoderConfig> transcoderConfigs = new ArrayList<TranscoderConfig>();
            for (Transcoder tc : myTranscoders) {
                TranscoderConfig config = new TranscoderConfig();
                messages = tc.updateTranscoderConfig(config);
                if (messages != null) {
                    return messages;
                }
                transcoderNames.add(config.getName());
                transcoderConfigs.add(config);
            }
            if (transcoderNames.size() < transcoderConfigs.size()) {
                return MyTunesRssUtils.getBundleString("error.duplicateTranscoderName");
            }
            MyTunesRss.CONFIG.setTranscoderConfigs(transcoderConfigs);
            MyTunesRss.CONFIG.setLameBinary(myLameBinaryInput.getText());
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
            MyTunesRss.CONFIG.setLameTargetOptions(myLameTargetOptions.getText());
        }
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.streaming.title");
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

    public class DeleteTranscoderActionListener implements ActionListener {
        private Transcoder myTranscoder;

        public DeleteTranscoderActionListener(Transcoder transcoder) {
            myTranscoder = transcoder;
        }

        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(myRootPanel,
                    MyTunesRssUtils.getBundleString("confirmation.deleteTranscoder", myTranscoder.getName()),
                    MyTunesRssUtils.getBundleString("confirmation.titleDeleteTranscoder"),
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                myTranscoders.remove(myTranscoder);
                refreshTranscoders();
            }
        }
    }
}