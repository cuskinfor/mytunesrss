/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import javax.management.*;
import java.io.UnsupportedEncodingException;

/**
 * de.codewave.mytunesrss.jmx.EditUserConfigMBean
 */
public interface EditUserConfigMBean {
    void activate();

    void deactivate();

    boolean isActive();

    String getName();

    void setName(String name) throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException, NotCompliantMBeanException,
            InstanceAlreadyExistsException;

    void setPassword(String password);

    boolean isPermissionChangePassword();

    void setPermissionChangePassword(boolean permissionChangePassword);

    boolean isPermissionEditLastFmAccount();

    void setPermissionEditLastFmAccount(boolean permissionEditLastFmAccount);

    boolean isPermissionDownload();

    void setPermissionDownload(boolean permissionDownload);

    boolean isPermissionPlaylist();

    void setPermissionPlaylist(boolean permissionPlaylist);

    boolean isPermissionPlayer();

    void setPermissionPlayer(boolean permissionPlayer);

    boolean isPermissionRss();

    void setPermissionRss(boolean permissionRss);

    boolean isPermissionUpload();

    void setPermissionUpload(boolean permissionUpload);

    boolean isPermissionSpecialPlaylists();

    void setPermissionSpecialPlaylists(boolean permissionSpecialPlaylists);

    boolean isPermissionCreatePlaylists();

    void setPermissionCreatePlaylists(boolean permissionCreatePlaylists);

    boolean isPermissionEditWebSettings();

    void setPermissionEditWebSettings(boolean permissionEditWebSettings);

    void disableQuota();

    void setQuotaTypeToDay();

    void setQuotaTypeToWeek();

    void setQuotaTypeToMonth();

    void setQuotaMegabytes(long megabyte);

    void setMaximumFilesInArchive(int maxFiles);

    int getMaximumFilesInArchive();

    void resetDownloadHistory();

    String getLastHistoryResetDate();

    long getHistoryDownloadBytes();

    String getQuotaInfo();

    String delete() throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException, NotCompliantMBeanException,
            InstanceAlreadyExistsException;

    int getSessionTimeout();

    void setSessionTimeout(int minutes);

    boolean isPermissionTranscoding();

    void setPermissionTranscoding(boolean permissionTranscoding);

    String[] getPlaylists();

    String[] getRestrictionPlaylists();

    void addRestrictionPlaylist(String playlistName);

    void removeRestrictionPlaylist(String playlistName);

    boolean isSaveWebSettings();

    void setSaveWebSettings(boolean saveWebSettings);

    String getLastFmUsername();

    void setLastFmUsername(String username);

    void setLastFmPassword(String password);

    boolean isUrlEncryption();

    void setUrlEncryption(boolean urlEncryption);

    String getEmail();

    void setEmail(String email);

    boolean isPermissionChangeEmail();

    void setPermissionChangeEmail(boolean permissionChangeEmail);

    String removeWebSettingsFromUserProfile();

    boolean isPermissionRemoteControl();

    void setPermissionRemoteControl(boolean permissionRemoteControl);

    String getParentUser();

    String setParentUser(String username);

    void removeParentUser();

    boolean isPermissionExternalSites();

    void setPermissionExternalSites(boolean permissionExternalSites);

    int getSearchFuzziness();

    void setSearchFuzziness(int fuziness);

    boolean isPermissionEditTags();

    void setPermissionEditTags(boolean permissionEditTags);

    String[] getForceTranscoders();

    void addForceTranscoder(String name);

    void removeForceTranscoder(String name);

    void clearForceTranscoders();
}