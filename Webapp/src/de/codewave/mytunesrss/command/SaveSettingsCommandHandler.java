/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;

/**
 * de.codewave.mytunesrss.command.SaveSettingsCommandHandler
 */
public class SaveSettingsCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        WebConfig webConfig = getWebConfig();
        if (transferAndValidate(webConfig)) {
            webConfig.save(getResponse());
            forward(MyTunesRssCommand.ShowPortal);
        } else {
            forward(MyTunesRssResource.Settings);
        }
    }

    private boolean transferAndValidate(WebConfig webConfig) {
        webConfig.setRssArtwork(getBooleanRequestParameter("rssArtwork", false));
        webConfig.setShowDownload(getBooleanRequestParameter("showDownload", false));
        webConfig.setShowRss(isParameterValue("feedType", "rss"));
        webConfig.setShowM3u(isParameterValue("feedType", "m3u"));
        boolean error = false;
        error |= transferAndValidatePageSize(webConfig);
        error |= transferAndValidateRssFeedLimit(webConfig);
        error |= transferAndValidateRandomTrackCount(webConfig);
        return !error;
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