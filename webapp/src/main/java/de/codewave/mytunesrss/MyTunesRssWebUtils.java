package de.codewave.mytunesrss;

import de.codewave.mytunesrss.jsp.Error;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.servlet.ServletUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

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

    public static User getAuthUser(HttpServletRequest request) {
        User user = (User)request.getSession().getAttribute("authUser");
        if (user == null) {
            user = (User)request.getAttribute("authUser");
        }
        return user;
    }

    public static String encryptPathInfo(HttpServletRequest request, String pathInfo) {
        try {
            if (MyTunesRss.CONFIG.getPathInfoKey() != null && (getAuthUser(request) == null || getAuthUser(request).isUrlEncryption())) {
                Cipher cipher = Cipher.getInstance(MyTunesRss.CONFIG.getPathInfoKey().getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE, MyTunesRss.CONFIG.getPathInfoKey());
                return "{" + MyTunesRssBase64Utils.encode(cipher.doFinal(pathInfo.getBytes("UTF-8"))) + "}";
            } else {
                return pathInfo;
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
            webConfig.clearWithDefaults();
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



