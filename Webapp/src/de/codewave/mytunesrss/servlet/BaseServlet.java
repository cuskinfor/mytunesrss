/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.musicfile.*;
import de.codewave.mytunesrss.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.servlet.BaseServlet
 */
public abstract class BaseServlet extends HttpServlet {
    protected Collection<String> getRequestSelection(HttpServletRequest request) {
        String[] selection = request.getParameterValues("id");
        if (selection != null) {
            return Arrays.asList(selection);
        }
        return Collections.emptyList();
    }

    protected void createSectionsAndForward(HttpServletRequest request, HttpServletResponse response, SortOrder sortOrder)
            throws ServletException, IOException {
        Collection<String> requestSelection = getRequestSelection(request);
        request.setAttribute("sortOrder", sortOrder);
        request.setAttribute("sections", SectionUtils.buildSections((Collection<MusicFile>)request.getSession().getAttribute("searchResult"),
                                                                    sortOrder));
        SectionUtils.setSelection((Collection<Section>)request.getAttribute("sections"), requestSelection);
        String authHash = (String)request.getSession().getAttribute("authHash");
        if (StringUtils.isNotEmpty(authHash)) {
            request.setAttribute("authInfo", "/au=" + authHash);
        }
        request.getRequestDispatcher("/select.jsp").forward(request, response);
    }

    protected MyTunesRssConfig getMyTunesRssConfig(HttpServletRequest request) {
        return (MyTunesRssConfig)request.getSession().getServletContext().getAttribute(MyTunesRssConfig.class.getName());
    }

    protected boolean isAuthorized(HttpServletRequest request, String authHash) {
        MyTunesRssConfig config = getMyTunesRssConfig(request);
        return !config.isAuth() || ("" + config.getPasswordHash()).equals(authHash);
    }
}