/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.*;
import de.codewave.utils.network.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

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
        int maxMemory = -1;// ProgramUtils.getMemorySwitch(MemorySwitchType.Maxmimum);
        if (maxMemory != -1) {
            SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(maxMemory, Math.max(10, minMemory), 500, 10);
            myMaxMemInput.setModel(spinnerNumberModel);
            myMaxMemSaveButton.addActionListener(new MaxMemSaveButtonListener());
        } else {
            myMaxMemLabel.setVisible(false);
            myMaxMemInput.setVisible(false);
            myMaxMemSaveButton.setVisible(false);
        }
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
        setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.idle"), null);
    }

    public void setServerRunningStatus(int serverPort, boolean getExternalAddress) {
        String[] localAddresses = NetworkUtils.getLocalNetworkAddresses();
        if (localAddresses.length == 0) {
            setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.running"), null);
        } else {
            StringBuffer tooltip = new StringBuffer("<html>").append(MyTunesRss.BUNDLE.getString("serverStatus.running.addresses"));
            for (int i = 0; i < localAddresses.length; i++) {
                tooltip.append("http://").append(localAddresses[i]).append(":").append(serverPort).append("<br>");
            }
            if (getExternalAddress) {
                String externalAddress = getExternalAddress();
                if (StringUtils.isNotEmpty(externalAddress) && !externalAddress.equals("unreachable")) {
                    tooltip.append(MyTunesRss.BUNDLE.getString("serverStatus.running.external"));
                    tooltip.append("http://").append(externalAddress).append(":").append(serverPort);
                } else {
                    tooltip.append(MyTunesRss.BUNDLE.getString("serverStatus.running.noExternal"));
                }
            }
            tooltip.append("</html>");
            setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.running") + " [ http://" + localAddresses[0] + ":" + serverPort + " ] ",
                            tooltip.toString());
        }
        myRootPanel.validate();
    }

    private String getExternalAddress() {
        BufferedReader reader = null;
        try {
            URLConnection connection = new URL("http://www.codewave.de/getip.php").openConnection();
            if (connection != null) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                if (reader != null) {
                    return reader.readLine();
                }
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not read my external address from \"www.codewave.de/getip.php\".", e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not close reader.", e);
                    }
                }
            }
        }
        return null;
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
            FileDialog fileDialog = new FileDialog(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("dialog.loadITunes"), FileDialog.LOAD);
            fileDialog.setVisible(true);
            if (fileDialog.getFile() != null) {
                File sourceFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
                try {
                    myTunesXmlPathInput.setText(sourceFile.getCanonicalPath());
                } catch (IOException e) {
                    SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("error.lookupLibraryXml") + e.getMessage());
                }
            }
        }
    }

    public static class ITunesLibraryFileFilter implements FilenameFilter {
        public boolean accept(File directory, String filename) {
            return filename != null && (new File(directory, filename).isFile() && LIBRARY_XML_FILE_NAME.equalsIgnoreCase(filename));
        }
    }

    public class MaxMemSaveButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int maxMem = ((Integer)myMaxMemInput.getValue()).intValue();
            if (ProgramUtils.updateMemorySwitch(MemorySwitchType.Maxmimum, maxMem)) {
                SwingUtils.showInfoMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("info.saveMaxMemoryDone"));
            } else {
                SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("error.saveMaxMemory"));
            }
        }
    }

}