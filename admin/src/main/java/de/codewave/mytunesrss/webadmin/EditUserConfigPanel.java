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
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.EmailValidator;
import de.codewave.vaadin.validation.SameValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
    private CheckBox myPermCreatePublicPlaylists;
    private CheckBox myPermPhotos;
    private CheckBox myPermShare;
    private CheckBox myPermDownloadPhotoAlbum;
    private CheckBox myPermAudio;
    private CheckBox myPermVideo;
    private Table myPermissions;
    private TreeTable myPlaylistsRestrictions;
    private Table myPhotoAlbumRestrictions;
    private Table myDatasourceExclusions;
    private Table myForceTranscoders;
    private SmartTextField mySearchFuzziness;
    private Select myDownloadLimitType;
    private SmartTextField myDownloadLimitSize;
    private SmartTextField myBandwidthLimit;
    private SmartTextField myMaxFilesPerArchive;
    private SmartTextField mySessionTimeout;
    private CheckBox mySharedUser;
    private SmartTextField myLastFmUsername;
    private SmartTextField myLastFmPassword;
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
        super.attach();
        int rows = myUser.getParent() == null ? 8 : 5;
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
        myPermCreatePublicPlaylists = new CheckBox();
        myPermPhotos = new CheckBox();
        myPermShare = new CheckBox();
        myPermDownloadPhotoAlbum = new CheckBox();
        myPermAudio = new CheckBox();
        myPermVideo = new CheckBox();
        Panel panel = null;
        myPermissions = new Table();
        myPermissions.setCacheRate(50);
        myPermissions.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myPermissions.addContainerProperty("active", CheckBox.class, null, "", null, null);
        myPermissions.addContainerProperty("permission", String.class, null, getBundleString("editUserConfigPanel.permissions.name"), null, null);
        myPermissions.setColumnExpandRatio("permission", 1);
        myPermissions.setEditable(false);
        myPermissions.addItem(new Object[]{myPermAudio, getBundleString("editUserConfigPanel.permAudio")}, myPermAudio);
        myPermissions.addItem(new Object[]{myPermVideo, getBundleString("editUserConfigPanel.permMovies")}, myPermVideo);
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
        myPermissions.addItem(new Object[]{myPermCreatePublicPlaylists, getBundleString("editUserConfigPanel.permCreatePublicPlaylists")}, myPermCreatePublicPlaylists);
        myPermissions.addItem(new Object[]{myPermPhotos, getBundleString("editUserConfigPanel.permPhotos")}, myPermPhotos);
        myPermissions.addItem(new Object[]{myPermDownloadPhotoAlbum, getBundleString("editUserConfigPanel.permDownloadPhotoAlbum")}, myPermDownloadPhotoAlbum);
        myPermissions.addItem(new Object[]{myPermShare, getBundleString("editUserConfigPanel.permShare")}, myPermShare);
        myPermissions.setPageLength(Math.min(myPermissions.size(), 10));
        panel = new Panel(getBundleString("editUserConfigPanel.caption.permissions"));
        panel.addComponent(myPermissions);
        if (myUser.getParent() == null) {
            addComponent(panel);
        }
        myDatasourceExclusions = new Table();
        myDatasourceExclusions.setCacheRate(50);
        myDatasourceExclusions.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myDatasourceExclusions.addContainerProperty("excluded", CheckBox.class, null, getBundleString("editUserConfigPanel.datasource.excluded"), null, null);
        myDatasourceExclusions.addContainerProperty("name", String.class, null, getBundleString("editUserConfigPanel.datasource.name"), null, null);
        myDatasourceExclusions.setColumnExpandRatio("name", 1);
        myDatasourceExclusions.setEditable(false);
        myDatasourceExclusions.setSortContainerPropertyId("name");
        panel = new Panel(getBundleString("editUserConfigPanel.caption.excludedDatasources"));
        panel.addComponent(myDatasourceExclusions);
        if (myUser.getParent() == null) {
            addComponent(panel);
        }
        myPlaylistsRestrictions = new TreeTable();
        myPlaylistsRestrictions.setCacheRate(50);
        myPlaylistsRestrictions.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myPlaylistsRestrictions.addContainerProperty("restricted", CheckBox.class, null, getBundleString("editUserConfigPanel.playlists.restricted"), null, null);
        myPlaylistsRestrictions.addContainerProperty("excluded", CheckBox.class, null, getBundleString("editUserConfigPanel.playlists.excluded"), null, null);
        myPlaylistsRestrictions.addContainerProperty("hidden", CheckBox.class, null, getBundleString("editUserConfigPanel.playlists.hidden"), null, null);
        myPlaylistsRestrictions.addContainerProperty("name", String.class, null, getBundleString("editUserConfigPanel.playlists.name"), null, null);
        myPlaylistsRestrictions.setColumnExpandRatio("name", 1);
        myPlaylistsRestrictions.setHierarchyColumn("name");
        myPlaylistsRestrictions.setEditable(false);
        myPlaylistsRestrictions.setSortContainerPropertyId("name");
        panel = new Panel(getBundleString("editUserConfigPanel.caption.restrictedPlaylists"));
        panel.addComponent(myPlaylistsRestrictions);
        if (myUser.getParent() == null) {
            addComponent(panel);
        }
        myPhotoAlbumRestrictions = new Table();
        myPhotoAlbumRestrictions.setCacheRate(50);
        myPhotoAlbumRestrictions.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myPhotoAlbumRestrictions.addContainerProperty("restricted", CheckBox.class, null, getBundleString("editUserConfigPanel.photoalbum.restricted"), null, null);
        myPhotoAlbumRestrictions.addContainerProperty("excluded", CheckBox.class, null, getBundleString("editUserConfigPanel.photoalbum.excluded"), null, null);
        myPhotoAlbumRestrictions.addContainerProperty("firstDate", AlbumDate.class, null, getBundleString("editUserConfigPanel.photoalbum.firstDate"), null, null);
        myPhotoAlbumRestrictions.addContainerProperty("lastDate", AlbumDate.class, null, getBundleString("editUserConfigPanel.photoalbum.lastDate"), null, null);
        myPhotoAlbumRestrictions.addContainerProperty("name", String.class, null, getBundleString("editUserConfigPanel.photoalbum.name"), null, null);
        myPhotoAlbumRestrictions.setColumnExpandRatio("name", 1);
        myPhotoAlbumRestrictions.setEditable(false);
        myPhotoAlbumRestrictions.setSortContainerPropertyId("firstDate");
        panel = new Panel(getBundleString("editUserConfigPanel.caption.restrictedPhotoAlbums"));
        panel.addComponent(myPhotoAlbumRestrictions);
        if (myUser.getParent() == null) {
            addComponent(panel);
        }
        myForceTranscoders = new Table();
        myForceTranscoders.setCacheRate(50);
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
        myBandwidthLimit = getComponentFactory().createTextField("editUserConfigPanel.bandwidthLimit", getApplication().getValidatorFactory().createMinMaxValidator(0, Integer.MAX_VALUE));
        myMaxFilesPerArchive = getComponentFactory().createTextField("editUserConfigPanel.maxFilesPerArchive", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        mySessionTimeout = getComponentFactory().createTextField("editUserConfigPanel.sessionTimeout", getApplication().getValidatorFactory().createMinMaxValidator(1, Integer.MAX_VALUE));
        mySharedUser = getComponentFactory().createCheckBox("editUserConfigPanel.sharedUser");
        myLastFmUsername = getComponentFactory().createTextField("editUserConfigPanel.lastFmUsername");
        myLastFmPassword = getComponentFactory().createPasswordTextField("editUserConfigPanel.lastFmPassword");
        myOptionsForm = getComponentFactory().createForm(null, true);
        if (myUser.getParent() == null) {
            myOptionsForm.addField("searchFuzziness", mySearchFuzziness);
            myOptionsForm.addField("downloadLimitType", myDownloadLimitType);
            myOptionsForm.addField("downloadLimitSize", myDownloadLimitSize);
            myOptionsForm.addField("bandwidthLimit", myBandwidthLimit);
            myOptionsForm.addField("maxFilesPerArchive", myMaxFilesPerArchive);
            myOptionsForm.addField("sessionTimeout", mySessionTimeout);
            myOptionsForm.addField("sharedUser", mySharedUser);
        }
        if (!myUser.isGroup()) {
            myOptionsForm.addField("lastFmUsername", myLastFmUsername);
            myOptionsForm.addField("lastFmPassword", myLastFmPassword);
        }
        addComponent(getComponentFactory().surroundWithPanel(myOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("editUserConfigPanel.caption.options")));

        addDefaultComponents(0, rows - 1, 0, rows - 1, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        if (myUser != null) {
            myDownloadLimitSize.setValue(myUser.getBytesQuota() / (1024 * 1024), 1, Integer.MAX_VALUE, "");
            myBandwidthLimit.setValue(myUser.getBandwidthLimit(), 1, Integer.MAX_VALUE, "");
            myPermChangeEmail.setValue(myUser.isChangeEmail());
            myPermChangePassword.setValue(myUser.isChangePassword());
            myPermEditPlaylists.setValue(myUser.isCreatePlaylists());
            myPermCreatePublicPlaylists.setValue(myUser.isCreatePublicPlaylists());
            myPermPhotos.setValue(myUser.isPhotos());
            myPermShare.setValue(myUser.isShare());
            myPermDownloadPhotoAlbum.setValue(myUser.isDownloadPhotoAlbum());
            myPermAudio.setValue(myUser.isAudio());
            myPermVideo.setValue(myUser.isVideo());
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
            if (!myUser.isEmptyPassword()) {
                myPassword.setValue(myUser.getPasswordHash());
                myRetypePassword.setValue(myUser.getPasswordHash());
            } else {
                myPassword.setValue("");
                myRetypePassword.setValue("");
            }
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
            myPlaylistsRestrictions.removeAllItems();
            List<Playlist> playlists = Collections.emptyList();
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            try {
                playlists = session.executeQuery(new FindPlaylistQuery(Arrays.asList(PlaylistType.ITunes, PlaylistType.ITunesFolder, PlaylistType.M3uFile, PlaylistType.MyTunes, PlaylistType.MyTunesSmart), null, null, true)).getResults();
                for (Playlist playlist : playlists) {
                    CheckBox restricted = new CheckBox();
                    restricted.setValue(myUser.getRestrictedPlaylistIds().contains(playlist.getId()));
                    CheckBox excluded = new CheckBox();
                    excluded.setValue(myUser.getExcludedPlaylistIds().contains(playlist.getId()));
                    CheckBox hidden = new CheckBox();
                    hidden.setValue(myUser.getHiddenPlaylistIds().contains(playlist.getId()) || playlist.isHidden() || (playlist.isUserPrivate() && !myUser.getName().equals(playlist.getUserOwner())));
                    hidden.setEnabled(!playlist.isHidden() && !playlist.isUserPrivate());
                    myPlaylistsRestrictions.addItem(new Object[]{restricted, excluded, hidden, playlist.getName()}, playlist);
                }
                getApplication().createPlaylistTreeTableHierarchy(myPlaylistsRestrictions, playlists);
                myPlaylistsRestrictions.sort();
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not fetch playlists from database.", e);
                }
                MyTunesRss.UNHANDLED_EXCEPTION.set(true);
            } finally {
                session.rollback();
            }
            myPlaylistsRestrictions.setPageLength(Math.min(MyTunesRssUtils.getRootPlaylistCount(playlists), 10));
            myPhotoAlbumRestrictions.removeAllItems();
            List<PhotoAlbum> photoAlbums = Collections.emptyList();
            try {
                photoAlbums = session.executeQuery(new GetPhotoAlbumsQuery()).getResults();
                for (PhotoAlbum photoAlbum : photoAlbums) {
                    CheckBox restricted = new CheckBox();
                    restricted.setValue(myUser.getRestrictedPhotoAlbumIds().contains(photoAlbum.getId()));
                    CheckBox excluded = new CheckBox();
                    excluded.setValue(myUser.getExcludedPhotoAlbumIds().contains(photoAlbum.getId()));
                    AlbumDate firstDate = photoAlbum.getFirstDate() > 0 ? new AlbumDate(photoAlbum.getFirstDate()) : new AlbumDate(photoAlbum.getLastDate());
                    AlbumDate lastDate = new AlbumDate(photoAlbum.getLastDate());
                    myPhotoAlbumRestrictions.addItem(new Object[]{restricted, excluded, firstDate, lastDate, photoAlbum.getName()}, photoAlbum);
                }
                myPhotoAlbumRestrictions.sort();
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not fetch photo albums from database.", e);
                }
                MyTunesRss.UNHANDLED_EXCEPTION.set(true);
            } finally {
                session.rollback();
            }
            myPhotoAlbumRestrictions.setPageLength(Math.min(photoAlbums.size(), 10));
            myDatasourceExclusions.removeAllItems();
            List<DatasourceConfig> datasourceConfigs = MyTunesRss.CONFIG.getDatasources();
            for (DatasourceConfig datasourceConfig : datasourceConfigs) {
                CheckBox excluded = new CheckBox();
                excluded.setValue(myUser.getExcludedDataSourceIds().contains(datasourceConfig.getId()));
                myDatasourceExclusions.addItem(new Object[]{excluded, datasourceConfig.getDefinition()}, datasourceConfig.getId());
            }
            myDatasourceExclusions.sort();
            myDatasourceExclusions.setPageLength(Math.min(datasourceConfigs.size(), 10));
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

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myIdentificationForm, myOptionsForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    protected void writeToConfig() {
        long bytesQuota = myDownloadLimitSize.getLongValue(-1);
        myUser.setBytesQuota(bytesQuota > 0 ? bytesQuota * (1024 * 1024) : bytesQuota);
        myUser.setBandwidthLimit(myBandwidthLimit.getIntegerValue(0));
        myUser.setChangeEmail(myPermChangeEmail.booleanValue());
        myUser.setChangePassword(myPermChangePassword.booleanValue());
        myUser.setCreatePlaylists(myPermEditPlaylists.booleanValue());
        myUser.setCreatePublicPlaylists(myPermCreatePublicPlaylists.booleanValue());
        myUser.setPhotos(myPermPhotos.booleanValue());
        myUser.setShare(myPermShare.booleanValue());
        myUser.setDownloadPhotoAlbum(myPermDownloadPhotoAlbum.booleanValue());
        myUser.setAudio(myPermAudio.booleanValue());
        myUser.setVideo(myPermVideo.booleanValue());
        myUser.setDownload(myPermDownload.booleanValue());
        myUser.setYahooPlayer(myPermYahooPlayer.booleanValue());
        myUser.setEditLastFmAccount(myPermEditLastFm.booleanValue());
        myUser.setEditTags(myPermEditTags.booleanValue());
        myUser.setEditWebSettings(myPermEditSettings.booleanValue());
        myUser.setEmail((String) myEmail.getValue());
        myUser.setExternalSites(myPermExternalLinks.booleanValue());
        myUser.setLastFmPasswordHash(myLastFmPassword.getStringHashValue(MyTunesRss.MD5_DIGEST.get()));
        myUser.setLastFmUsername((String) myLastFmUsername.getValue());
        myUser.setMaximumZipEntries(myMaxFilesPerArchive.getIntegerValue(-1));
        final String oldUsername = myUser.getName();
        final String newUsername = (String) myUsername.getValue();
        myUser.setName(newUsername);
        if (StringUtils.isBlank(myPassword.getStringValue("")) && !myUser.isEmptyPassword()) {
            myUser.setPasswordHash(MyTunesRss.SHA1_DIGEST.get().digest(MiscUtils.getUtf8Bytes(UUID.randomUUID().toString())));
            myUser.setEmptyPassword(true);
        } else if (StringUtils.isNotBlank(myPassword.getStringValue(""))) {
            myUser.setPasswordHash(myPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST.get()));
            myUser.setEmptyPassword(false);
        }
        myUser.setPlayer(myPermFlashPlayer.booleanValue());
        myUser.setPlaylist(myPermPlaylist.booleanValue());
        myUser.setQuotaType((User.QuotaType) myDownloadLimitType.getValue());
        myUser.setRemoteControl(myPermRemote.booleanValue());
        myUser.setRss(myPermRss.booleanValue());
        myUser.setSharedUser(mySharedUser.booleanValue());
        myUser.setSearchFuzziness(mySearchFuzziness.getIntegerValue(-1));
        myUser.setSessionTimeout(mySessionTimeout.getIntegerValue(10));
        myUser.setSpecialPlaylists(myPermStandardPlaylist.booleanValue());
        myUser.setTranscoder(myPermTranscoder.booleanValue());
        myUser.setUpload(myPermUpload.booleanValue());
        Set<String> restricted = new HashSet<String>();
        Set<String> excluded = new HashSet<String>();
        Set<String> hidden = new HashSet<String>();
        for (Object itemId : myPlaylistsRestrictions.getItemIds()) {
            Playlist playlist = (Playlist) itemId;
            if ((Boolean) getTableCellPropertyValue(myPlaylistsRestrictions, playlist, "restricted")) {
                restricted.add(playlist.getId());
            }
            if ((Boolean) getTableCellPropertyValue(myPlaylistsRestrictions, playlist, "excluded")) {
                excluded.add(playlist.getId());
            }
            CheckBox hiddenCheckbox = (CheckBox) getTableCellItemValue(myPlaylistsRestrictions, playlist, "hidden");
            if (hiddenCheckbox.isEnabled() && (Boolean)hiddenCheckbox.getValue()) {
                hidden.add(playlist.getId());
            }
        }
        myUser.setRestrictedPlaylistIds(restricted);
        myUser.setExcludedPlaylistIds(excluded);
        myUser.setHiddenPlaylistIds(hidden);
        restricted = new HashSet<String>();
        excluded = new HashSet<String>();
        for (Object itemId : myPhotoAlbumRestrictions.getItemIds()) {
            PhotoAlbum photoAlbum = (PhotoAlbum) itemId;
            if ((Boolean) getTableCellPropertyValue(myPhotoAlbumRestrictions, photoAlbum, "restricted")) {
                restricted.add(photoAlbum.getId());
            }
            if ((Boolean) getTableCellPropertyValue(myPhotoAlbumRestrictions, photoAlbum, "excluded")) {
                excluded.add(photoAlbum.getId());
            }
        }
        myUser.setRestrictedPhotoAlbumIds(restricted);
        myUser.setExcludedPhotoAlbumIds(excluded);
        excluded = new HashSet<String>();
        for (Object itemId : myDatasourceExclusions.getItemIds()) {
            String id = (String) itemId;
            if ((Boolean) getTableCellPropertyValue(myDatasourceExclusions, id, "excluded")) {
                excluded.add(id);
            }
        }
        myUser.setExcludedDataSourceIds(excluded);
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
        if (!myNewUser && !StringUtils.equals(oldUsername, newUsername)) {
            try {
                MyTunesRss.STORE.executeStatement(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        SmartStatement renameStatement = MyTunesRssUtils.createStatement(connection, "renamePlaylistOwner");
                        renameStatement.setString("oldUsername", oldUsername);
                        renameStatement.setString("newUsername", newUsername);
                        renameStatement.execute();
                    }
                });
            } catch (SQLException e) {
                LOGGER.error("Could not rename owner of playlists. Orphaned playlists will be removed by maintenance job later.", e);
            }
        }
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

    public class AlbumDate implements Comparable<AlbumDate> {
        private long myCompareDate;
        private String myDisplayDate = "";

        public AlbumDate(long timestamp) {
            myCompareDate = timestamp;
            if (timestamp > 0) {
                myDisplayDate = new SimpleDateFormat(getBundleString("common.dateFormat")).format(new Date(timestamp));
            }
        }

        @Override
        public String toString() {
            return myDisplayDate;
        }

        public int compareTo(AlbumDate other) {
            return (int)Math.signum(myCompareDate - other.myCompareDate);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AlbumDate)) return false;

            AlbumDate albumDate = (AlbumDate) o;

            if (myCompareDate != albumDate.myCompareDate) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (myCompareDate ^ (myCompareDate >>> 32));
        }
    }
}
