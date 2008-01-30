package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.*;
import de.codewave.mytunesrss.remote.render.*;
import de.codewave.mytunesrss.remote.service.*;
import de.codewave.mytunesrss.command.*;
import org.apache.commons.logging.*;
import org.apache.commons.lang.*;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.common.*;
import org.apache.xmlrpc.server.*;
import org.apache.xmlrpc.webserver.*;
import org.jabsorb.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssXmlRpcServlet
 */
public class MyTunesRssJsonRpcServlet extends JSONRPCServlet {
    private static final Log LOG = LogFactory.getLog(MyTunesRssJsonRpcServlet.class);

    private static final RenderMachine RENDER_MACHINE = new RenderMachine();

    static {
        RENDER_MACHINE.addRenderer(Playlist.class, new PlaylistRenderer());
        RENDER_MACHINE.addRenderer(Album.class, new AlbumRenderer());
        JSONRPCBridge.getGlobalBridge().registerObject("albumService", new AlbumService());
        JSONRPCBridge.getGlobalBridge().registerObject("playlistService", new PlaylistService());
        JSONRPCBridge.getGlobalBridge().registerObject("loginService", new LoginService());
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        MyTunesRssRemoteEnv.setRequest(httpServletRequest);
        MyTunesRssRemoteEnv.setRenderMachine(RENDER_MACHINE);
        MyTunesRssRemoteEnv.setUser((User)httpServletRequest.getSession().getAttribute("jsonUser"));
        try {
            super.service(httpServletRequest, httpServletResponse);
        } finally {
            MyTunesRssRemoteEnv.removeUser();
            MyTunesRssRemoteEnv.removeRequest();
            MyTunesRssRemoteEnv.removeRenderMachine();
        }
    }
}
