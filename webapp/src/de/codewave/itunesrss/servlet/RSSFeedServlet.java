/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.itunesrss.servlet;

import org.apache.commons.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.text.*;

import de.codewave.itunesrss.jsp.*;
import de.codewave.itunesrss.musicfile.*;
import de.codewave.itunesrss.itunes.*;

public class RSSFeedServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String methodName = request.getParameter("method");
        request.setAttribute("servletUrl", request.getRequestURL());
        if (StringUtils.isEmpty(methodName)) {
            request.getRequestDispatcher("/search.jsp").forward(request, response);
        } else {
            try {
                Method method = getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
                method.invoke(this, request, response);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    public void executeSearch(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String albumPattern = request.getParameter("album");
        String artistPattern = request.getParameter("artist");
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
        List<MusicFile> matchingFiles = library.getMatchingFiles(new MusicFileAlbumSearch(albumPattern), new MusicFileArtistSearch(artistPattern));
        if (matchingFiles != null && !matchingFiles.isEmpty()) {
            request.getSession().setAttribute("musicFiles", matchingFiles);
            Collection<String> selection = new ArrayList<String>(matchingFiles.size());
            for (Iterator<MusicFile> iterMusicFiles = matchingFiles.iterator(); iterMusicFiles.hasNext();) {
                MusicFile musicFile = iterMusicFiles.next();
                selection.add(musicFile.getId());
            }
            if (!StringUtils.isEmpty(albumPattern)) {
                sortResultsByAlbum(request, response, selection);
            } else {
                sortResultsByArtist(request, response, selection);
            }
        } else {
            request.setAttribute("error", "No matching songs found!");
            request.getRequestDispatcher("/search.jsp").forward(request, response);
        }
    }

    public void getRssFeed(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
        List<MusicFile> feedFiles = new ArrayList<MusicFile>();
        for (String id : getRequestSelection(request)) {
            feedFiles.addAll(library.getMatchingFiles(new MusicFileIdSearch(id)));
        }
        request.setAttribute("musicFiles", feedFiles);
        String channel = request.getParameter("channel");
        if (StringUtils.isEmpty(channel)) {
            channel = "Codewave PSP-RSS Feeder";
        }
        request.setAttribute("channel", channel);
        request.setAttribute("pubDate", new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US).format(new Date()));
        request.getRequestDispatcher("/rss.jsp").forward(request, response);
    }

    public void sortResultsByAlbum(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Collection<String> selection = getRequestSelection(request);
        sortResultsByAlbum(request, response, selection);
    }

    private void sortResultsByAlbum(HttpServletRequest request, HttpServletResponse response, Collection<String> selection)
            throws ServletException, IOException {
        List<MusicFile> musicFiles = (List<MusicFile>)request.getSession().getAttribute("musicFiles");
        Collections.sort(musicFiles, new Comparator<MusicFile>() {
            public int compare(MusicFile o1, MusicFile o2) {
                int value = o1.getAlbum().compareTo(o2.getAlbum());
                if (value == 0) {
                    value = o1.getTrackNumber() - o2.getTrackNumber();
                }
                return value;
            }
        });
        List<Section> sections = new ArrayList<Section>();
        Section section = null;
        String album = null;
        for (MusicFile musicFile : musicFiles) {
            if (!musicFile.getAlbum().equals(album)) {
                section = new Section();
                sections.add(section);
                album = musicFile.getAlbum();
            }
            section.addItem(new SectionItem(musicFile, selection.contains(musicFile.getId())));
        }
        request.getSession().setAttribute("sections", sections);
        request.setAttribute("selection", selection);
        request.getRequestDispatcher("/select.jsp").forward(request, response);
    }

    public void sortResultsByArtist(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Collection<String> selection = getRequestSelection(request);
        sortResultsByArtist(request, response, selection);
    }

    private void sortResultsByArtist(HttpServletRequest request, HttpServletResponse response, Collection<String> selection)
            throws ServletException, IOException {
        List<MusicFile> musicFiles = (List<MusicFile>)request.getSession().getAttribute("musicFiles");
        Collections.sort(musicFiles, new Comparator<MusicFile>() {
            public int compare(MusicFile o1, MusicFile o2) {
                int value = o1.getArtist().compareTo(o2.getArtist());
                if (value == 0) {
                    value = o1.getAlbum().compareTo(o2.getAlbum());
                    if (value == 0) {
                        value = o1.getTrackNumber() - o2.getTrackNumber();
                    }
                }
                return value;
            }
        });
        List<Section> sections = new ArrayList<Section>();
        Section section = null;
        String artist = null;
        for (MusicFile musicFile : musicFiles) {
            if (!musicFile.getArtist().equals(artist)) {
                section = new Section();
                sections.add(section);
                artist = musicFile.getArtist();
            }
            section.addItem(new SectionItem(musicFile, selection.contains(musicFile.getId())));
        }
        request.getSession().setAttribute("sections", sections);
        request.setAttribute("selection", selection);
        request.getRequestDispatcher("/select.jsp").forward(request, response);
    }

    private Collection<String> getRequestSelection(HttpServletRequest request) {
        String[] selection = request.getParameterValues("id");
        if (selection != null) {
            return Arrays.asList(selection);
        }
        return Collections.emptyList();
    }
}
