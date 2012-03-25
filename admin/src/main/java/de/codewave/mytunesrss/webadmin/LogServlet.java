/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import de.codewave.mytunesrss.IndexedLoggingEvent;
import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PatternLayout;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class LogServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Boolean.TRUE.equals(request.getSession().getAttribute("MyTunesRSSWebAdmin"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Please login to the MyTunesRSS web admin.");
        } else {
            if (StringUtils.isBlank(request.getParameter("index"))) {
                sendPage(response);
            } else {
                String lineSeparator = "\r\n";
                sendLogLines(Long.parseLong(request.getParameter("index")), lineSeparator, response);
            }
        }
    }

    private void sendPage(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        IOUtils.copy(getClass().getResourceAsStream("logservlet.html"), response.getOutputStream());
    }

    private void sendLogLines(long index, String lineSeparator, HttpServletResponse response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m" + lineSeparator);
        long lastIndex = Long.MIN_VALUE;
        for (Iterator<IndexedLoggingEvent> iter = MyTunesRss.LOG_BUFFER.iterator(); iter.hasNext();) {
            IndexedLoggingEvent indexedLoggingEvent = iter.next();
            if (indexedLoggingEvent.getIndex() >= index) {
                baos.write(layout.format(indexedLoggingEvent.getLoggingEvent()).getBytes("UTF-8"));
                if (layout.ignoresThrowable() && indexedLoggingEvent.getLoggingEvent().getThrowableStrRep() != null) {
                    for (String s : indexedLoggingEvent.getLoggingEvent().getThrowableStrRep()) {
                        baos.write(s.getBytes("UTF-8"));
                        baos.write(lineSeparator.getBytes("UTF-8"));
                    }
                }
            }
            lastIndex = indexedLoggingEvent.getIndex();
        }
        response.setHeader("X-MYTUNESRSS-LASTINDEX", Long.toString(lastIndex));
        byte[] buffer = baos.toByteArray();
        baos.close();
        baos = null; // early GC
        if (buffer.length > 0) {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.setContentLength(buffer.length);
            response.getOutputStream().write(buffer);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
