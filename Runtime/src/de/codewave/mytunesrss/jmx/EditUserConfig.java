/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.management.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.jmx.EditUserConfig
 */
public class EditUserConfig extends MyTunesRssMBean implements EditUserConfigMBean {
    private static final Log LOG = LogFactory.getLog(EditUserConfig.class);

    private String myUsername;

    EditUserConfig(String username) throws NotCompliantMBeanException {
        super(EditUserConfigMBean.class);
        myUsername = username;
    }

    public void activate() {
        MyTunesRss.CONFIG.getUser(myUsername).setActive(true);
    }

    public void deactivate() {
        MyTunesRss.CONFIG.getUser(myUsername).setActive(false);
    }

    public boolean isActive() {
        return MyTunesRss.CONFIG.getUser(myUsername).isActive();
    }

    public String getName() {
        return MyTunesRss.CONFIG.getUser(myUsername).getName();
    }

    public void setName(String name) throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException,
            NotCompliantMBeanException, InstanceAlreadyExistsException {
        MyTunesRssJmxUtils.unregisterUsers();
        MyTunesRss.CONFIG.getUser(myUsername).setName(name);
        MyTunesRssJmxUtils.registerUsers();
    }

    public void setPassword(String password) {
        if (StringUtils.isNotEmpty(password)) {
            try {
                MyTunesRss.CONFIG.getUser(myUsername).setPasswordHash(MyTunesRss.MESSAGE_DIGEST.digest(StringUtils.trim(password).getBytes("UTF-8")));
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
    }

    public boolean isPermissionDownload() {
        return MyTunesRss.CONFIG.getUser(myUsername).isDownload();
    }

    public void setPermissionDownload(boolean permissionDownload) {
        MyTunesRss.CONFIG.getUser(myUsername).setDownload(permissionDownload);
    }

    public boolean isPermissionPlaylist() {
        return MyTunesRss.CONFIG.getUser(myUsername).isPlaylist();
    }

    public void setPermissionPlaylist(boolean permissionPlaylist) {
        MyTunesRss.CONFIG.getUser(myUsername).setPlaylist(permissionPlaylist);
    }

    public boolean isPermissionPlayer() {
        return MyTunesRss.CONFIG.getUser(myUsername).isPlayer();
    }

    public void setPermissionPlayer(boolean permissionPlayer) {
        MyTunesRss.CONFIG.getUser(myUsername).setPlayer(permissionPlayer);
    }

    public boolean isPermissionRss() {
        return MyTunesRss.CONFIG.getUser(myUsername).isRss();
    }

    public void setPermissionRss(boolean permissionRss) {
        MyTunesRss.CONFIG.getUser(myUsername).setRss(permissionRss);
    }

    public boolean isPermissionUpload() {
        return MyTunesRss.CONFIG.getUser(myUsername).isUpload();
    }

    public void setPermissionUpload(boolean permissionUpload) {
        MyTunesRss.CONFIG.getUser(myUsername).setUpload(permissionUpload);
    }

    public boolean isPermissionSpecialPlaylists() {
        return MyTunesRss.CONFIG.getUser(myUsername).isSpecialPlaylists();
    }

    public void setPermissionSpecialPlaylists(boolean permissionSpecialPlaylists) {
        MyTunesRss.CONFIG.getUser(myUsername).setSpecialPlaylists(permissionSpecialPlaylists);
    }

    public void setQuotaTypeToDay() {
        MyTunesRss.CONFIG.getUser(myUsername).setQuotaType(User.QuotaType.Day);
    }

    public void setQuotaTypeToMonth() {
        MyTunesRss.CONFIG.getUser(myUsername).setQuotaType(User.QuotaType.Month);
    }

    public void disableQuota() {
        MyTunesRss.CONFIG.getUser(myUsername).setQuotaType(User.QuotaType.None);
    }

    public void setQuotaTypeToWeek() {
        MyTunesRss.CONFIG.getUser(myUsername).setQuotaType(User.QuotaType.Week);
    }

    public int getMaximumFilesInArchive() {
        return MyTunesRss.CONFIG.getUser(myUsername).getMaximumZipEntries();
    }

    public void setQuotaMegabytes(long megabyte) {
        MyTunesRss.CONFIG.getUser(myUsername).setBytesQuota(megabyte * 1024 * 1024);
    }

    public void setMaximumFilesInArchive(int maxFiles) {
        MyTunesRss.CONFIG.getUser(myUsername).setMaximumZipEntries(maxFiles);
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
        return MyTunesRssUtils.getBundleString("jmx.userDeleted");
    }

    public int getSessionTimeout() {
        return MyTunesRss.CONFIG.getUser(myUsername).getSessionTimeout();
    }

    public void setSessionTimeout(int minutes) {
        MyTunesRss.CONFIG.getUser(myUsername).setSessionTimeout(minutes);
    }
}