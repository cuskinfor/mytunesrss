/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.InsertTrackStatement;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * de.codewave.mytunesrss.command.GetZipArchiveCommandHandler
 */
public class GetZipArchiveCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isRequestAuthorized() && getAuthUser().isDownload() && !getAuthUser().isQuotaExceeded()) {
            try {
            String baseName = getRequest().getPathInfo();
            baseName = baseName.substring(baseName.lastIndexOf("/") + 1, baseName.lastIndexOf("."));
            String tracklist = getRequestParameter("tracklist", null);
            DataStoreQuery.QueryResult<Track> tracks;
            if (StringUtils.isNotEmpty(tracklist)) {
                tracks = getTransaction().executeQuery(FindTrackQuery.getForId(StringUtils.split(tracklist, ",")));
            } else {
                tracks = getTransaction().executeQuery(TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), true));
            }
            if (MyTunesRss.CONFIG.isLocalTempArchive()) {
                File tempFile = File.createTempFile("MyTunesRSS_", null);
                tempFile.deleteOnExit();
                try {
                    createZipArchive(new FileOutputStream(tempFile), tracks, baseName, null);
                    FileSender fileSender = new FileSender(tempFile, "application/zip", (int)tempFile.length());
                    fileSender.setCounter((FileSender.ByteSentCounter)SessionManager.getSessionInfo(getRequest()));
                    fileSender.setOutputStreamWrapper(getAuthUser().getOutputStreamWrapper(0));
                    fileSender.sendGetResponse(getRequest(), getResponse(), false);
                } finally {
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            } else {
                getResponse().setContentType("application/zip");
                OutputStream outputStream = getAuthUser().getOutputStreamWrapper(0).wrapStream(getResponse().getOutputStream());
                createZipArchive(outputStream, tracks, baseName, (FileSender.ByteSentCounter)SessionManager.getSessionInfo(
                        getRequest()));
            }
            } finally {
                getSession().setMaxInactiveInterval(getAuthUser().getSessionTimeout() * 60); // reset correct session timeout
            }
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void createZipArchive(OutputStream outputStream, DataStoreQuery.QueryResult<Track> tracks, String baseName, FileSender.ByteSentCounter counter)
            throws IOException {
        ZipOutputStream zipStream = new ZipOutputStream(outputStream);
        zipStream.setComment("MyTunesRSS v" + MyTunesRss.VERSION + " (http://www.codewave.de)");
        byte[] buffer = new byte[102400];
        Set<String> entryNames = new HashSet<String>();
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder m3uPlaylist = new StringBuilder("#EXTM3U").append(lineSeparator);
        int trackCount = 0;
        boolean quotaExceeded = false;
        for (Track track = tracks.nextResult(); track != null; track = tracks.nextResult()) {
            getSession().setMaxInactiveInterval(-1); // keep unlimited session until finished
            if (track.getFile().exists() && !getAuthUser().isQuotaExceeded()) {
                String trackArtist = track.getArtist();
                if (trackArtist.equals(InsertTrackStatement.UNKNOWN)) {
                    trackArtist = "unknown";
                }
                String trackAlbum = track.getAlbum();
                if (trackAlbum.equals(InsertTrackStatement.UNKNOWN)) {
                    trackAlbum = "unknown";
                }
                int number = 1;
                String entryNameWithoutSuffix =
                        MyTunesFunctions.getLegalFileName(trackArtist) + "/" + MyTunesFunctions.getLegalFileName(trackAlbum) + "/";
                if (track.getTrackNumber() > 0) {
                    entryNameWithoutSuffix += StringUtils.leftPad(Integer.toString(track.getTrackNumber()), 2, "0") + " ";
                }
                entryNameWithoutSuffix += MyTunesFunctions.getLegalFileName(track.getName());
                String entryName = entryNameWithoutSuffix + "." + FilenameUtils.getExtension(track.getFile().getName());
                while (entryNames.contains(entryName)) {
                    entryName = entryNameWithoutSuffix + " " + number + "." + FilenameUtils.getExtension(track.getFile().getName());
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
                m3uPlaylist.append("#EXTINF:").append(track.getTime()).append(",").append(trackArtist).append(" - ").append(track.getName())
                        .append(lineSeparator);
                m3uPlaylist.append(entryName).append(lineSeparator);
                trackCount++;
                if (counter != null) {
                    counter.add((int)entry.getCompressedSize());
                }
            } else if (getAuthUser().isQuotaExceeded()) {
                quotaExceeded = true;
            }
        }
        if (trackCount > 0) {
            ZipEntry m3uPlaylistEntry = new ZipEntry(baseName + "/" + baseName + ".m3u");
            zipStream.putNextEntry(m3uPlaylistEntry);
            zipStream.write(m3uPlaylist.toString().getBytes("UTF-8"));
            zipStream.closeEntry();
            if (counter != null) {
                counter.add((int)m3uPlaylistEntry.getCompressedSize());
            }
        }
        if (quotaExceeded) {
            ZipEntry quotaExceededInfoEntry = new ZipEntry(baseName + "/Readme.txt");
            zipStream.putNextEntry(quotaExceededInfoEntry);
            zipStream.write("This archive is not complete since your download limit has been reached!".getBytes("UTF-8"));
            zipStream.closeEntry();
            if (counter != null) {
                counter.add((int)quotaExceededInfoEntry.getCompressedSize());
            }
        } else if (trackCount == 0) {
            ZipEntry noFilesInfoEntry = new ZipEntry(baseName + "/Readme.txt");
            zipStream.putNextEntry(noFilesInfoEntry);
            zipStream.write("This archive does not contain any files from MyTunesRSS! If you think it should, please contact the MyTunesRSS server administrator or - in case you are the administrator - contact the Codewave support.".getBytes("UTF-8"));
            zipStream.closeEntry();
            if (counter != null) {
                counter.add((int)noFilesInfoEntry.getCompressedSize());
            }
        }
        zipStream.close();
    }

}