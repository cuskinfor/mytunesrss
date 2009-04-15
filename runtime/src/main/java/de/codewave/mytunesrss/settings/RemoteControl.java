package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.MinMaxValueTextFieldValidation;

import javax.swing.*;

import org.apache.commons.lang.SystemUtils;

/**
 * de.codewave.mytunesrss.settings.RemoteControl
 */
public class RemoteControl implements SettingsForm {
    private JPanel myRootPanel;
    private JTextField myVlcHostInput;
    private JTextField myVlcPortInput;
    private JComboBox myRemoteControlTypeInput;

    public void init() {
        myRemoteControlTypeInput.addItem(RemoteControlType.None);
        myRemoteControlTypeInput.addItem(RemoteControlType.Vlc);
        if (SystemUtils.IS_OS_MAC_OSX) {
            myRemoteControlTypeInput.addItem(RemoteControlType.Quicktime);
        }
        initValues();
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myVlcPortInput, 1, 65535, true, MyTunesRssUtils.getBundleString(
                "error.illegalVlcPort")));
    }

    public void initValues() {
        myVlcHostInput.setText(MyTunesRss.CONFIG.getVideoLanClientHost());
        myVlcPortInput.setText(MyTunesRss.CONFIG.getVideoLanClientPort() > 0 ? Integer.toString(MyTunesRss.CONFIG.getVideoLanClientPort()) : "");
        myRemoteControlTypeInput.setSelectedItem(MyTunesRss.CONFIG.getRemoteControlType());
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages == null) {
            MyTunesRss.CONFIG.setVideoLanClientHost(myVlcHostInput.getText());
            MyTunesRss.CONFIG.setVideoLanClientPort(MyTunesRssUtils.getStringInteger(myVlcPortInput.getText(), 0));
            MyTunesRss.CONFIG.setRemoteControlType((RemoteControlType) myRemoteControlTypeInput.getSelectedItem());
        }
        return messages;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.remotecontrol.title");
    }
}