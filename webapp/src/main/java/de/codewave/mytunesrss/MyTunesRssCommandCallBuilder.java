/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.command.MyTunesRssCommand;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class MyTunesRssCommandCallBuilder {
    private MyTunesRssCommand myCommand;
    private Map<String, String> myParams = new HashMap<String, String>();

    public MyTunesRssCommandCallBuilder(MyTunesRssCommand command) {
        myCommand = command;
    }

    public MyTunesRssCommandCallBuilder addParam(String key, String value) {
        myParams.put(key, value);
        return this;
    }

    public String getCall(HttpServletRequest request) {
        String servletUrl = MyTunesRssWebUtils.getServletUrl(request);
        StringBuilder builder = new StringBuilder(MyTunesRssWebUtils.getApplicationUrl(request) + servletUrl.substring(servletUrl.lastIndexOf("/")) + "/" + myCommand.getName());
        String auth = (String) request.getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) request.getSession().getAttribute("auth");
        }
        builder.append("/").append(auth);
        if (!myParams.isEmpty()) {
            StringBuilder pathInfo = new StringBuilder();
            for (Map.Entry<String, String> param : myParams.entrySet()) {
                pathInfo.append("/").append(param.getKey()).append("=").append(MyTunesRssUtils.getUtf8UrlEncoded(param.getValue()));
            }
            builder.append("/").append(MyTunesRssWebUtils.encryptPathInfo(request, pathInfo.substring(1)));
        }
        return builder.toString();
    }
}
