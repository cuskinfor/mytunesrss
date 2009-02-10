package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.settings.AdminNotify
 */
public class AdminNotify implements SettingsForm {
    private JPanel myRootPanel;
    private JTextField myAdminEmailInput;
    private JCheckBox myNotifyEmailChangeInput;
    private JCheckBox myNotifyDatabaseUpdateInput;
    private JCheckBox myNotifyInternalErrorInput;
    private JCheckBox myNotifyLoginFailureInput;
    private JCheckBox myNotifyPasswordChangeInput;
    private JCheckBox myNotifyQuotaInput;
    private JCheckBox myNotifyTranscodingFailureInput;
    private JCheckBox myNotifyWebUploadInput;
    private JCheckBox myNotifyMissingFileInput;


    public void init() {
        initValues();
    }

    public void initValues() {
        myAdminEmailInput.setText(MyTunesRss.CONFIG.getAdminEmail());
        myNotifyDatabaseUpdateInput.setSelected(MyTunesRss.CONFIG.isNotifyOnDatabaseUpdate());
        myNotifyEmailChangeInput.setSelected(MyTunesRss.CONFIG.isNotifyOnEmailChange());
        myNotifyInternalErrorInput.setSelected(MyTunesRss.CONFIG.isNotifyOnInternalError());
        myNotifyLoginFailureInput.setSelected(MyTunesRss.CONFIG.isNotifyOnLoginFailure());
        myNotifyPasswordChangeInput.setSelected(MyTunesRss.CONFIG.isNotifyOnPasswordChange());
        myNotifyQuotaInput.setSelected(MyTunesRss.CONFIG.isNotifyOnQuotaExceeded());
        myNotifyTranscodingFailureInput.setSelected(MyTunesRss.CONFIG.isNotifyOnTranscodingFailure());
        myNotifyWebUploadInput.setSelected(MyTunesRss.CONFIG.isNotifyOnWebUpload());
        myNotifyMissingFileInput.setSelected(MyTunesRss.CONFIG.isNotifyOnMissingFile());
    }

    public String updateConfigFromGui() {
        MyTunesRss.CONFIG.setAdminEmail(myAdminEmailInput.getText());
        MyTunesRss.CONFIG.setNotifyOnDatabaseUpdate(myNotifyDatabaseUpdateInput.isSelected());
        MyTunesRss.CONFIG.setNotifyOnEmailChange(myNotifyEmailChangeInput.isSelected());
        MyTunesRss.CONFIG.setNotifyOnInternalError(myNotifyInternalErrorInput.isSelected());
        MyTunesRss.CONFIG.setNotifyOnLoginFailure(myNotifyLoginFailureInput.isSelected());
        MyTunesRss.CONFIG.setNotifyOnPasswordChange(myNotifyPasswordChangeInput.isSelected());
        MyTunesRss.CONFIG.setNotifyOnQuotaExceeded(myNotifyQuotaInput.isSelected());
        MyTunesRss.CONFIG.setNotifyOnTranscodingFailure(myNotifyTranscodingFailureInput.isSelected());
        MyTunesRss.CONFIG.setNotifyOnWebUpload(myNotifyWebUploadInput.isSelected());
        MyTunesRss.CONFIG.setNotifyOnMissingFile(myNotifyMissingFileInput.isSelected());
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.adminNotify.title");
    }
}