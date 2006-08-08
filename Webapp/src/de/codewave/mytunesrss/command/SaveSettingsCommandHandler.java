/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;

import java.util.*;

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
        boolean error = false;
        error |= transferAndValidatePageSize(webConfig);
        error |= transferAndValidateRssFeedLimit(webConfig);
        error |= transferAndValidateFakeSuffixes(webConfig);
        error |= transferAndValidateFeedTypes(webConfig);
        return !error;
    }

    private boolean transferAndValidatePageSize(WebConfig webConfig) {
        try {
            webConfig.setPageSize(Integer.parseInt(getRequestParameter("pageSize", "0")));
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
            webConfig.setRssFeedLimit(Integer.parseInt(getRequestParameter("rssFeedLimit", "0")));
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

    private boolean transferAndValidateFakeSuffixes(WebConfig webConfig) {
        webConfig.clearFileSuffixes();
        for (String parameterName : Collections.list((Enumeration<String>)getRequest().getParameterNames())) {
            if (parameterName.startsWith("suffix.")) {
                String fakeSuffix = getRequestParameter(parameterName, "");
                while (fakeSuffix.startsWith(".")) {
                    fakeSuffix = fakeSuffix.substring(1);
                }
                webConfig.addFileSuffix(parameterName.substring("suffix.".length()), fakeSuffix);
            }
        }
        webConfig.setShowDownload("true".equals(getRequestParameter("showDownload", "false")));
        return false;
    }

    private boolean transferAndValidateFeedTypes(WebConfig webConfig) {
        webConfig.clearFeedTypes();
        String[] feedTypes = getNonEmptyParameterValues("feedType");
        if (feedTypes != null && feedTypes.length > 0) {
            for (String type : feedTypes) {
                webConfig.addFeedType(type);
            }
        }
        webConfig.setRssArtwork(Boolean.valueOf(getRequestParameter("rssArtwork", "false")));
        if (webConfig.getFeedTypes() == null || webConfig.getFeedTypes().length == 0) {
            addError(new BundleError("error.settingsNoFeedType"));
            return true;
        }
        return false;
    }
}