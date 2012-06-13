package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.remote.service.*;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssJsonRpcServlet
 */
public class MyTunesRssJsonRpcServlet extends JSONRPCServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssJsonRpcServlet.class);

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        JSONRPCBridge.getGlobalBridge().registerObject("AlbumService", new AlbumService());
        JSONRPCBridge.getGlobalBridge().registerObject("ArtistService", new ArtistService());
        JSONRPCBridge.getGlobalBridge().registerObject("LoginService", new LoginService());
        JSONRPCBridge.getGlobalBridge().registerObject("PlaylistService", new PlaylistService());
        JSONRPCBridge.getGlobalBridge().registerObject("TrackService", new TrackService());
        JSONRPCBridge.getGlobalBridge().registerObject("ServerService", new ServerService());
        JSONRPCBridge.getGlobalBridge().registerObject("GenreService", new GenreService());
        JSONRPCBridge.getGlobalBridge().registerObject("TranscodingService", new TranscodingService());
        JSONRPCBridge.getGlobalBridge().registerObject("RemoteControlService", new RemoteControlService());
        JSONRPCBridge.getGlobalBridge().registerObject("SessionService", new SessionService());
        try {
            JSONRPCBridge.setSerializer(new MyTunesRssJsonSerializer());
        } catch (Exception e) {
            throw new ServletException("Could not create special MyTunesRSS JSON serializer!", e);
        }
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        HttpSession session = httpServletRequest.getSession(false);
        MyTunesRssRemoteEnv.setRequest(httpServletRequest);
        MyTunesRssRemoteEnv.initRequestWebConfig();
        try {
            super.service(httpServletRequest, httpServletResponse);
        } finally {
            MyTunesRssRemoteEnv.removeRequest();
            if (session == null) {
                // if we did not have a session before, we invalidate the current one if it has been created
                if (httpServletRequest.getSession(false) != null) {
                    httpServletRequest.getSession().invalidate();
                }
            }
        }
    }
}
