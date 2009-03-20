/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * de.codewave.mytunesrss.jmx.EditUserConfig
 */
public class EditUserConfig extends MyTunesRssMBean implements EditUserConfigMBean {
    private static final Logger LOG = LoggerFactory.getLogger(EditUserConfig.class);

    private String myUsername;

    EditUserConfig(String username) throws NotCompliantMBeanException {
        super(EditUserConfigMBean.class);
        myUsername = username;
    }

    public void activate() {
        MyTunesRss.CONFIG.getUser(myUsername).setActive(true);
        onChange();
    }

    public void deactivate() {
        MyTunesRss.CONFIG.getUser(myUsername).setActive(false);
        onChange();
    }

    public boolean isActive() {
        return MyTunesRss.CONFIG.getUser(myUsername).isActive();
    }

    public String getName() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUser(myUsername).getName());
    }

    public void setName(String name)
            throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException, NotCompliantMBeanException,
            InstanceAlreadyExistsException {
        if (StringUtils.isNotBlank(name)) {
            MyTunesRssJmxUtils.unregisterUsers();
            MyTunesRss.CONFIG.getUser(myUsername).setName(name);
            MyTunesRssJmxUtils.registerUsers();
            onChange();
        }
    }

    public void setPassword(String password) {
        if (StringUtils.isNotBlank(password)) {
            try {
                MyTunesRss.CONFIG.getUser(myUsername).setPasswordHash(MyTunesRss.SHA1_DIGEST.digest(StringUtils.trim(password).getBytes("UTF-8")));
                onChange();
            } catch (UnsupportedEncodingException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create password hash.", e);
                }
            }
        }
    }

    public boolean isPermissionChangePassword() {
        return MyTunesRss.CONFIG.getUser(myUsername).isChangePassword();
    }

    public void setPermissionChangePassword(boolean permissionChangePassword) {
        MyTunesRss.CONFIG.getUser(myUsername).setChangePassword(permissionChangePassword);
        onChange();
    }

    public boolean isPermissionEditLastFmAccount() {
        return MyTunesRss.CONFIG.getUser(myUsername).isEditLastFmAccount();
    }

    public void setPermissionEditLastFmAccount(boolean permissionEditLastFmAccount) {
        MyTunesRss.CONFIG.getUser(myUsername).setEditLastFmAccount(permissionEditLastFmAccount);
        onChange();
    }

    public boolean isPermissionDownload() {
        return MyTunesRss.CONFIG.getUser(myUsername).isDownload();
    }

    public void setPermissionDownload(boolean permissionDownload) {
        MyTunesRss.CONFIG.getUser(myUsername).setDownload(permissionDownload);
        onChange();
    }

    public boolean isPermissionPlaylist() {
        return MyTunesRss.CONFIG.getUser(myUsername).isPlaylist();
    }

    public void setPermissionPlaylist(boolean permissionPlaylist) {
        MyTunesRss.CONFIG.getUser(myUsername).setPlaylist(permissionPlaylist);
        onChange();
    }

    public boolean isPermissionPlayer() {
        return MyTunesRss.CONFIG.getUser(myUsername).isPlayer();
    }

    public void setPermissionPlayer(boolean permissionPlayer) {
        MyTunesRss.CONFIG.getUser(myUsername).setPlayer(permissionPlayer);
        onChange();
    }

    public boolean isPermissionRss() {
        return MyTunesRss.CONFIG.getUser(myUsername).isRss();
    }

    public void setPermissionRss(boolean permissionRss) {
        MyTunesRss.CONFIG.getUser(myUsername).setRss(permissionRss);
        onChange();
    }

    public boolean isPermissionUpload() {
        return MyTunesRss.CONFIG.getUser(myUsername).isUpload();
    }

    public void setPermissionUpload(boolean permissionUpload) {
        MyTunesRss.CONFIG.getUser(myUsername).setUpload(permissionUpload);
        onChange();
    }

    public boolean isPermissionSpecialPlaylists() {
        return MyTunesRss.CONFIG.getUser(myUsername).isSpecialPlaylists();
    }

    public void setPermissionSpecialPlaylists(boolean permissionSpecialPlaylists) {
        MyTunesRss.CONFIG.getUser(myUsername).setSpecialPlaylists(permissionSpecialPlaylists);
        onChange();
    }

    public boolean isPermissionCreatePlaylists() {
        return MyTunesRss.CONFIG.getUser(myUsername).isCreatePlaylists();
    }

    public void setPermissionCreatePlaylists(boolean permissionCreatePlaylists) {
        MyTunesRss.CONFIG.getUser(myUsername).setCreatePlaylists(permissionCreatePlaylists);
        onChange();
    }

    public boolean isPermissionEditWebSettings() {
        return MyTunesRss.CONFIG.getUser(myUsername).isEditWebSettings();
    }

    public void setPermissionEditWebSettings(boolean permissionEditWebSettings) {
        MyTunesRss.CONFIG.getUser(myUsername).setEditWebSettings(permissionEditWebSettings);
        onChange();
    }

    public boolean isPermissionTranscoding() {
        return MyTunesRss.CONFIG.getUser(myUsername).isTranscoder();
    }

    public void setPermissionTranscoding(boolean permissionTranscoding) {
        MyTunesRss.CONFIG.getUser(myUsername).setTranscoder(permissionTranscoding);
        onChange();
    }

    public void setQuotaTypeToDay() {
        MyTunesRss.CONFIG.getUser(myUsername).setQuotaType(User.QuotaType.Day);
        onChange();
    }

    public void setQuotaTypeToMonth() {
        MyTunesRss.CONFIG.getUser(myUsername).setQuotaType(User.QuotaType.Month);
        onChange();
    }

    public void disableQuota() {
        MyTunesRss.CONFIG.getUser(myUsername).setQuotaType(User.QuotaType.None);
        onChange();
    }

    public void setQuotaTypeToWeek() {
        MyTunesRss.CONFIG.getUser(myUsername).setQuotaType(User.QuotaType.Week);
        onChange();
    }

    public int getMaximumFilesInArchive() {
        return MyTunesRss.CONFIG.getUser(myUsername).getMaximumZipEntries();
    }

    public void setQuotaMegabytes(long megabyte) {
        MyTunesRss.CONFIG.getUser(myUsername).setBytesQuota(megabyte * 1024 * 1024);
        onChange();
    }

    public void setMaximumFilesInArchive(int maxFiles) {
        MyTunesRss.CONFIG.getUser(myUsername).setMaximumZipEntries(maxFiles);
        onChange();
    }

    public long getHistoryDownloadBytes() {
        return MyTunesRss.CONFIG.getUser(myUsername).getDownBytes();
    }

    public String getLastHistoryResetDate() {
        return new SimpleDateFormat(MyTunesRssUtils.getBundleString("common.dateFormat")).format(new Date(MyTunesRss.CONFIG
                .getUser(myUsername).getResetTime()));
    }

    public void resetDownloadHistory() {
        MyTunesRss.CONFIG.getUser(myUsername).setDownBytes(0);
        MyTunesRss.CONFIG.getUser(myUsername).setResetTime(System.currentTimeMillis());
        onChange();
    }

    public String getQuotaInfo() {
        if (MyTunesRss.CONFIG.getUser(myUsername).getQuotaType() != User.QuotaType.None) {
            return MessageFormat.format(MyTunesRssUtils.getBundleString("jmx.quota"),
                                        MyTunesRssUtils.getMemorySizeForDisplay(MyTunesRss.CONFIG.getUser(myUsername).getBytesQuota()),
                                        MyTunesRss.CONFIG.getUser(myUsername).getQuotaType().toString(),
                                        MyTunesRssUtils.getMemorySizeForDisplay(MyTunesRss.CONFIG.getUser(myUsername).getQuotaDownBytes()),
                                        MyTunesRssUtils.getMemorySizeForDisplay(MyTunesRss.CONFIG.getUser(myUsername).getQuotaRemaining()));
        }
        return MyTunesRss.CONFIG.getUser(myUsername).getQuotaType().toString();
    }

    public String delete() throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException, NotCompliantMBeanException,
            InstanceAlreadyExistsException {
        MyTunesRssJmxUtils.unregisterUsers();
        MyTunesRss.CONFIG.removeUser(myUsername);
        MyTunesRssJmxUtils.registerUsers();
        onChange();
        return MyTunesRssUtils.getBundleString("jmx.userDeleted");
    }

    public int getSessionTimeout() {
        return MyTunesRss.CONFIG.getUser(myUsername).getSessionTimeout();
    }

    public void setSessionTimeout(int minutes) {
        MyTunesRss.CONFIG.getUser(myUsername).setSessionTimeout(minutes);
        onChange();
    }

    public String[] getPlaylists() {
        String[] names = null;
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            DataStoreQuery.QueryResult<Playlist> playlists = session.executeQuery(new FindPlaylistQuery(null, null, null, true));
            names = new String[playlists.getResultSize()];
            int i = 0;
            for (Playlist playlist = playlists.nextResult(); playlist != null; playlist = playlists.nextResult()) {
                names[i++] = playlist.getName();
            }
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not query playlists.", e);
            }
        } finally {
            session.commit();
        }
        return names;
    }

    public String getRestrictionPlaylist() {
        String playlistId = MyTunesRss.CONFIG.getUser(myUsername).getPlaylistId();
        if (StringUtils.isNotEmpty(playlistId)) {
            DataStoreQuery.QueryResult<Playlist> playlists = null;
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            try {
                playlists = session.executeQuery(new FindPlaylistQuery(null, playlistId, null, true));
                return playlists.getResultSize() != 0 ? playlists.nextResult().getName() : MyTunesRssUtils.getBundleString("editUser.noPlaylist");
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not query playlists.", e);
                }
            } finally {
                session.commit();
            }
        }
        return MyTunesRssUtils.getBundleString("editUser.noPlaylist");
    }

    public void setRestrictionPlaylist(String playlistName) {
        DataStoreQuery.QueryResult<Playlist> playlists = null;
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            playlists = session.executeQuery(new FindPlaylistQuery(null, null, null, true));
            if (playlists.getResultSize() > 0) {
                User user = MyTunesRss.CONFIG.getUser(myUsername);
                user.setPlaylistId(null);
                for (Playlist playlist = playlists.nextResult(); playlist != null; playlist = playlists.nextResult()) {
                    if (StringUtils.containsIgnoreCase(playlist.getName(), playlistName)) {
                        user.setPlaylistId(playlist.getId());
                    }
                }
            }
            onChange();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not query playlists.", e);
            }
        } finally {
            session.commit();
        }
    }

    public boolean isSaveWebSettings() {
        return MyTunesRss.CONFIG.getUser(myUsername).isSaveWebSettings();
    }

    public void setSaveWebSettings(boolean saveWebSettings) {
        MyTunesRss.CONFIG.getUser(myUsername).setSaveWebSettings(saveWebSettings);
        onChange();
    }

    public String getLastFmUsername() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUser(myUsername).getLastFmUsername());
    }

    public void setLastFmUsername(String username) {
        MyTunesRss.CONFIG.getUser(myUsername).setLastFmUsername(StringUtils.trimToNull(username));
        onChange();
    }

    public void setLastFmPassword(String password) {
        if (StringUtils.isNotBlank(password)) {
            try {
                MyTunesRss.CONFIG.getUser(myUsername).setLastFmPasswordHash(MyTunesRss.MD5_DIGEST.digest(StringUtils
                        .trim(password).getBytes("UTF-8")));
                onChange();
            } catch (UnsupportedEncodingException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create password hash.", e);
                }
            }
        }
    }

    public boolean isUrlEncryption() {
        return MyTunesRss.CONFIG.getUser(myUsername).isUrlEncryption();
    }

    public void setUrlEncryption(boolean urlEncryption) {
        MyTunesRss.CONFIG.getUser(myUsername).setUrlEncryption(urlEncryption);
        onChange();
    }

    public String getEmail() {
        return MyTunesRss.CONFIG.getUser(myUsername).getEmail();
    }

    public void setEmail(String email) {
        MyTunesRss.CONFIG.getUser(myUsername).setEmail(email);
        onChange();
    }

    public boolean isPermissionChangeEmail() {
        return MyTunesRss.CONFIG.getUser(myUsername).isChangeEmail();
    }

    public void setPermissionChangeEmail(boolean permissionChangeEmail) {
        MyTunesRss.CONFIG.getUser(myUsername).setChangeEmail(permissionChangeEmail);
        onChange();
    }

    public String removeWebSettingsFromUserProfile() {
        MyTunesRss.CONFIG.getUser(myUsername).setWebSettings(null);
        onChange();
        return MyTunesRssUtils.getBundleString("info.userSettingsRemovedFromProfile");
    }

    public boolean isPermissionRemoteControl() {
        return MyTunesRss.CONFIG.getUser(myUsername).isRemoteControl();
    }

    public void setPermissionRemoteControl(boolean permissionRemoteControl) {
        MyTunesRss.CONFIG.getUser(myUsername).setRemoteControl(permissionRemoteControl);
        onChange();
    }
}