package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.remote.service.*;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssJsonRpcServlet
 */
public class MyTunesRssJsonRpcServlet extends JSONRPCServlet {
    static {
        JSONRPCBridge.getGlobalBridge().registerObject("AlbumService", new AlbumService());
        JSONRPCBridge.getGlobalBridge().registerObject("ArtistService", new ArtistService());
        JSONRPCBridge.getGlobalBridge().registerObject("LoginService", new LoginService());
        JSONRPCBridge.getGlobalBridge().registerObject("PlaylistService", new PlaylistService());
        JSONRPCBridge.getGlobalBridge().registerObject("EditPlaylistService", new EditPlaylistService());
        JSONRPCBridge.getGlobalBridge().registerObject("TrackService", new TrackService());
        JSONRPCBridge.getGlobalBridge().registerObject("UserService", new UserService());
        JSONRPCBridge.getGlobalBridge().registerObject("ServerService", new ServerService());
        JSONRPCBridge.getGlobalBridge().registerObject("GenreService", new GenreService());
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        if (MyTunesRss.REGISTRATION.isRegistered()) {
            MyTunesRssRemoteEnv.setRequest(httpServletRequest);
            try {
                super.service(httpServletRequest, httpServletResponse);
            } finally {
                MyTunesRssRemoteEnv.removeRequest();
                httpServletRequest.getSession().invalidate();
            }
        } else {
            throw new IllegalStateException("JSON RPC is available in the registered version of MyTunesRSS only.");
        }
    }
}
