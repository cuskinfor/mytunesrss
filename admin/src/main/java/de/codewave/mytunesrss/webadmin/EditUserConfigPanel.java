/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Select;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.validation.EmailValidator;
import de.codewave.vaadin.validation.SameValidator;
import de.codewave.vaadin.validation.ValidationTriggerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class EditUserConfigPanel extends MyTunesRssConfigPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditUserConfigPanel.class);

    private Form myIdentificationForm;
    private Form myPermissionsForm;
    private Form myOptionsForm;
    private SmartTextField myUsername;
    private SmartTextField myPassword;
    private SmartTextField myRetypePassword;
    private SmartTextField myEmail;
    private CheckBox myPermRss;
    private CheckBox myPermPlaylist;
    private CheckBox myPermDownload;
    private CheckBox myPermStandardPlaylist;
    private CheckBox myPermFlashPlayer;
    private CheckBox myPermRemote;
    private CheckBox myPermExternalLinks;
    private CheckBox myPermEditTags;
    private CheckBox myPermUpload;
    private CheckBox myPermTranscoder;
    private CheckBox myPermChangePassword;
    private CheckBox myPermChangeEmail;
    private CheckBox myPermEditLastFm;
    private CheckBox myPermEditSettings;
    private CheckBox myPermEditPlaylists;
    private SmartTextField mySearchFuzziness;
    private Select myDownloadLimitType;
    private SmartTextField myDownloadLimitSize;
    private SmartTextField myMaxFilesPerArchive;
    private SmartTextField mySessionTimeout;
    private SmartTextField myBandwidthLimit;
    private Button myEditRestrictionPlaylists;
    private CheckBox mySaveSettingsInProfile;
    private SmartTextField myLastFmUsername;
    private SmartTextField myLastFmPassword;
    private CheckBox myEncryptUrls;
    private User myUser;
    private UserConfigPanel myUserConfigPanel;
    private boolean myNewUser;

    public EditUserConfigPanel(Application application, ComponentFactory componentFactory, UserConfigPanel userConfigPanel, User user, boolean newUser) {
        super(application, getBundleString("editUserConfigPanel.caption"), componentFactory.createGridLayout(1, 4, true, true), componentFactory);
        myUserConfigPanel = userConfigPanel;
        myUser = user;
        myNewUser = newUser;
        initFields(application);
    }

    protected void init(Application application) {
        myUsername = getComponentFactory().createTextField("editUserConfigPanel.username"); // TODO unique username validation
        myPassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.password");
        myRetypePassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.retypePassword", new SameValidator(myPassword, getBundleString("editUserConfigPanel.error.retypePassword")));
        myPassword.addValidator(new ValidationTriggerValidator(myRetypePassword));
        myEmail = getComponentFactory().createTextField("editUserConfigPanel.email", new EmailValidator("editUserConfigPanel.error.email"));
        myIdentificationForm = getComponentFactory().createForm(null, true);
        myIdentificationForm.addField("username", myUsername);
        myIdentificationForm.addField("password", myPassword);
        myIdentificationForm.addField("retypePassword", myRetypePassword);
        myIdentificationForm.addField("email", myEmail);
        addComponent(getComponentFactory().surroundWithPanel(myIdentificationForm, FORM_PANEL_MARGIN_INFO, getBundleString("editUserConfigPanel.caption.identification")));
        myPermRss = getComponentFactory().createCheckBox("editUserConfigPanel.permRss");
        myPermPlaylist = getComponentFactory().createCheckBox("editUserConfigPanel.permPlaylist");
        myPermDownload = getComponentFactory().createCheckBox("editUserConfigPanel.permDownload");
        myPermStandardPlaylist = getComponentFactory().createCheckBox("editUserConfigPanel.permStandardPlaylist");
        myPermFlashPlayer = getComponentFactory().createCheckBox("editUserConfigPanel.permFlashPlayer");
        myPermRemote = getComponentFactory().createCheckBox("editUserConfigPanel.permRemote");
        myPermExternalLinks = getComponentFactory().createCheckBox("editUserConfigPanel.permExternalLinks");
        myPermEditTags = getComponentFactory().createCheckBox("editUserConfigPanel.permEditTags");
        myPermUpload = getComponentFactory().createCheckBox("editUserConfigPanel.permUpload");
        myPermTranscoder = getComponentFactory().createCheckBox("editUserConfigPanel.permTranscoder");
        myPermChangePassword = getComponentFactory().createCheckBox("editUserConfigPanel.permChangePassword");
        myPermChangeEmail = getComponentFactory().createCheckBox("editUserConfigPanel.permChangeEmail");
        myPermEditLastFm = getComponentFactory().createCheckBox("editUserConfigPanel.permEditLastFm");
        myPermEditSettings = getComponentFactory().createCheckBox("editUserConfigPanel.permEditSettings");
        myPermEditPlaylists = getComponentFactory().createCheckBox("editUserConfigPanel.permEditPlaylists");
        myPermissionsForm = getComponentFactory().createForm(null, true);
        myPermissionsForm.addField("permRss", myPermRss);
        myPermissionsForm.addField("permPlaylist", myPermPlaylist);
        myPermissionsForm.addField("permDownload", myPermDownload);
        myPermissionsForm.addField("permStandardPlaylists", myPermStandardPlaylist);
        myPermissionsForm.addField("permFlashPlayer", myPermFlashPlayer);
        myPermissionsForm.addField("permRemote", myPermRemote);
        myPermissionsForm.addField("permExternalLinks", myPermExternalLinks);
        myPermissionsForm.addField("permEditTags", myPermEditTags);
        myPermissionsForm.addField("permUpload", myPermUpload);
        myPermissionsForm.addField("permTranscoder", myPermTranscoder);
        myPermissionsForm.addField("permChangePassword", myPermChangePassword);
        myPermissionsForm.addField("permChangeEmail", myPermChangeEmail);
        myPermissionsForm.addField("permEditLastFm", myPermEditLastFm);
        myPermissionsForm.addField("permEditSettings", myPermEditSettings);
        myPermissionsForm.addField("permEditPlaylists", myPermEditPlaylists);
        addComponent(getComponentFactory().surroundWithPanel(myPermissionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("editUserConfigPanel.caption.permissions")));
        mySearchFuzziness = getComponentFactory().createTextField("editUserConfigPanel.searchFuzziness");
        myDownloadLimitType = getComponentFactory().createSelect("editUserConfigPanel.downloadLimitType", Arrays.asList(User.QuotaType.values()));
        myDownloadLimitType.setNewItemsAllowed(false);
        myDownloadLimitSize = getComponentFactory().createTextField("editUserConfigPanel.downloadLimitSize");
        myMaxFilesPerArchive = getComponentFactory().createTextField("editUserConfigPanel.maxFilesPerArchive");
        mySessionTimeout = getComponentFactory().createTextField("editUserConfigPanel.sessionTimeout");
        myBandwidthLimit = getComponentFactory().createTextField("editUserConfigPanel.bandwidthLimit");
        myEditRestrictionPlaylists = getComponentFactory().createButton("editUserConfigPanel.editRestrictionPlaylists", this);
        mySaveSettingsInProfile = getComponentFactory().createCheckBox("editUserConfigPanel.saveSettingsInProfile");
        myLastFmUsername = getComponentFactory().createTextField("editUserConfigPanel.lastFmUsername");
        myLastFmPassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.lastFmPassword");
        myEncryptUrls = getComponentFactory().createCheckBox("editUserConfigPanel.encryptUrls");
        myOptionsForm = getComponentFactory().createForm(null, true);
        myOptionsForm.addField("searchFuzziness", mySearchFuzziness);
        myOptionsForm.addField("downloadLimitType", myDownloadLimitType);
        myOptionsForm.addField("downloadLimitSize", myDownloadLimitSize);
        myOptionsForm.addField("maxFilesPerArchive", myMaxFilesPerArchive);
        myOptionsForm.addField("sessionTimeout", mySessionTimeout);
        myOptionsForm.addField("bandwidthLimit", myBandwidthLimit);
        myOptionsForm.addField("editRestrictionPlaylists", myEditRestrictionPlaylists);
        myOptionsForm.addField("saveSettingsInProfile", mySaveSettingsInProfile);
        myOptionsForm.addField("lastFmUsername", myLastFmUsername);
        myOptionsForm.addField("lastFmPassword", myLastFmPassword);
        myOptionsForm.addField("encryptUrls", myEncryptUrls);
        addComponent(getComponentFactory().surroundWithPanel(myOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("editUserConfigPanel.caption.options")));

        addMainButtons(0, 3, 0, 3);

    }

    protected void initFromConfig(Application application) {
        // intentionally left blank
    }

    private void initFields(Application application) {
        //myUser.setActive(); // TODO activation
        myBandwidthLimit.setValue(myUser.getBandwidthLimit());
        myDownloadLimitSize.setValue(myUser.getQuotaDownBytes());
        myPermChangeEmail.setValue(myUser.isChangeEmail());
        myPermChangePassword.setValue(myUser.isChangePassword());
        myPermEditPlaylists.setValue(myUser.isCreatePlaylists());
        //myUser.setDownBytes();
        myPermDownload.setValue(myUser.isDownload());
        myPermEditLastFm.setValue(myUser.isEditLastFmAccount());
        myPermEditTags.setValue(myUser.isEditTags());
        myPermEditSettings.setValue(myUser.isEditWebSettings());
        myEmail.setValue(myUser.getEmail());
        myPermExternalLinks.setValue(myUser.isExternalSites());
        //myUser.setFileTypes(...);
        myLastFmPassword.setValue(myUser.getLastFmPasswordHash());
        myLastFmUsername.setValue(myUser.getLastFmUsername());
        myMaxFilesPerArchive.setValue(myUser.getMaximumZipEntries());
        myUsername.setValue(myUser.getName());
        myPassword.setValue(myUser.getPasswordHash());
        myRetypePassword.setValue(myUser.getPasswordHash());
        myPermFlashPlayer.setValue(myUser.isPlayer());
        myPermPlaylist.setValue(myUser.isPlaylist());
        //myUser.setQuotaDownBytes();
        //myUser.setQuotaResetTime();
        myDownloadLimitType.setValue(myUser.getQuotaType());
        myPermRemote.setValue(myUser.isRemoteControl());
        //myUser.setResetTime();
        myPermRss.setValue(myUser.isRss());
        mySaveSettingsInProfile.setValue(myUser.isSaveWebSettings());
        mySearchFuzziness.setValue(myUser.getSearchFuzziness());
        mySessionTimeout.setValue(myUser.getSessionTimeout());
        myPermStandardPlaylist.setValue(myUser.isSpecialPlaylists());
        myPermTranscoder.setValue(myUser.isTranscoder());
        myPermUpload.setValue(myUser.isUpload());
        myEncryptUrls.setValue(myUser.isUrlEncryption());
        //myUser.setWebSettings();
    }

    protected void writeToConfig() {
        //myUser.setActive();
        myUser.setBandwidthLimit(myBandwidthLimit.getIntegerValue(0));
        myUser.setBytesQuota(myDownloadLimitSize.getLongValue(0));
        myUser.setChangeEmail(myPermChangeEmail.booleanValue());
        myUser.setChangePassword(myPermChangePassword.booleanValue());
        myUser.setCreatePlaylists(myPermEditPlaylists.booleanValue());
        //myUser.setDownBytes();
        myUser.setDownload(myPermDownload.booleanValue());
        myUser.setEditLastFmAccount(myPermEditLastFm.booleanValue());
        myUser.setEditTags(myPermEditTags.booleanValue());
        myUser.setEditWebSettings(myPermEditSettings.booleanValue());
        myUser.setEmail((String) myEmail.getValue());
        myUser.setExternalSites(myPermExternalLinks.booleanValue());
        //myUser.setFileTypes(...);
        myUser.setLastFmPasswordHash(myLastFmPassword.getStringHashValue(MyTunesRss.MD5_DIGEST));
        myUser.setLastFmUsername((String) myLastFmUsername.getValue());
        myUser.setMaximumZipEntries(myMaxFilesPerArchive.getIntegerValue(0));
        myUser.setName((String) myUsername.getValue());
        myUser.setPasswordHash(myPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST));
        myUser.setPlayer(myPermFlashPlayer.booleanValue());
        myUser.setPlaylist(myPermPlaylist.booleanValue());
        //myUser.setQuotaDownBytes();
        //myUser.setQuotaResetTime();
        myUser.setQuotaType((User.QuotaType) myDownloadLimitType.getValue());
        myUser.setRemoteControl(myPermRemote.booleanValue());
        //myUser.setResetTime();
        myUser.setRss(myPermRss.booleanValue());
        myUser.setSaveWebSettings(mySaveSettingsInProfile.booleanValue());
        myUser.setSearchFuzziness(mySearchFuzziness.getIntegerValue(0));
        myUser.setSessionTimeout(mySessionTimeout.getIntegerValue(0));
        myUser.setSpecialPlaylists(myPermStandardPlaylist.booleanValue());
        myUser.setTranscoder(myPermTranscoder.booleanValue());
        myUser.setUpload(myPermUpload.booleanValue());
        myUser.setUrlEncryption(myEncryptUrls.booleanValue());
        //myUser.setWebSettings();
    }

    @Override
    protected boolean beforeSave() {
        writeToConfig();
        getApplication().setMainComponent(myUserConfigPanel);
        return false;
    }

    @Override
    protected boolean beforeReset() {
        initFields(getApplication());
        return false;
    }

    @Override
    protected boolean beforeCancel() {
        getApplication().setMainComponent(myUserConfigPanel);
        return false;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }
}