/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.server.*;
import de.codewave.utils.*;
import de.codewave.utils.io.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.lang.*;

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
            String album = Base64Utils.decodeToString(getRequestParameter("album", null));
            String artist = Base64Utils.decodeToString(getRequestParameter("artist", null));
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
            zipStream.setComment("MyTunesRSS v" + MyTunesRss.VERSION + " (http://www.mytunesrss.com)");
            byte[] buffer = new byte[102400];
            MyTunesRssSessionInfo sessionInfo = (MyTunesRssSessionInfo)SessionManager.getSessionInfo(getRequest());
            for (Track track : tracks) {
                String trackArtist = track.getArtist();
                if (trackArtist.equals(InsertTrackStatement.UNKNOWN)) {
                    trackArtist = "unknown";
                }
                String trackAlbum = track.getAlbum();
                if (trackAlbum.equals(InsertTrackStatement.UNKNOWN)) {
                    trackAlbum = "unknown";
                }
                ZipEntry entry = new ZipEntry(trackArtist + "/" + trackAlbum + "/" + track.getName() + "." + IOUtils.getSuffix(track.getFile()));
                zipStream.putNextEntry(entry);
                InputStream file = new FileInputStream(track.getFile());
                for (int length = file.read(buffer); length >= 0; length = file.read(buffer)) {
                    if (length > 0) {
                        zipStream.write(buffer, 0, length);
                    }
                }
                file.close();
                zipStream.closeEntry();
                sessionInfo.addBytesStreamed(entry.getCompressedSize());
            }
            zipStream.close();
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}