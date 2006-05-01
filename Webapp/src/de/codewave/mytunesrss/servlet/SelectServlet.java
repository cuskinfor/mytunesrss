/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.itunes.*;
import de.codewave.mytunesrss.musicfile.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class SelectServlet extends BaseServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Collection<String> requestSelection = getRequestSelection(request);
        Collection<MusicFile> playlist = (Collection<MusicFile>)request.getSession().getAttribute("playlist");
        if (playlist == null) {
            playlist = new ArrayList<MusicFile>();
            request.getSession().setAttribute("playlist", playlist);
        }
        boolean finalSelection = "true".equalsIgnoreCase(request.getParameter("final"));
        if (!requestSelection.isEmpty() || !finalSelection || !playlist.isEmpty()) {
            ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
            List<MusicFile> selectedFiles = new ArrayList<MusicFile>();
            for (String id : requestSelection) {
                List<MusicFile> matches = library.getMatchingFiles(new MusicFileIdSearch(id));
                SortOrder sortOrder = SortOrder.valueOf(request.getParameter("sortOrder"));
                switch (sortOrder) {
                    case Album:
                        Collections.sort(matches, new AlbumComparator());
                        break;
                    case Artist:
                        Collections.sort(matches, new ArtistComparator());
                        break;
                    default:
                        // intentionally left blank
                }
                selectedFiles.addAll(matches);
            }
            MyTunesRssConfig config = getMyTunesRssConfig(request);
            int targetSize = playlist.size() + selectedFiles.size();
            int maxSize = Integer.parseInt(config.getMaxRssItems());
            if (config.isLimitRss() && targetSize > maxSize) {
                request.setAttribute("error", "error.too_many_feed_items");
                request.setAttribute("errorParam0", new Integer(targetSize));
                request.setAttribute("errorParam1", new Integer(maxSize));
                SortOrder sortOrder = SortOrder.valueOf(request.getParameter("sortOrder"));
                createSectionsAndForward(request, response, sortOrder);
            } else {
                playlist.addAll(selectedFiles);
                if (finalSelection) {
                    String channel = request.getParameter("channel");
                    if (StringUtils.isEmpty(channel)) {
                        String channelPattern = ResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRSSWeb").getString("rss.channel.default_name");
                        channel = MessageFormat.format(channelPattern, System.getProperty("mytunesrss.version"));
                    }
                    Map<String, String> urls = (Map<String, String>)request.getSession().getAttribute("urlMap");
                    StringBuffer url = new StringBuffer(urls.get("rss")).append("/ch=").append(channel);
                    for (MusicFile musicFile : playlist) {
                        url.append("/id=").append(musicFile.getId());
                    }
                    if (StringUtils.isNotEmpty((String)request.getSession().getAttribute("authHash"))) {
                        url.append("/au=").append(request.getSession().getAttribute("authHash"));
                    }
                    response.sendRedirect(url.toString());
                } else {
                    request.getRequestDispatcher("/search.jsp").forward(request, response);
                }
            }
        } else {
            request.setAttribute("error", "error.must_select_one_song");
            SortOrder sortOrder = SortOrder.valueOf(request.getParameter("sortOrder"));
            createSectionsAndForward(request, response, sortOrder);
        }
    }
}