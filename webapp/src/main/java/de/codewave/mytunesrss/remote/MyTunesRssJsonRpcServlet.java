package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.remote.service.*;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssJsonRpcServlet
 */
public class MyTunesRssJsonRpcServlet extends JSONRPCServlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        JSONRPCBridge.getGlobalBridge().registerObject("AlbumService", new AlbumService());
        JSONRPCBridge.getGlobalBridge().registerObject("ArtistService", new ArtistService());
        JSONRPCBridge.getGlobalBridge().registerObject("LoginService", new LoginService());
        JSONRPCBridge.getGlobalBridge().registerObject("PlaylistService", new PlaylistService());
        JSONRPCBridge.getGlobalBridge().registerObject("EditPlaylistService", new EditPlaylistService());
        JSONRPCBridge.getGlobalBridge().registerObject("TrackService", new TrackService());
        JSONRPCBridge.getGlobalBridge().registerObject("ServerService", new ServerService());
        JSONRPCBridge.getGlobalBridge().registerObject("GenreService", new GenreService());
        JSONRPCBridge.getGlobalBridge().registerObject("TranscodingService", new TranscodingService());
        JSONRPCBridge.getSerializer().setMarshallClassHints(false);
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        MyTunesRssRemoteEnv.setRequest(httpServletRequest);
        try {
            super.service(httpServletRequest, httpServletResponse);
        } finally {
            MyTunesRssRemoteEnv.removeRequest();
            httpServletRequest.getSession().invalidate();
        }
    }
}
