/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.network;

import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * de.codewave.utils.network.UpdateInfo
 */
public class UpdateInfo {
    private JXPathContext myContext;

    UpdateInfo(JXPathContext context) {
        myContext = context;
    }

    public URL getUrl(String osIdentifier) throws MalformedURLException {
        return new URL(JXPathUtils.getStringValue(myContext, "/update-info/url[@os='" + osIdentifier + "']", ""));
    }

    public String getVersion() {
        return JXPathUtils.getStringValue(myContext, "/update-info/version", "");
    }

    public String getFileName(String osIdentifier) {
        return JXPathUtils.getStringValue(myContext, "/update-info/filename[@os='" + osIdentifier + "']", "");
    }

    public String getInfo(Locale locale) {
        String info = JXPathUtils.getStringValue(myContext, "/update-info/info[@lang='" + locale.getLanguage() + "']", "");
        if (StringUtils.isEmpty(info)) {
            info = JXPathUtils.getStringValue(myContext, "/update-info/info[not(@lang)]", "");
        }
        return info;
    }
}
