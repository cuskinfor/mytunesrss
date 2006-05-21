/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;

import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.SaveSettingsCommandHandler
 */
public class SaveSettingsCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        WebConfig webConfig = getWebConfig();
        String errors = transferAndValidate(webConfig);
        if (StringUtils.isEmpty(errors)) {
            webConfig.save(getResponse());
            forward(MyTunesRssCommand.ShowPortal);
        } else {
            getRequest().setAttribute("error", errors);
            forward(MyTunesRssResource.Settings);
        }
    }

    private String transferAndValidate(WebConfig webConfig) {
        StringBuffer errors = new StringBuffer();
        errors.append(transferAndValidatePageSize(webConfig)).append(" ");
        errors.append(transferAndValidateRssFeedLimit(webConfig)).append(" ");
        errors.append(transferAndValidateFakeSuffixes(webConfig)).append(" ");
        errors.append(transferAndValidateFeedTypes(webConfig)).append(" ");
        return errors.toString().trim();
    }

    private String transferAndValidatePageSize(WebConfig webConfig) {
        try {
            webConfig.setPageSize(Integer.parseInt(getRequestParameter("pageSize", "0")));
            if (webConfig.getPageSize() < 0 || webConfig.getPageSize() > 1000) {
                return "page size range error.";
            }
        } catch (NumberFormatException e) {
            return "page size format error.";
        }
        return "";
    }

    private String transferAndValidateRssFeedLimit(WebConfig webConfig) {
        try {
            webConfig.setRssFeedLimit(Integer.parseInt(getRequestParameter("rssFeedLimit", "0")));
            if (webConfig.getRssFeedLimit() < 0 || webConfig.getRssFeedLimit() > 1000) {
                return "feed limit range error.";
            }
        } catch (NumberFormatException e) {
            return "rss feed limit format error.";
        }
        return "";
    }

    private String transferAndValidateFakeSuffixes(WebConfig webConfig) {
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
        return "";
    }

    private String transferAndValidateFeedTypes(WebConfig webConfig) {
        webConfig.clearFeedTypes();
        String[] feedTypes = getNonEmptyParameterValues("feedType");
        if (feedTypes != null && feedTypes.length > 0) {
            for (String type : feedTypes) {
                webConfig.addFeedType(type);
            }
        }
        if (webConfig.getFeedTypes() == null || webConfig.getFeedTypes().length == 0) {
            return "must select at least one feed type!";
        }
        return "";
    }
}