/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.WebConfig;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * de.codewave.mytunesrss.command.SaveSettingsCommandHandler
 */
public class SaveSettingsCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isEditWebSettings()) {
            WebConfig webConfig = getWebConfig();
            if (transferAndValidate(webConfig)) {
                MyTunesRssWebUtils.saveWebConfig(getRequest(), getResponse(), getAuthUser(), webConfig);
                if (getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) != null) {
                    restartMyTunesRssCom();
                } else {
                    forward(MyTunesRssCommand.ShowPortal);
                }
            } else {
                forward(MyTunesRssResource.Settings);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

    private boolean transferAndValidate(WebConfig webConfig) {
        webConfig.setShowDownload(getBooleanRequestParameter("showDownload", false));
        webConfig.setShowRss(isParameterValue("feedType", "rss"));
        webConfig.setShowPlaylist(isParameterValue("feedType", "playlist"));
        webConfig.setPlaylistType(getRequestParameter("playlistType", "m3u"));
        webConfig.setShowPlayer(getBooleanRequestParameter("showPlayer", false));
        webConfig.setTheme(getRequest().getParameter("theme"));
        webConfig.setRandomSource(getRequestParameter("randomSource", ""));
        webConfig.setFlashplayer(StringUtils.trimToNull(getRequestParameter("flashplayer", null)));
        webConfig.setYahooMediaPlayer(getBooleanRequestParameter("showYahooMediaPlayer", false));
        webConfig.setBrowserStartIndex(getRequest().getParameter("browserStartIndex"));
        if (getAuthUser().isChangeEmail() && !StringUtils.equals(getAuthUser().getEmail(), getRequest().getParameter("email"))) {
            String oldEmail = getAuthUser().getEmail();
            getAuthUser().setEmail(getRequest().getParameter("email"));
            MyTunesRss.ADMIN_NOTIFY.notifyEmailChange(getAuthUser(), oldEmail);
        }
        webConfig.setMyTunesRssComAddress(getBooleanRequestParameter("myTunesRssComAddress", false));
        webConfig.setAlbumImageSize(getIntegerRequestParameter("albImgSize", 128));
        webConfig.setRemoteControl(getBooleanRequestParameter("remoteControl", false));
        webConfig.setKeepAlive(getBooleanRequestParameter("keepAlive", false));
        webConfig.setShowThumbnailsForAlbums(getBooleanRequestParameter("showThumbnailsForAlbums", false));
        webConfig.setShowThumbnailsForTracks(getBooleanRequestParameter("showThumbnailsForTracks", false));
        webConfig.setShowExternalSites(getBooleanRequestParameter("showExternalSites", false));
        webConfig.setShowEditTags(getBooleanRequestParameter("showEditTags", false));
        webConfig.setShowAddToPlaylist(getBooleanRequestParameter("showAddToPlaylist", false));
        transferTranscoders(webConfig);
        boolean error = false;
        error |= transferAndValidatePageSize(webConfig);
        error |= transferAndValidateRssFeedLimit(webConfig);
        error |= transferAndValidateRandomValues(webConfig);
        error |= transferAndValidateLastUpdatedTrackCount(webConfig);
        error |= transferAndValidateMostPlayedTrackCount(webConfig);
        error |= transferAndValidateRecentlyPlayedTrackCount(webConfig);
        error |= transferAndValidatePassword();
        error |= transferAndValidateLastFmAccount();
        return !error;
    }

    private void transferTranscoders(WebConfig webConfig) {
        StringBuilder transcoders = new StringBuilder();
        if (getRequest().getParameterValues("transcoder") != null) {
            for (String tcName : getRequest().getParameterValues("transcoder")) {
                transcoders.append(",").append(tcName);
            }
        }
        webConfig.setActiveTranscoders(transcoders.length() > 0 ? transcoders.substring(1) : null);
    }

    private boolean transferAndValidateLastFmAccount() {
        String username = getRequestParameter("lastfmusername", null);
        String password1 = getRequestParameter("lastfmpassword1", null);
        String password2 = getRequestParameter("lastfmpassword2", null);
        getAuthUser().setLastFmUsername(username);
        if (StringUtils.isNotEmpty(password1) || StringUtils.isNotEmpty(password2)) {
            if (StringUtils.equals(password1, password2)) {
                getAuthUser().setLastFmPasswordHash(MyTunesRss.MD5_DIGEST.digest(MyTunesRssUtils.getUtf8Bytes(password1)));
            } else {
                addError(new BundleError("error.settingsLastFmPasswordMismatch"));
                return true;
            }
        }
        return false;
    }

    private boolean transferAndValidatePassword() {
        String password1 = getRequestParameter("password1", null);
        String password2 = getRequestParameter("password2", null);
        if (StringUtils.isNotEmpty(password1) || StringUtils.isNotEmpty(password2)) {
            if (StringUtils.equals(password1, password2)) {
                byte[] passwordHash = MyTunesRss.SHA1_DIGEST.digest(MyTunesRssUtils.getUtf8Bytes(password1));
                if (!Arrays.equals(passwordHash, getAuthUser().getPasswordHash())) {
                    getAuthUser().setPasswordHash(passwordHash);
                    MyTunesRss.ADMIN_NOTIFY.notifyPasswordChange(getAuthUser());
                }
            } else {
                addError(new BundleError("error.settingsPasswordMismatch"));
                return true;
            }
        }
        return false;
    }

    private boolean transferAndValidateRandomValues(WebConfig webConfig) {
        try {
            webConfig.setRandomPlaylistSize(getIntegerRequestParameter("randomPlaylistSize", 0));
            if (webConfig.getRandomPlaylistSize() < 0 || webConfig.getRandomPlaylistSize() > 999) {
                addError(new BundleError("error.settingsRandomPlaylistSizeRange"));
                return true;
            }
        } catch (NumberFormatException e) {
            addError(new BundleError("error.settingsRandomPlaylistSizeRange"));
            return true;
        }
        webConfig.setRandomMediaType(getRequestParameter("randomMediaType", ""));
        webConfig.setRandomProtected(getBooleanRequestParameter("randomProtected", false));
        return false;
    }

    private boolean transferAndValidateLastUpdatedTrackCount(WebConfig webConfig) {
        try {
            webConfig.setLastUpdatedPlaylistSize(getIntegerRequestParameter("lastUpdatedPlaylistSize", 0));
            if (webConfig.getLastUpdatedPlaylistSize() < 0 || webConfig.getLastUpdatedPlaylistSize() > 999) {
                addError(new BundleError("error.settingsLastUpdatedPlaylistSizeRange"));
                return true;
            }
        } catch (NumberFormatException e) {
            addError(new BundleError("error.settingsLastUpdatedPlaylistSizeRange"));
            return true;
        }
        return false;
    }

    private boolean transferAndValidateMostPlayedTrackCount(WebConfig webConfig) {
        try {
            webConfig.setMostPlayedPlaylistSize(getIntegerRequestParameter("mostPlayedPlaylistSize", 0));
            if (webConfig.getMostPlayedPlaylistSize() < 0 || webConfig.getMostPlayedPlaylistSize() > 999) {
                addError(new BundleError("error.settingsMostPlayedPlaylistSizeRange"));
                return true;
            }
        } catch (NumberFormatException e) {
            addError(new BundleError("error.settingsMostPlayedPlaylistSizeRange"));
            return true;
        }
        return false;
    }

    private boolean transferAndValidateRecentlyPlayedTrackCount(WebConfig webConfig) {
        try {
            webConfig.setRecentlyPlayedPlaylistSize(getIntegerRequestParameter("recentlyPlayedPlaylistSize", 0));
            if (webConfig.getRecentlyPlayedPlaylistSize()  < 0 || webConfig.getRecentlyPlayedPlaylistSize() > 999) {
                addError(new BundleError("error.settingsRecentlyPlayedPlaylistSizeRange"));
                return true;
            }
        } catch (NumberFormatException e) {
            addError(new BundleError("error.settingsRecentlyPlayedPlaylistSizeRange"));
            return true;
        }
        return false;
    }

    private boolean transferAndValidatePageSize(WebConfig webConfig) {
        try {
            webConfig.setPageSize(getIntegerRequestParameter("pageSize", 0));
            if (webConfig.getPageSize() < 0 || webConfig.getPageSize() > 999) {
                addError(new BundleError("error.settingsPageSizeRange"));
                return true;
            }
        } catch (NumberFormatException e) {
            addError(new BundleError("error.settingsPageSizeRange"));
            return true;
        }
        return false;
    }

    private boolean transferAndValidateRssFeedLimit(WebConfig webConfig) {
        try {
            webConfig.setRssFeedLimit(getIntegerRequestParameter("rssFeedLimit", 0));
            if (webConfig.getRssFeedLimit() < 0 || webConfig.getRssFeedLimit() > 999) {
                addError(new BundleError("error.settingsFeedLimitSizeRange"));
                return true;
            }
        } catch (NumberFormatException e) {
            addError(new BundleError("error.settingsFeedLimitSizeRange"));
            return true;
        }
        return false;
    }
}