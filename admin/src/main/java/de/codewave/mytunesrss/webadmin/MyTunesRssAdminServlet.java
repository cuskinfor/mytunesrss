/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.gwt.server.ApplicationServlet;

import java.io.BufferedWriter;
import java.io.IOException;

public class MyTunesRssAdminServlet extends ApplicationServlet {
    @Override
    protected void writeAjaxPageHtmlHeader(BufferedWriter page, String title, String themeUri) throws IOException {
        super.writeAjaxPageHtmlHeader(page, title, themeUri);
        page.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no, maximum-scale=1.0\" />");
    }
}
