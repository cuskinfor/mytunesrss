package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.remote.render.AlbumRenderer;
import de.codewave.mytunesrss.remote.render.PlaylistRenderer;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssXmlRpcServlet
 */
public class MyTunesRssXmlRpcServlet extends XmlRpcServlet {
    @Override
    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        MyTunesRssRemoteEnv.setRequest(httpServletRequest);
        try {
            super.doPost(httpServletRequest, httpServletResponse);
        } finally {
            MyTunesRssRemoteEnv.removeRequest();
            httpServletRequest.getSession().invalidate();
        }
    }
}
