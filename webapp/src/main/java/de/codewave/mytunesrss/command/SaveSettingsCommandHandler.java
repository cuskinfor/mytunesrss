/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.command.SaveSettingsCommandHandler
 */
public class SaveSettingsCommandHandler extends MyTunesRssCommandHandler {
    private static final Log LOG = LogFactory.getLog(SaveSettingsCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            WebConfig webConfig = getWebConfig();
            if (transferAndValidate(webConfig)) {
                webConfig.save(getRequest(), getResponse());
                if (getAuthUser().isSaveWebSettings()) {
                    getAuthUser().setWebSettings(webConfig.createCookieValue());
                }
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
        webConfig.setLame(getBooleanRequestParameter("useLame", false));
        webConfig.setLameTargetBitrate(getIntegerRequestParameter("lameTargetBitrate", 96));
        webConfig.setLameTargetSampleRate(getIntegerRequestParameter("lameTargetSampleRate", 22050));
        webConfig.setFaad2(getBooleanRequestParameter("useFaad2", false));
        webConfig.setAlac(getBooleanRequestParameter("useAlac", false));
        webConfig.setTranscodeOnTheFlyIfPossible(getBooleanRequestParameter("transcodeOnTheFlyIfPossible", false));
        webConfig.setRandomSource(getRequestParameter("randomSource", ""));
        webConfig.setFlashplayerType(getRequestParameter("flashplayerType", "jw"));
        boolean error = false;
        error |= transferAndValidatePageSize(webConfig);
        error |= transferAndValidateRssFeedLimit(webConfig);
        error |= transferAndValidateRandomTrackCount(webConfig);
        error |= transferAndValidateLastUpdatedTrackCount(webConfig);
        error |= transferAndValidateMostPlayedTrackCount(webConfig);
        error |= transferAndValidatePassword();
        return !error;
    }

    private boolean transferAndValidatePassword() {
        String password1 = getRequestParameter("password1", null);
        String password2 = getRequestParameter("password2", null);
        if (StringUtils.isNotEmpty(password1) || StringUtils.isNotEmpty(password2)) {
            if (StringUtils.equals(password1, password2)) {
                try {
                    getAuthUser().setPasswordHash(MyTunesRss.MESSAGE_DIGEST.digest(password1.getBytes("UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not get bytes from password string.", e);
                    }
                }
            } else {
                addError(new BundleError("error.settingsPasswordMismatch"));
                return true;
            }
        }
        return false;
    }

    private boolean transferAndValidateRandomTrackCount(WebConfig webConfig) {
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