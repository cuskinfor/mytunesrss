package de.codewave.mytunesrss.xmlrpc;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.command.*;
import org.apache.commons.logging.*;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.common.*;
import org.apache.xmlrpc.server.*;
import org.apache.xmlrpc.webserver.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.xmlrpc.MyTunesRssXmlRpcServlet
 */
public class MyTunesRssXmlRpcServlet extends XmlRpcServlet {
    private static final Log LOG = LogFactory.getLog(MyTunesRssXmlRpcServlet.class);
    private static final ThreadLocal<User> USERS = new ThreadLocal<User>();
    private static final ThreadLocal<HttpServletRequest> REQUESTS = new ThreadLocal<HttpServletRequest>();

    public static User getAuthUser() {
        return USERS.get();
    }

    protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
        PropertyHandlerMapping mapping = (PropertyHandlerMapping)super.newXmlRpcHandlerMapping();
        AbstractReflectiveHandlerMapping.AuthenticationHandler handler = new AbstractReflectiveHandlerMapping.AuthenticationHandler() {
            public boolean isAuthorized(XmlRpcRequest request) {
                XmlRpcHttpRequestConfig config = (XmlRpcHttpRequestConfig)request.getConfig();
                User user = MyTunesRss.CONFIG.getUser(config.getBasicUserName());
                try {
                    byte[] passwordHash = config.getBasicPassword() != null ? MyTunesRss.MESSAGE_DIGEST.digest(config
                            .getBasicPassword().getBytes("UTF-8")) : new byte[0];
                    boolean authorized = user != null && Arrays.equals(user.getPasswordHash(), passwordHash) && user.isActive();
                    if (authorized) {
                        USERS.set(user);
                    }
                    return authorized;
                } catch (UnsupportedEncodingException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not create password hash from plain text password.", e);
                    }
                    return false;
                }
            }
        };
        mapping.setAuthenticationHandler(handler);
        return mapping;
    }

    @Override
    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        REQUESTS.set(httpServletRequest);
        try {
            super.doPost(httpServletRequest, httpServletResponse);
        } finally {
            USERS.remove();
            REQUESTS.remove();
        }
    }

    public static String getServerCall(MyTunesRssCommand command) {
        String auth = MyTunesRssWebUtils.encryptPathInfo("auth=" + MyTunesRssBase64Utils.encode(USERS.get().getName()) + " " +
                MyTunesRssBase64Utils.encode(USERS.get().getPasswordHash()));
        return MyTunesRssWebUtils.getServletUrl(REQUESTS.get()) + "/" + command.getName() + "/" + auth;
    }
}
