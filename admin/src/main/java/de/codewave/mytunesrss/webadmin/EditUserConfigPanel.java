/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.validation.EmailValidator;
import de.codewave.vaadin.validation.SameValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Table myPermissions;
    private Table myPlaylistsRestrictions;
    private SmartTextField mySearchFuzziness;
    private Select myDownloadLimitType;
    private SmartTextField myDownloadLimitSize;
    private SmartTextField myMaxFilesPerArchive;
    private SmartTextField mySessionTimeout;
    private SmartTextField myBandwidthLimit;
    private CheckBox mySaveSettingsInProfile;
    private SmartTextField myLastFmUsername;
    private SmartTextField myLastFmPassword;
    private CheckBox myEncryptUrls;
    private User myUser;
    private UserConfigPanel myUserConfigPanel;

    public EditUserConfigPanel(UserConfigPanel userConfigPanel, User user) {
        myUserConfigPanel = userConfigPanel;
        myUser = user;
    }

    public void attach() {
        init(getBundleString("editUserConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));
        myUsername = getComponentFactory().createTextField("editUserConfigPanel.username", new UniqueUsernameValidator());
        myPassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.password");
        myRetypePassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.retypePassword", new SameValidator(myPassword, getBundleString("editUserConfigPanel.error.retypePassword")));
        myEmail = getComponentFactory().createTextField("editUserConfigPanel.email", new EmailValidator(getBundleString("editUserConfigPanel.error.email")));
        myIdentificationForm = getComponentFactory().createForm(null, true);
        myIdentificationForm.addField("username", myUsername);
        myIdentificationForm.addField("password", myPassword);
        myIdentificationForm.addField("retypePassword", myRetypePassword);
        myIdentificationForm.addField("email", myEmail);
        addComponent(getComponentFactory().surroundWithPanel(myIdentificationForm, FORM_PANEL_MARGIN_INFO, getBundleString("editUserConfigPanel.caption.identification")));
        myPermRss = new CheckBox();
        myPermPlaylist = new CheckBox();
        myPermDownload = new CheckBox();
        myPermStandardPlaylist = new CheckBox();
        myPermFlashPlayer = new CheckBox();
        myPermRemote = new CheckBox();
        myPermExternalLinks = new CheckBox();
        myPermEditTags = new CheckBox();
        myPermUpload = new CheckBox();
        myPermTranscoder = new CheckBox();
        myPermChangePassword = new CheckBox();
        myPermChangeEmail = new CheckBox();
        myPermEditLastFm = new CheckBox();
        myPermEditSettings = new CheckBox();
        myPermEditPlaylists = new CheckBox();
        myPermissions = new Table();
        myPermissions.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myPermissions.addContainerProperty("active", CheckBox.class, null, "", null, null);
        myPermissions.addContainerProperty("permission", String.class, null, getBundleString("editUserConfigPanel.permissions.name"), null, null);
        myPermissions.setColumnExpandRatio("permission", 1);
        myPermissions.setEditable(false);
        myPermissions.addItem(new Object[]{myPermRss, getBundleString("editUserConfigPanel.permRss")}, myPermRss);
        myPermissions.addItem(new Object[]{myPermPlaylist, getBundleString("editUserConfigPanel.permPlaylist")}, myPermPlaylist);
        myPermissions.addItem(new Object[]{myPermDownload, getBundleString("editUserConfigPanel.permDownload")}, myPermDownload);
        myPermissions.addItem(new Object[]{myPermStandardPlaylist, getBundleString("editUserConfigPanel.permStandardPlaylist")}, myPermStandardPlaylist);
        myPermissions.addItem(new Object[]{myPermFlashPlayer, getBundleString("editUserConfigPanel.permFlashPlayer")}, myPermFlashPlayer);
        myPermissions.addItem(new Object[]{myPermRemote, getBundleString("editUserConfigPanel.permRemote")}, myPermRemote);
        myPermissions.addItem(new Object[]{myPermExternalLinks, getBundleString("editUserConfigPanel.permExternalLinks")}, myPermExternalLinks);
        myPermissions.addItem(new Object[]{myPermEditTags, getBundleString("editUserConfigPanel.permEditTags")}, myPermEditTags);
        myPermissions.addItem(new Object[]{myPermUpload, getBundleString("editUserConfigPanel.permUpload")}, myPermUpload);
        myPermissions.addItem(new Object[]{myPermTranscoder, getBundleString("editUserConfigPanel.permTranscoder")}, myPermTranscoder);
        myPermissions.addItem(new Object[]{myPermChangePassword, getBundleString("editUserConfigPanel.permChangePassword")}, myPermChangePassword);
        myPermissions.addItem(new Object[]{myPermChangeEmail, getBundleString("editUserConfigPanel.permChangeEmail")}, myPermChangeEmail);
        myPermissions.addItem(new Object[]{myPermEditLastFm, getBundleString("editUserConfigPanel.permEditLastFm")}, myPermEditLastFm);
        myPermissions.addItem(new Object[]{myPermEditSettings, getBundleString("editUserConfigPanel.permEditSettings")}, myPermEditSettings);
        myPermissions.addItem(new Object[]{myPermEditPlaylists, getBundleString("editUserConfigPanel.permEditPlaylists")}, myPermEditPlaylists);
        myPermissions.setPageLength(Math.min(myPermissions.size(), 10));
        Panel panel = new Panel(getBundleString("editUserConfigPanel.caption.permissions"));
        panel.addComponent(myPermissions);
        addComponent(panel);
        myPlaylistsRestrictions = new Table();
        myPlaylistsRestrictions.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myPlaylistsRestrictions.addContainerProperty("restricted", CheckBox.class, null, "", null, null);
        myPlaylistsRestrictions.addContainerProperty("name", String.class, null, getBundleString("editUserConfigPanel.playlists.name"), null, null);
        myPlaylistsRestrictions.setColumnExpandRatio("name", 1);
        myPlaylistsRestrictions.setEditable(false);
        panel = new Panel(getBundleString("editUserConfigPanel.caption.restrictedPlaylists"));
        panel.addComponent(myPlaylistsRestrictions);
        addComponent(panel);
        mySearchFuzziness = getComponentFactory().createTextField("editUserConfigPanel.searchFuzziness", getApplication().getValidatorFactory().createMinMaxValidator(0, 100));
        myDownloadLimitType = getComponentFactory().createSelect("editUserConfigPanel.downloadLimitType", Arrays.asList(User.QuotaType.values()));
        myDownloadLimitType.setNewItemsAllowed(false);
        myDownloadLimitSize = getComponentFactory().createTextField("editUserConfigPanel.downloadLimitSize", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        myMaxFilesPerArchive = getComponentFactory().createTextField("editUserConfigPanel.maxFilesPerArchive", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        mySessionTimeout = getComponentFactory().createTextField("editUserConfigPanel.sessionTimeout", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        myBandwidthLimit = getComponentFactory().createTextField("editUserConfigPanel.bandwidthLimit", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
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
        myOptionsForm.addField("saveSettingsInProfile", mySaveSettingsInProfile);
        myOptionsForm.addField("lastFmUsername", myLastFmUsername);
        myOptionsForm.addField("lastFmPassword", myLastFmPassword);
        myOptionsForm.addField("encryptUrls", myEncryptUrls);
        addComponent(getComponentFactory().surroundWithPanel(myOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("editUserConfigPanel.caption.options")));

        addMainButtons(0, 4, 0, 4);

        initFromConfig();
    }

    protected void initFromConfig() {
        if (myUser != null) {
            //myUser.setActive(); // TODO activation
            myBandwidthLimit.setValue(myUser.getBandwidthLimit(), 1, Integer.MAX_VALUE, "");
            myDownloadLimitSize.setValue(myUser.getQuotaDownBytes(), 1, Integer.MAX_VALUE, "");
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
            myMaxFilesPerArchive.setValue(myUser.getMaximumZipEntries(), 1, Integer.MAX_VALUE, "");
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
            mySearchFuzziness.setValue(myUser.getSearchFuzziness(), 0, 100, "");
            mySessionTimeout.setValue(myUser.getSessionTimeout(), 1, Integer.MAX_VALUE, 10);
            myPermStandardPlaylist.setValue(myUser.isSpecialPlaylists());
            myPermTranscoder.setValue(myUser.isTranscoder());
            myPermUpload.setValue(myUser.isUpload());
            myEncryptUrls.setValue(myUser.isUrlEncryption());
            //myUser.setWebSettings();
            myPlaylistsRestrictions.removeAllItems();
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            List<Playlist> playlists = null;
            try {
                playlists = session.executeQuery(new FindPlaylistQuery(Arrays.asList(PlaylistType.ITunes, PlaylistType.ITunesFolder, PlaylistType.M3uFile), null, null, true)).getResults();
                for (Playlist playlist : playlists) {
                    CheckBox restricted = new CheckBox();
                    restricted.setValue(myUser.getPlaylistIds().contains(playlist.getId()));
                    myPlaylistsRestrictions.addItem(new Object[]{restricted, playlist.getName()}, playlist);
                }
            } catch (SQLException e) {
                getApplication().handleException(e);
            }
            myPlaylistsRestrictions.setPageLength(Math.min(playlists.size(), 10));
        }
    }

    protected void writeToConfig() {
        //myUser.setActive();
        myUser.setBandwidthLimit(myBandwidthLimit.getIntegerValue(-1));
        myUser.setBytesQuota(myDownloadLimitSize.getLongValue(-1));
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
        myUser.setMaximumZipEntries(myMaxFilesPerArchive.getIntegerValue(-1));
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
        myUser.setSearchFuzziness(mySearchFuzziness.getIntegerValue(-1));
        myUser.setSessionTimeout(mySessionTimeout.getIntegerValue(10));
        myUser.setSpecialPlaylists(myPermStandardPlaylist.booleanValue());
        myUser.setTranscoder(myPermTranscoder.booleanValue());
        myUser.setUpload(myPermUpload.booleanValue());
        myUser.setUrlEncryption(myEncryptUrls.booleanValue());
        //myUser.setWebSettings();
        Set<String> ids = new HashSet<String>();
        for (Object itemId : myPlaylistsRestrictions.getItemIds()) {
            Playlist playlist = (Playlist) itemId;
            if ((Boolean) getTableCellPropertyValue(myPlaylistsRestrictions, playlist, "restricted")) {
                ids.add(playlist.getId());
            }
        }
        myUser.setPlaylistIds(ids);
        myUserConfigPanel.saveUser(myUser);
    }

    @Override
    protected Component getSaveFollowUpComponent() {
        return myUserConfigPanel;
    }

    @Override
    protected Component getCancelFollowUpComponent() {
        return myUserConfigPanel;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }

    public class UniqueUsernameValidator extends AbstractStringValidator {

        public UniqueUsernameValidator() {
            super(getBundleString("editUserConfigPanel.error.usernameMustBeUnique"));
        }

        @Override
        protected boolean isValidString(String s) {
            for (User user : myUserConfigPanel.getUsers()) {
                if (user != myUser && StringUtils.equalsIgnoreCase(user.getName(), s)) {
                    return false;
                }
            }
            return true;
        }
    }
}