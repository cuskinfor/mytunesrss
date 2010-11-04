/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Property;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.TranscoderConfig;
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
import java.util.*;

public class EditUserConfigPanel extends MyTunesRssConfigPanel implements Property.ValueChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditUserConfigPanel.class);

    private Form myIdentificationForm;
    private Form myOptionsForm;
    private SmartTextField myUsername;
    private SmartTextField myPassword;
    private SmartTextField myRetypePassword;
    private SmartTextField myEmail;
    private CheckBox myPermRss;
    private CheckBox myPermPlaylist;
    private CheckBox myPermDownload;
    private CheckBox myPermYahooPlayer;
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
    private Table myForceTranscoders;
    private SmartTextField mySearchFuzziness;
    private Select myDownloadLimitType;
    private SmartTextField myDownloadLimitSize;
    private SmartTextField myMaxFilesPerArchive;
    private SmartTextField mySessionTimeout;
    private SmartTextField myBandwidthLimit;
    private CheckBox mySharedUser;
    private SmartTextField myLastFmUsername;
    private SmartTextField myLastFmPassword;
    private CheckBox myEncryptUrls;
    private User myUser;
    private UserConfigPanel myUserConfigPanel;
    private CheckBox myExpire;
    private DateField myExpiration;
    private boolean myNewUser;

    public EditUserConfigPanel(UserConfigPanel userConfigPanel, User user, boolean newUser) {
        myUserConfigPanel = userConfigPanel;
        myUser = user;
        myNewUser = newUser;
    }

    public void attach() {
        int rows = myUser.getParent() == null ? 6 : 3;
        init(getBundleString("editUserConfigPanel.caption"), getComponentFactory().createGridLayout(1, rows, true, true));
        myUsername = getComponentFactory().createTextField("editUserConfigPanel.username", new UniqueUsernameValidator());
        myPassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.password");
        myRetypePassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.retypePassword", new SameValidator(myPassword, getBundleString("editUserConfigPanel.error.retypePassword")));
        myEmail = getComponentFactory().createTextField("editUserConfigPanel.email", new EmailValidator(getBundleString("editUserConfigPanel.error.email")));
        myExpire = getComponentFactory().createCheckBox("editUserConfigPanel.expire");
        myExpire.addListener((Property.ValueChangeListener) this);
        myExpiration = new DateField(getBundleString("editUserConfigPanel.expiration"), new Date(0));
        myExpiration.setLenient(false);
        myExpiration.setDateFormat(MyTunesRssUtils.getBundleString(Locale.getDefault(), "common.dateFormat"));
        myExpiration.setResolution(DateField.RESOLUTION_DAY);
        myIdentificationForm = getComponentFactory().createForm(null, true);
        myIdentificationForm.addField("username", myUsername);
        if (!myUser.isGroup()) {
            myIdentificationForm.addField("password", myPassword);
            myIdentificationForm.addField("retypePassword", myRetypePassword);
            myIdentificationForm.addField("email", myEmail);
            myIdentificationForm.addField("expireCheck", myExpire);
            myIdentificationForm.addField("expiration", myExpiration);
        }
        addComponent(getComponentFactory().surroundWithPanel(myIdentificationForm, FORM_PANEL_MARGIN_INFO, getBundleString("editUserConfigPanel.caption.identification")));
        myPermRss = new CheckBox();
        myPermPlaylist = new CheckBox();
        myPermDownload = new CheckBox();
        myPermYahooPlayer = new CheckBox();
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
        Panel panel = null;
        myPermissions = new Table();
        myPermissions.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myPermissions.addContainerProperty("active", CheckBox.class, null, "", null, null);
        myPermissions.addContainerProperty("permission", String.class, null, getBundleString("editUserConfigPanel.permissions.name"), null, null);
        myPermissions.setColumnExpandRatio("permission", 1);
        myPermissions.setEditable(false);
        myPermissions.addItem(new Object[]{myPermRss, getBundleString("editUserConfigPanel.permRss")}, myPermRss);
        myPermissions.addItem(new Object[]{myPermPlaylist, getBundleString("editUserConfigPanel.permPlaylist")}, myPermPlaylist);
        myPermissions.addItem(new Object[]{myPermDownload, getBundleString("editUserConfigPanel.permDownload")}, myPermDownload);
        myPermissions.addItem(new Object[]{myPermYahooPlayer, getBundleString("editUserConfigPanel.permYahooPlayer")}, myPermYahooPlayer);
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
        panel = new Panel(getBundleString("editUserConfigPanel.caption.permissions"));
        panel.addComponent(myPermissions);
        if (myUser.getParent() == null) {
            addComponent(panel);
        }
        myPlaylistsRestrictions = new Table();
        myPlaylistsRestrictions.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myPlaylistsRestrictions.addContainerProperty("restricted", CheckBox.class, null, getBundleString("editUserConfigPanel.playlists.restricted"), null, null);
        myPlaylistsRestrictions.addContainerProperty("excluded", CheckBox.class, null, getBundleString("editUserConfigPanel.playlists.excluded"), null, null);
        myPlaylistsRestrictions.addContainerProperty("name", String.class, null, getBundleString("editUserConfigPanel.playlists.name"), null, null);
        myPlaylistsRestrictions.setColumnExpandRatio("name", 1);
        myPlaylistsRestrictions.setEditable(false);
        myPlaylistsRestrictions.setSortContainerPropertyId("name");
        panel = new Panel(getBundleString("editUserConfigPanel.caption.restrictedPlaylists"));
        panel.addComponent(myPlaylistsRestrictions);
        if (myUser.getParent() == null) {
            addComponent(panel);
        }
        myForceTranscoders = new Table();
        myForceTranscoders.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myForceTranscoders.addContainerProperty("active", CheckBox.class, null, "", null, null);
        myForceTranscoders.addContainerProperty("name", String.class, null, getBundleString("editUserConfigPanel.forceTranscoders.name"), null, null);
        myForceTranscoders.setColumnExpandRatio("name", 1);
        myForceTranscoders.setEditable(false);
        myForceTranscoders.setSortContainerPropertyId("name");
        panel = new Panel(getBundleString("editUserConfigPanel.caption.forceTranscoders"));
        panel.addComponent(myForceTranscoders);
        if (myUser.getParent() == null) {
            addComponent(panel);
        }
        mySearchFuzziness = getComponentFactory().createTextField("editUserConfigPanel.searchFuzziness", getApplication().getValidatorFactory().createMinMaxValidator(0, 100));
        myDownloadLimitType = getComponentFactory().createSelect("editUserConfigPanel.downloadLimitType", Arrays.asList(User.QuotaType.values()));
        myDownloadLimitType.setNewItemsAllowed(false);
        myDownloadLimitSize = getComponentFactory().createTextField("editUserConfigPanel.downloadLimitSize", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        myMaxFilesPerArchive = getComponentFactory().createTextField("editUserConfigPanel.maxFilesPerArchive", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        mySessionTimeout = getComponentFactory().createTextField("editUserConfigPanel.sessionTimeout", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        myBandwidthLimit = getComponentFactory().createTextField("editUserConfigPanel.bandwidthLimit", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        mySharedUser = getComponentFactory().createCheckBox("editUserConfigPanel.sharedUser");
        myLastFmUsername = getComponentFactory().createTextField("editUserConfigPanel.lastFmUsername");
        myLastFmPassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.lastFmPassword");
        myEncryptUrls = getComponentFactory().createCheckBox("editUserConfigPanel.encryptUrls");
        myOptionsForm = getComponentFactory().createForm(null, true);
        if (myUser.getParent() == null) {
            myOptionsForm.addField("searchFuzziness", mySearchFuzziness);
            myOptionsForm.addField("downloadLimitType", myDownloadLimitType);
            myOptionsForm.addField("downloadLimitSize", myDownloadLimitSize);
            myOptionsForm.addField("maxFilesPerArchive", myMaxFilesPerArchive);
            myOptionsForm.addField("sessionTimeout", mySessionTimeout);
            myOptionsForm.addField("bandwidthLimit", myBandwidthLimit);
            myOptionsForm.addField("sharedUser", mySharedUser);
            myOptionsForm.addField("encryptUrls", myEncryptUrls);
        }
        if (!myUser.isGroup()) {
            myOptionsForm.addField("lastFmUsername", myLastFmUsername);
            myOptionsForm.addField("lastFmPassword", myLastFmPassword);
        }
        addComponent(getComponentFactory().surroundWithPanel(myOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("editUserConfigPanel.caption.options")));

        attach(0, rows - 1, 0, rows - 1);

        initFromConfig();
    }

    protected void initFromConfig() {
        if (myUser != null) {
            myBandwidthLimit.setValue(myUser.getBandwidthLimit(), 1, Integer.MAX_VALUE, "");
            myDownloadLimitSize.setValue(myUser.getBytesQuota(), 1, Integer.MAX_VALUE, "");
            myPermChangeEmail.setValue(myUser.isChangeEmail());
            myPermChangePassword.setValue(myUser.isChangePassword());
            myPermEditPlaylists.setValue(myUser.isCreatePlaylists());
            myPermDownload.setValue(myUser.isDownload());
            myPermYahooPlayer.setValue(myUser.isYahooPlayer());
            myPermEditLastFm.setValue(myUser.isEditLastFmAccount());
            myPermEditTags.setValue(myUser.isEditTags());
            myPermEditSettings.setValue(myUser.isEditWebSettings());
            myEmail.setValue(myUser.getEmail());
            myPermExternalLinks.setValue(myUser.isExternalSites());
            myLastFmPassword.setValue(myUser.getLastFmPasswordHash());
            myLastFmUsername.setValue(myUser.getLastFmUsername());
            myMaxFilesPerArchive.setValue(myUser.getMaximumZipEntries(), 1, Integer.MAX_VALUE, "");
            myUsername.setValue(myUser.getName());
            myPassword.setValue(myUser.getPasswordHash());
            myRetypePassword.setValue(myUser.getPasswordHash());
            myPermFlashPlayer.setValue(myUser.isPlayer());
            myPermPlaylist.setValue(myUser.isPlaylist());
            myDownloadLimitType.setValue(myUser.getQuotaType());
            myPermRemote.setValue(myUser.isRemoteControl());
            myPermRss.setValue(myUser.isRss());
            mySharedUser.setValue(myUser.isSharedUser());
            mySearchFuzziness.setValue(myUser.getSearchFuzziness(), 0, 100, "");
            mySessionTimeout.setValue(myUser.getSessionTimeout(), 1, Integer.MAX_VALUE, 10);
            myPermStandardPlaylist.setValue(myUser.isSpecialPlaylists());
            myPermTranscoder.setValue(myUser.isTranscoder());
            myPermUpload.setValue(myUser.isUpload());
            myEncryptUrls.setValue(myUser.isUrlEncryption());
            myPlaylistsRestrictions.removeAllItems();
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            List<Playlist> playlists = null;
            try {
                playlists = session.executeQuery(new FindPlaylistQuery(Arrays.asList(PlaylistType.ITunes, PlaylistType.ITunesFolder, PlaylistType.M3uFile, PlaylistType.MyTunes, PlaylistType.MyTunesSmart), null, null, true)).getResults();
                for (Playlist playlist : playlists) {
                    CheckBox restricted = new CheckBox();
                    restricted.setValue(myUser.getRestrictedPlaylistIds().contains(playlist.getId()));
                    CheckBox excluded = new CheckBox();
                    excluded.setValue(myUser.getExcludedPlaylistIds().contains(playlist.getId()));
                    StringBuilder name = new StringBuilder();
                    for (Playlist pathElement : MyTunesRssUtils.getPlaylistPath(playlist, playlists)) {
                        name.append(" \u21E8 ").append(pathElement.getName());
                    }
                    myPlaylistsRestrictions.addItem(new Object[]{restricted, excluded, name.substring(3)}, playlist);
                }
                myPlaylistsRestrictions.sort();
            } catch (SQLException e) {
                getApplication().handleException(e);
            }
            myPlaylistsRestrictions.setPageLength(Math.min(playlists.size(), 10));
            myForceTranscoders.removeAllItems();
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                CheckBox active = new CheckBox();
                active.setValue(myUser.getForceTranscoders().contains(config.getName()));
                myForceTranscoders.addItem(new Object[]{active, config.getName()}, config.getName());
            }
            myForceTranscoders.sort();
            myForceTranscoders.setPageLength(Math.min(MyTunesRss.CONFIG.getTranscoderConfigs().size(), 10));
            if (myUser.getExpiration() > 0) {
                myExpire.setValue(true);
                myExpiration.setVisible(true);
                myExpiration.setValue(new Date(myUser.getExpiration()));
            } else {
                myExpire.setValue(false);
                myExpiration.setVisible(false);
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(GregorianCalendar.HOUR_OF_DAY, 23);
                calendar.set(GregorianCalendar.MINUTE, 59);
                calendar.set(GregorianCalendar.SECOND, 59);
                calendar.set(GregorianCalendar.MILLISECOND, 999);
                calendar.add(GregorianCalendar.MONTH, 1); // default: expire one month later
                myExpiration.setValue(calendar.getTime());
            }
        }
    }

    protected void writeToConfig() {
        myUser.setBandwidthLimit(myBandwidthLimit.getIntegerValue(-1));
        myUser.setBytesQuota(myDownloadLimitSize.getLongValue(-1));
        myUser.setChangeEmail(myPermChangeEmail.booleanValue());
        myUser.setChangePassword(myPermChangePassword.booleanValue());
        myUser.setCreatePlaylists(myPermEditPlaylists.booleanValue());
        //myUser.setDownBytes();
        myUser.setDownload(myPermDownload.booleanValue());
        myUser.setYahooPlayer(myPermYahooPlayer.booleanValue());
        myUser.setEditLastFmAccount(myPermEditLastFm.booleanValue());
        myUser.setEditTags(myPermEditTags.booleanValue());
        myUser.setEditWebSettings(myPermEditSettings.booleanValue());
        myUser.setEmail((String) myEmail.getValue());
        myUser.setExternalSites(myPermExternalLinks.booleanValue());
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
        myUser.setSharedUser(mySharedUser.booleanValue());
        myUser.setSearchFuzziness(mySearchFuzziness.getIntegerValue(-1));
        myUser.setSessionTimeout(mySessionTimeout.getIntegerValue(10));
        myUser.setSpecialPlaylists(myPermStandardPlaylist.booleanValue());
        myUser.setTranscoder(myPermTranscoder.booleanValue());
        myUser.setUpload(myPermUpload.booleanValue());
        myUser.setUrlEncryption(myEncryptUrls.booleanValue());
        Set<String> restricted = new HashSet<String>();
        Set<String> excluded = new HashSet<String>();
        for (Object itemId : myPlaylistsRestrictions.getItemIds()) {
            Playlist playlist = (Playlist) itemId;
            if ((Boolean) getTableCellPropertyValue(myPlaylistsRestrictions, playlist, "restricted")) {
                restricted.add(playlist.getId());
            }
            if ((Boolean) getTableCellPropertyValue(myPlaylistsRestrictions, playlist, "excluded")) {
                excluded.add(playlist.getId());
            }
        }
        myUser.setRestrictedPlaylistIds(restricted);
        myUser.setExcludedPlaylistIds(excluded);
        myUser.clearForceTranscoders();
        for (Object transcoderName : myForceTranscoders.getItemIds()) {
            if ((Boolean) getTableCellPropertyValue(myForceTranscoders, transcoderName, "active")) {
                myUser.addForceTranscoder((String) transcoderName);
            }
        }
        if (myExpire.booleanValue()) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime((Date) myExpiration.getValue());
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 23);
            calendar.set(GregorianCalendar.MINUTE, 59);
            calendar.set(GregorianCalendar.SECOND, 59);
            calendar.set(GregorianCalendar.MILLISECOND, 999);
            myUser.setExpiration(calendar.getTime().getTime());
        } else {
            myUser.setExpiration(0);
        }
        if (myNewUser) {
            MyTunesRss.CONFIG.addUser(myUser);
        }
        MyTunesRss.CONFIG.save();
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

    public void valueChange(Property.ValueChangeEvent event) {
        if (((EventObject) event).getSource() == myExpire) {
            myExpiration.setVisible((Boolean) event.getProperty().getValue());
        }
    }

    public class UniqueUsernameValidator extends AbstractStringValidator {

        public UniqueUsernameValidator() {
            super(getBundleString("editUserConfigPanel.error.usernameMustBeUnique"));
        }

        @Override
        protected boolean isValidString(String s) {
            for (User user : MyTunesRss.CONFIG.getUsers()) {
                if (user != myUser && StringUtils.equalsIgnoreCase(user.getName(), s)) {
                    return false;
                }
            }
            return true;
        }
    }
}