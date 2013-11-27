/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class LogServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Boolean.TRUE.equals(request.getSession().getAttribute("MyTunesRSSWebAdmin"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Please login to the MyTunesRSS web admin.");
        } else {
            if ("true".equals(request.getParameter("events"))) {
                sendEvents(response);
            } else {
                sendPage(response);
            }
        }
    }

    private void sendPage(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        InputStream inStream = getClass().getResourceAsStream("logservlet.html");
        try {
            IOUtils.copy(inStream, response.getOutputStream());
        } finally {
            inStream.close();
        }
    }

    private void sendEvents(HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m");
        PrintWriter writer = response.getWriter();
        BlockingQueue<LoggingEvent> queue = MyTunesRss.LOG_QUEUE_MANAGER.createQueue();
        try {
            for (LoggingEvent event = queue.poll(10000, TimeUnit.MILLISECONDS); true; event = queue.poll(10000, TimeUnit.MILLISECONDS)) {
                if (event != null) {
                    writer.println("event: log");
                    writer.println("data: " + StringEscapeUtils.escapeHtml4(layout.format(event)));
                    if (layout.ignoresThrowable() && event.getThrowableStrRep() != null) {
                        for (String s : event.getThrowableStrRep()) {
                            writer.println("data: " + StringEscapeUtils.escapeHtml4(s));
                        }
                    }
                } else {
                    writer.println("event: noop");
                }
                writer.println();
                writer.flush();
            }
        } catch (InterruptedException e) {
            // ignore and stop writing
        } finally {
            MyTunesRss.LOG_QUEUE_MANAGER.removeQueue(queue);
        }
    }

}
