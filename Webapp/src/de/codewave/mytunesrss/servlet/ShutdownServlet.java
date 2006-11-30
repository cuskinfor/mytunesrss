package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.*;
import de.codewave.utils.network.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.servlet.ShutdownServlet
 */
public class ShutdownServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doAction(httpServletRequest, httpServletResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doAction(httpServletRequest, httpServletResponse);
    }

    private void doAction(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (MyTunesRss.HEADLESS && NetworkUtils.isLocalAddress(httpServletRequest.getRemoteAddr())) {
            MyTunesRss.CONFIG.save();
            MyTunesRss.SERVER_RUNNING_TIMER.cancel();
            MyTunesRss.STORE.destroy();
            MyTunesRss.WEBSERVER.stop();
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}