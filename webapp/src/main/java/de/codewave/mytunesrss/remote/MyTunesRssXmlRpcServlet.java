package de.codewave.mytunesrss.remote;

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
        super.service(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        MyTunesRssRemoteEnv.setRequest(httpServletRequest);
        MyTunesRssRemoteEnv.initRequestWebConfig();
        try {
            super.doPost(httpServletRequest, httpServletResponse);
        } finally {
            MyTunesRssRemoteEnv.removeRequest();
            httpServletRequest.getSession().invalidate();
        }
    }
}
