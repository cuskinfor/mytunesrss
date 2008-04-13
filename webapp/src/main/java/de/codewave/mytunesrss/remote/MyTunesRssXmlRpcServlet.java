package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.MyTunesRss;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssXmlRpcServlet
 */
public class MyTunesRssXmlRpcServlet extends XmlRpcServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (MyTunesRss.REGISTRATION.isRegistered()) {
            super.service(req, resp);
        } else {
            throw new IllegalStateException("XML RPC is available in the registered version of MyTunesRSS only.");
        }
    }

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
