/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.*;
import de.codewave.utils.network.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.zip.*;

/**
 * General settings panel
 */
public class General {
    private static final Log LOG = LogFactory.getLog(General.class);

    private static final String LIBRARY_XML_FILE_NAME = "iTunes Music Library.xml";

    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JPasswordField myPasswordInput;
    private JTextField myTunesXmlPathInput;
    private JButton myTunesXmlPathLookupButton;
    private Settings mySettingsForm;
    private JLabel myServerStatusLabel;
    private JSpinner myMaxMemInput;
    private JLabel myMaxMemLabel;
    private JButton myMaxMemSaveButton;

    public JPasswordField getPasswordInput() {
        return myPasswordInput;
    }

    public JTextField getPortInput() {
        return myPortInput;
    }

    public JTextField getTunesXmlPathInput() {
        return myTunesXmlPathInput;
    }

    public void init(Settings settingsForm) {
        mySettingsForm = settingsForm;
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        myPasswordInput.setText(MyTunesRss.CONFIG.getPassword());
        int minMemory = ProgramUtils.getMemorySwitch(MemorySwitchType.Minimum);
        int maxMemory = ProgramUtils.getMemorySwitch(MemorySwitchType.Maxmimum);
        if (maxMemory != -1) {
            SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(maxMemory, Math.max(10, minMemory), 500, 10);
            myMaxMemInput.setModel(spinnerNumberModel);
        } else {
            myMaxMemLabel.setVisible(false);
            myMaxMemInput.setVisible(false);
            myMaxMemSaveButton.setVisible(false);
            myMaxMemSaveButton.addActionListener(new MaxMemSaveButtonListener());
        }
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
        setServerStatus(MyTunesRss.BUNDLE.getString("info.server.idle"), null);
    }

    public void setServerRunningStatus(int serverPort) {
        String[] localAddresses = NetworkUtils.getLocalNetworkAddresses();
        if (localAddresses.length == 0) {
            setServerStatus(MyTunesRss.BUNDLE.getString("info.server.running"), null);
        } else {
            StringBuffer tooltip = new StringBuffer("<html>").append(MyTunesRss.BUNDLE.getString("info.server.running.addressInfo"));
            for (int i = 0; i < localAddresses.length; i++) {
                tooltip.append("http://").append(localAddresses[i]).append(":").append(serverPort);
                tooltip.append(i + 1 < localAddresses.length ? "<br>" : "</html>");
            }
            setServerStatus(MyTunesRss.BUNDLE.getString("info.server.running") + " [ http://" + localAddresses[0] + ":" + serverPort + " ] ",
                            tooltip.toString());
        }
        myRootPanel.validate();
    }

    public void updateConfigFromGui() {
        try {
            MyTunesRss.CONFIG.setPort(Integer.parseInt(myPortInput.getText().trim()));
        } catch (NumberFormatException e) {
            // intentionally left blank
        }
        MyTunesRss.CONFIG.setLibraryXml(myTunesXmlPathInput.getText().trim());
        MyTunesRss.CONFIG.setPassword(new String(myPasswordInput.getPassword()).trim());
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myPortInput, false);
                SwingUtils.enableElementAndLabel(myPasswordInput, false);
                SwingUtils.enableElementAndLabel(myMaxMemInput, false);
                myMaxMemSaveButton.setEnabled(false);
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, false);
                myTunesXmlPathLookupButton.setEnabled(false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myPortInput, true);
                SwingUtils.enableElementAndLabel(myPasswordInput, true);
                SwingUtils.enableElementAndLabel(myMaxMemInput, true);
                myMaxMemSaveButton.setEnabled(true);
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, true);
                myTunesXmlPathLookupButton.setEnabled(true);
                break;
        }
    }

    public void setServerStatus(String text, String tooltipText) {
        if (text != null) {
            myServerStatusLabel.setText(text);
        }
        if (tooltipText != null) {
            myServerStatusLabel.setToolTipText(tooltipText);
        }
    }

    public class TunesXmlPathLookupButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new ITunesLibraryFileFilter(true));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle(MyTunesRss.BUNDLE.getString("dialog.lookupLibraryXml.title"));
            if (fileChooser.showDialog(myRootPanel.getTopLevelAncestor(), null) == JFileChooser.APPROVE_OPTION) {
                try {
                    myTunesXmlPathInput.setText(fileChooser.getSelectedFile().getCanonicalPath());
                } catch (IOException e) {
                    SwingUtils.showErrorMessage(mySettingsForm.getFrame(),
                                                MyTunesRss.BUNDLE.getString("error.lookupLibraryXml.failure") + e.getMessage());
                }
            }
        }
    }

    public static class ITunesLibraryFileFilter extends javax.swing.filechooser.FileFilter {
        private boolean myAllowDirectories;

        public ITunesLibraryFileFilter(boolean allowDirectories) {
            myAllowDirectories = allowDirectories;
        }

        public boolean accept(File f) {
            return f != null && f.exists() &&
                    ((f.isDirectory() && myAllowDirectories) || (f.isFile() && LIBRARY_XML_FILE_NAME.equalsIgnoreCase(f.getName())));
        }

        public String getDescription() {
            return "iTunes Library";
        }
    }

    public class MaxMemSaveButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int maxMem = ((Integer)myMaxMemInput.getValue()).intValue();
            if (ProgramUtils.updateMemorySwitch(MemorySwitchType.Maxmimum, maxMem)) {
                SwingUtils.showInfoMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("info.savemem.success"));
            } else {
                SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("error.memsave.failure"));
            }
        }
    }

}