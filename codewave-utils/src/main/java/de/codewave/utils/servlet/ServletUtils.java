/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

import de.codewave.utils.xml.JXPathUtils;
import de.codewave.utils.xml.XmlUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * de.codewave.utils.servlet.ServletUtils
 */
public class ServletUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletUtils.class);

    public static String getApplicationUrl(HttpServletRequest request) {
        StringBuffer applicationUrl = new StringBuffer(request.getScheme());
        applicationUrl.append("://").append(request.getServerName());
        if ((StringUtils.equalsIgnoreCase(request.getScheme(), "http") && request.getServerPort() != 80) || (StringUtils.equalsIgnoreCase(request.getScheme(), "https") && request.getServerPort() != 443)) {
            applicationUrl.append(":").append(request.getServerPort());
        }
        applicationUrl.append(request.getContextPath());
        return applicationUrl.toString();
    }

    public static String getServletUrl(HttpServletRequest request, String servletName) {
        return getServletUrl(getApplicationUrl(request), getServletMapping(request.getSession().getServletContext(), servletName));
    }

    public static String getServletUrl(HttpServletRequest request, Class servletClass) {
        return getServletUrl(getApplicationUrl(request), getServletMapping(request.getSession().getServletContext(), servletClass));
    }

    private static String getServletUrl(String applicationUrl, String servletMapping) {
        String originalMapping = servletMapping;
        if (servletMapping.endsWith("/*")) {
            servletMapping = servletMapping.substring(0, servletMapping.length() - 2);
        }
        if (!servletMapping.contains("*")) {
            if (!servletMapping.startsWith("/")) {
                servletMapping = "/" + servletMapping;
            }
            return applicationUrl + servletMapping;
        }
        throw new IllegalArgumentException("Servlet mapping \"" + originalMapping + "\" not supported!");
    }

    public static String getServletMapping(ServletContext servletContext, String servletName) {
        try {
            return getServletMapping(servletContext.getResource("/WEB-INF/web.xml"), servletName);
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get URL for web.xml deployment descriptor.", e);
            }
        }
        return null;
    }

    public static String getServletMapping(URL webXml, String servletName) {
        if (webXml != null && StringUtils.isNotEmpty(servletName)) {
            JXPathContext jxpcWebXml = JXPathUtils.getContext(webXml);
            String prefix = "";
            try {
                String defaultNamespaceUri = XmlUtils.getDefaultNamespaceUri(webXml, "web-app");
                if (defaultNamespaceUri != null) {
                    prefix = "j2ee:";
                    jxpcWebXml.registerNamespace("j2ee", defaultNamespaceUri);
                }
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse web.xml file from \"" + webXml + "\".", e);
                }
            }
            return (String) jxpcWebXml.getValue("/" + prefix + "web-app/" + prefix + "servlet-mapping[" + prefix + "servlet-name='" + servletName +
                    "']/" + prefix + "url-pattern");
        }
        return null;
    }

    public static String getServletMapping(ServletContext servletContext, Class servletClass) {
        try {
            return getServletMapping(servletContext.getResource("/WEB-INF/web.xml"), servletClass);
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get URL for web.xml deployment descriptor.", e);
            }
        }
        return null;
    }

    public static String getServletMapping(URL webXml, Class servletClass) {
        if (webXml != null && servletClass != null) {
            JXPathContext jxpcWebXml = JXPathUtils.getContext(webXml);
            String prefix = "";
            try {
                String defaultNamespaceUri = XmlUtils.getDefaultNamespaceUri(webXml, "web-app");
                if (defaultNamespaceUri != null) {
                    prefix = "j2ee:";
                    jxpcWebXml.registerNamespace("j2ee", defaultNamespaceUri);
                }
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse web.xml file from \"" + webXml + "\".", e);
                }
            }
            String servletName = (String) jxpcWebXml.getValue("/" + prefix + "web-app/" + prefix + "servlet[" + prefix + "servlet-class='" +
                    servletClass.getName() + "']/" + prefix + "servlet-name");

            return StringUtils.isEmpty(servletName) ? null : getServletMapping(webXml, servletName);
        }
        return null;
    }

    public Collection<String> getRequestParameterValuesCollection(HttpServletRequest request, String parameterName) {
        String[] parameterValues = request.getParameterValues(parameterName);
        if (parameterValues == null || parameterValues.length == 0) {
            return Collections.emptySet();
        } else if (parameterValues.length == 1) {
            return Collections.singleton(parameterValues[0]);
        }
        return Arrays.asList(parameterValues);
    }

    public static String getRequestInfo(HttpServletRequest servletRequest) {
        StringBuffer info = new StringBuffer();
        info.append(servletRequest.getMethod()).append(" ");
        String rangeHeader = StringUtils.trimToEmpty(servletRequest.getHeader("Range"));
        if (rangeHeader.length() > 0) {
            info.append(rangeHeader).append("\n");
        }
        return info.toString();
    }

    public static boolean isRangeRequest(HttpServletRequest request) {
        String rangeHeader = StringUtils.trimToEmpty(request.getHeader("Range"));
        return rangeHeader.startsWith("bytes=") && rangeHeader.indexOf("-", "bytes=".length()) > -1 && rangeHeader.length() > "bytes=-".length();
    }

    public static boolean isHeadRequest(HttpServletRequest request) {
        return "head".equalsIgnoreCase(request.getMethod());
    }

    /**
     * Get the best available remote address for a servlet request. If the X-Forwarded-For header is set,
     * the first address in the list is returned. Otherwise the result of {@link javax.servlet.http.HttpServletRequest#getRemoteAddr()}
     * is returned.
     *
     * @param request A servlet request.
     * @return The best available remote address as specified above.
     */
    public static String getBestRemoteAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            return StringUtils.trim(StringUtils.split(xForwardedFor, ',')[0]);
        }
        return request.getRemoteAddr();
    }
}
