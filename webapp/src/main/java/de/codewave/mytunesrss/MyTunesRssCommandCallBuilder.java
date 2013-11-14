/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTunesRssCommandCallBuilder {
    private MyTunesRssCommand myCommand;
    private Map<String, String> myParams = new HashMap<String, String>();
    private List<String> myPathInfoSegments = new ArrayList<String>();
    private String myFileName;

    public MyTunesRssCommandCallBuilder(MyTunesRssCommand command) {
        myCommand = command;
    }

    public MyTunesRssCommandCallBuilder addParam(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            myParams.put(key, value);
        }
        return this;
    }

    public MyTunesRssCommandCallBuilder addPathInfoSegment(String segment) {
        if (StringUtils.isNotBlank(segment)) {
            myPathInfoSegments.add(segment);
        }
        return this;
    }

    public MyTunesRssCommandCallBuilder setFileName(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            myFileName = fileName;
        }
        return this;
    }

    public String getCall(HttpServletRequest request) {
        String servletUrl = MyTunesRssWebUtils.getServletUrl(request);
        StringBuilder builder = new StringBuilder(MyTunesRssWebUtils.getApplicationUrl(request) + servletUrl.substring(servletUrl.lastIndexOf("/")) + "/" + myCommand.getName());
        String auth = (String) request.getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) request.getSession().getAttribute("auth");
        }
        if (StringUtils.isNotBlank(auth)) {
            builder.append("/").append(auth);
        }
        StringBuilder pathInfo = new StringBuilder();
        if (!myParams.isEmpty()) {
            for (Map.Entry<String, String> param : myParams.entrySet()) {
                pathInfo.append("/").append(param.getKey()).append("=").append(MiscUtils.getUtf8UrlEncoded(param.getValue()));
            }
        }
        for (String segment : myPathInfoSegments) {
            pathInfo.append("/").append(segment);
        }
        if (pathInfo.length() > 1) {
            builder.append("/").append(MyTunesRssWebUtils.encryptPathInfo(request, pathInfo.substring(1)));
        }
        if (StringUtils.isNotBlank(myFileName)) {
            builder.append("/").append(myFileName);
        }
        return builder.toString();
    }
}
