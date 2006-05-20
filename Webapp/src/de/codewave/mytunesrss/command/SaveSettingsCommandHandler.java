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
        webConfig.clear();
        webConfig.setRememberLogin(Boolean.valueOf(getRequestParameter("rememberLogin", "false")));
        webConfig.setRssFeedLimit(Integer.parseInt(getRequestParameter("rssFeedLimit", "0")));
        for (String parameterName : Collections.list((Enumeration<String>)getRequest().getParameterNames())) {
            if (parameterName.startsWith("suffix.")) {
                webConfig.addFileSuffix(parameterName.substring("suffix.".length()), getRequestParameter(parameterName, ""));
            }
        }
        String[] feedTypes = getNonEmptyParameterValues("feedType");
        if (feedTypes != null && feedTypes.length > 0) {
            for (String type : feedTypes) {
                webConfig.addFeedType(type);
            }
        }
        if (validate(webConfig)) {
            webConfig.save(getResponse());
            forward(MyTunesRssCommand.ShowPortal);
        } else {
            forward(MyTunesRssResource.Settings);
        }
    }

    private boolean validate(WebConfig webConfig) {
        StringBuffer errors = new StringBuffer();
        if (webConfig.getRssFeedLimit() < 0) {
            errors.append("feed limit negative! ");
        }
        if (webConfig.getFeedTypes() == null || webConfig.getFeedTypes().length == 0) {
            errors.append("must select at least one feed type! ");
        }
        if (errors.length() > 0) {
            setError(errors.toString());
            return false;
        }
        return true;
    }
}