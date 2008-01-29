package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.*;
import de.codewave.mytunesrss.command.*;
import org.apache.commons.logging.*;
import org.apache.commons.lang.*;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.common.*;
import org.apache.xmlrpc.server.*;
import org.apache.xmlrpc.webserver.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssXmlRpcServlet
 */
public class MyTunesRssXmlRpcServlet extends XmlRpcServlet {
    private static final Log LOG = LogFactory.getLog(MyTunesRssXmlRpcServlet.class);

    private static final RenderMachine RENDER_MACHINE = new RenderMachine();

    static {
        RENDER_MACHINE.addRenderer(Playlist.class, new PlaylistRenderer());
        RENDER_MACHINE.addRenderer(Album.class, new AlbumRenderer());
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
                        MyTunesRssRemoteEnv.setUser(user);
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
        MyTunesRssRemoteEnv.setRequest(httpServletRequest);
        MyTunesRssRemoteEnv.setRenderMachine(RENDER_MACHINE);
        try {
            super.doPost(httpServletRequest, httpServletResponse);
        } finally {
            MyTunesRssRemoteEnv.removeUser();
            MyTunesRssRemoteEnv.removeRequest();
            MyTunesRssRemoteEnv.removeRenderMachine();
        }
    }
}
