/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.server.*;
import de.codewave.utils.*;
import de.codewave.utils.jsp.CodewaveFunctions;
import de.codewave.utils.io.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.lang.*;
import org.apache.commons.jxpath.ClassFunctions;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * de.codewave.mytunesrss.command.GetZipArchiveCommandHandler
 */
public class GetZipArchiveCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (getAuthUser().isDownload()) {
            String baseName = getRequest().getPathInfo();
            baseName = baseName.substring(baseName.lastIndexOf("/") + 1, baseName.lastIndexOf("."));
            String album = MyTunesRssBase64Utils.decodeToString(getRequestParameter("album", null));
            String artist = MyTunesRssBase64Utils.decodeToString(getRequestParameter("artist", null));
            String playlist = getRequestParameter("playlist", null);
            Collection<Track> tracks;
            if (StringUtils.isNotEmpty(album)) {
                tracks = getDataStore().executeQuery(FindTrackQuery.getForAlbum(new String[] {album}, true));
            } else if (StringUtils.isNotEmpty(artist)) {
                tracks = getDataStore().executeQuery(FindTrackQuery.getForArtist(new String[] {artist}, true));
            } else {
                tracks = getDataStore().executeQuery(new FindPlaylistTracksQuery(playlist));
            }
            getResponse().setContentType("application/zip");
            ZipOutputStream zipStream = new ZipOutputStream(getResponse().getOutputStream());
            zipStream.setComment("MyTunesRSS v" + MyTunesRss.VERSION + " (http://www.codewave.de)");
            byte[] buffer = new byte[102400];
            MyTunesRssSessionInfo sessionInfo = (MyTunesRssSessionInfo)SessionManager.getSessionInfo(getRequest());
            Set<String> entryNames = new HashSet<String>();
            String lineSeparator = System.getProperty("line.separator");
            StringBuffer m3uPlaylist = new StringBuffer("#EXTM3U").append(lineSeparator);
            for (Track track : tracks) {
                String trackArtist = track.getArtist();
                if (trackArtist.equals(InsertTrackStatement.UNKNOWN)) {
                    trackArtist = "unknown";
                }
                String trackAlbum = track.getAlbum();
                if (trackAlbum.equals(InsertTrackStatement.UNKNOWN)) {
                    trackAlbum = "unknown";
                }
                int number = 1;
                String entryNameWithoutSuffix = MyTunesFunctions.getLegalFileName(trackArtist) + "/" + MyTunesFunctions.getLegalFileName(trackAlbum) + "/";
                if (track.getTrackNumber() > 0) {
                    entryNameWithoutSuffix += StringUtils.leftPad(Integer.toString(track.getTrackNumber()), 2, "0") + " ";
                }
                entryNameWithoutSuffix += MyTunesFunctions.getLegalFileName(track.getName());
                String entryName = entryNameWithoutSuffix + "." + IOUtils.getSuffix(track.getFile());
                while (entryNames.contains(entryName)) {
                    entryName = entryNameWithoutSuffix + " " + number + "." + IOUtils.getSuffix(track.getFile());
                    number++;
                }
                entryNames.add(entryName);
                ZipEntry entry = new ZipEntry(baseName + "/" + entryName);
                zipStream.putNextEntry(entry);
                InputStream file = new FileInputStream(track.getFile());
                for (int length = file.read(buffer); length >= 0; length = file.read(buffer)) {
                    if (length > 0) {
                        zipStream.write(buffer, 0, length);
                    }
                }
                file.close();
                zipStream.closeEntry();
                m3uPlaylist.append("#EXTINF:").append(track.getTime()).append(",").append(trackArtist).append(" - ").append(track.getName()).append(lineSeparator);
                m3uPlaylist.append(entryName).append(lineSeparator);
                sessionInfo.addBytesStreamed(entry.getCompressedSize());
            }
            ZipEntry m3uPlaylistEntry = new ZipEntry(baseName + "/" + baseName + ".m3u");
            zipStream.putNextEntry(m3uPlaylistEntry);
            zipStream.write(m3uPlaylist.toString().getBytes());
            zipStream.closeEntry();
            zipStream.close();
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

}