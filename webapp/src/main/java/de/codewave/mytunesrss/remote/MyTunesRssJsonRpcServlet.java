package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.remote.render.AlbumRenderer;
import de.codewave.mytunesrss.remote.render.PlaylistRenderer;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.remote.service.AlbumService;
import de.codewave.mytunesrss.remote.service.ArtistService;
import de.codewave.mytunesrss.remote.service.LoginService;
import de.codewave.mytunesrss.remote.service.PlaylistService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssXmlRpcServlet
 */
public class MyTunesRssJsonRpcServlet extends JSONRPCServlet {
    private static final Log LOG = LogFactory.getLog(MyTunesRssJsonRpcServlet.class);

    private static final RenderMachine RENDER_MACHINE = new RenderMachine();

    static {
        RENDER_MACHINE.addRenderer(Playlist.class, new PlaylistRenderer());
        RENDER_MACHINE.addRenderer(Album.class, new AlbumRenderer());
        JSONRPCBridge.getGlobalBridge().registerObject("AlbumService", new AlbumService());
        JSONRPCBridge.getGlobalBridge().registerObject("ArtistService", new ArtistService());
        JSONRPCBridge.getGlobalBridge().registerObject("LoginService", new LoginService());
        JSONRPCBridge.getGlobalBridge().registerObject("PlaylistService", new PlaylistService());
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        MyTunesRssRemoteEnv.setRequest(httpServletRequest);
        MyTunesRssRemoteEnv.setRenderMachine(RENDER_MACHINE);
        MyTunesRssRemoteEnv.setUser((User)httpServletRequest.getSession().getAttribute("remoteApiUser"));
        try {
            super.service(httpServletRequest, httpServletResponse);
        } finally {
            MyTunesRssRemoteEnv.removeUser();
            MyTunesRssRemoteEnv.removeRequest();
            MyTunesRssRemoteEnv.removeRenderMachine();
        }
    }
}
