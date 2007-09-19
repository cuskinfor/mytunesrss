package de.codewave.mytunesrss;

import de.codewave.utils.servlet.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.jsp.Error;

import javax.crypto.*;
import javax.servlet.http.*;
import java.io.*;
import java.security.*;
import java.util.*;

import org.apache.commons.logging.*;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2006<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 08.11.2006
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class MyTunesRssWebUtils {
    private static final Log LOG = LogFactory.getLog(MyTunesRssWebUtils.class);

    public static String getServletUrl(HttpServletRequest request) {
        return ServletUtils.getApplicationUrl(request) + "/mytunesrss";
    }

    public static String encryptPathInfo(String pathInfo) {
        try {
            if (MyTunesRss.CONFIG.getPathInfoKey() != null) {
                Cipher cipher = Cipher.getInstance(MyTunesRss.CONFIG.getPathInfoKey().getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE, MyTunesRss.CONFIG.getPathInfoKey());
                return "{" + MyTunesRssBase64Utils.encode(cipher.doFinal(pathInfo.getBytes("UTF-8"))) + "}";
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not encrypt path info.", e);
            }
        }
        return pathInfo;
    }

    public static WebConfig getWebConfig(HttpServletRequest httpServletRequest) {
        WebConfig webConfig = (WebConfig)httpServletRequest.getSession().getAttribute("config");
        if (webConfig == null) {
            webConfig = new WebConfig();
            webConfig.load(httpServletRequest);
            httpServletRequest.getSession().setAttribute("config", webConfig);
        }
        return webConfig;
    }

    public static void addError(HttpServletRequest request, Error error, String holderName) {
        Set<Error> errors = (Set<Error>)request.getSession().getAttribute(holderName);
        if (errors == null) {
            synchronized (request.getSession()) {
                errors = (Set<Error>)request.getSession().getAttribute(holderName);
                if (errors == null) {
                    errors = new HashSet<Error>();
                    request.getSession().setAttribute(holderName, errors);
                }
            }
        }
        errors.add(error);
    }
}



