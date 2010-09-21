/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.InsertTrackStatement;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.io.FileCache;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.SessionManager.SessionInfo;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreQuery.QueryResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.command.GetZipArchiveCommandHandler
 */
public class GetZipArchiveCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetZipArchiveCommandHandler.class);

    private static final long ARCHIVE_CACHE_TIMEOUT = 1000L * 60L * 60L; // timeout = 60 minutes

    @Override
    public void executeAuthorized() throws Exception {
        User user = getAuthUser();
        if (isRequestAuthorized() && user.isDownload() && !user.isQuotaExceeded()) {
            String baseName = getRequest().getPathInfo();
            baseName = baseName.substring(baseName.lastIndexOf("/") + 1, baseName.lastIndexOf("."));
            String tracklist = getRequestParameter("tracklist", null);
            DataStoreQuery.QueryResult<Track> tracks;
            if (StringUtils.isNotEmpty(tracklist)) {
                tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(StringUtils.split(tracklist, ",")));
            } else {
                tracks = getTransaction().executeQuery(TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), user, true));
            }
            SessionInfo sessionInfo = SessionManager.getSessionInfo(getRequest());
            if (MyTunesRss.CONFIG.isLocalTempArchive()) {
                File cachedFile = null;
                String fileIdentifier = null;
                if (tracks.getResultSize() < 10000) {
                    fileIdentifier = calculateIdentifier(tracks);
                    LOGGER.debug("Archive file ID is \"" + fileIdentifier + "\".");
                    FileCache.FileInfo fileInfo = MyTunesRss.TEMP_CACHE.get(fileIdentifier);
                    cachedFile = fileInfo != null ? fileInfo.getFile() : null;
                } else {
                    LOGGER.debug("Result set has \"" + tracks.getResultSize() + "\" results which is too much for archive file ID generation.");
                }
                File tempFile = null;
                try {
                    if (cachedFile == null || !cachedFile.isFile()) {
                        LOGGER.debug("No archive with ID \"" + fileIdentifier + "\" found in cache.");
                        tempFile = File.createTempFile("mytunesrss_", ".zip", new File(MyTunesRssUtils.getCacheDataPath(), MyTunesRss.CACHEDIR_TEMP));
                        try {
                            createZipArchive(user, new FileOutputStream(tempFile), tracks, baseName, null);
                        } catch (Exception e) {
                            tempFile.delete();
                            throw e;
                        }
                        MyTunesRss.TEMP_CACHE.add(fileIdentifier, tempFile, ARCHIVE_CACHE_TIMEOUT); // TODO timeout from config?
                    } else {
                        LOGGER.debug("Using archive with ID \"" + fileIdentifier + "\" from cache.");
                    }
                    File sendFile = cachedFile != null && cachedFile.isFile() ? cachedFile : tempFile;
                    FileSender fileSender = new FileSender(sendFile, "application/zip", sendFile.length());
                    fileSender.setCounter(new MyTunesRssSendCounter(user, sessionInfo));
                    fileSender.setOutputStreamWrapper(user.getOutputStreamWrapper(0));
                    fileSender.sendGetResponse(getRequest(), getResponse(), false);
                } finally {
                    /*if (tempFile != null && tempFile.isFile()) {
                        tempFile.delete();
                    }*/
                }
            } else {
                getResponse().setContentType("application/zip");
                OutputStream outputStream = user.getOutputStreamWrapper(0).wrapStream(getResponse().getOutputStream());
                createZipArchive(user, outputStream, tracks, baseName, new MyTunesRssSendCounter(user, sessionInfo));
            }
        } else {
            if (user.isQuotaExceeded()) {
                MyTunesRss.ADMIN_NOTIFY.notifyQuotaExceeded(user);
            }
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    /**
     * Calculate an identifier for a list of tracks.
     *
     * @param tracks A list of tracks.
     *
     * @return Identifier for the list of tracks.
     */
    private String calculateIdentifier(QueryResult<Track> tracks) {
        List<String> trackIds = new ArrayList<String>();
        for (Track track = tracks.nextResult(); track != null; track = tracks.nextResult()) {
            trackIds.add(track.getId());
        }
        tracks.reset();
        Collections.sort(trackIds);
        return Long.toString(StringUtils.join(trackIds, "").hashCode());
    }

    private void createZipArchive(User user, OutputStream outputStream, DataStoreQuery.QueryResult<Track> tracks, String baseName,
                                  FileSender.ByteSentCounter counter) throws IOException, SQLException {
        ZipOutputStream zipStream = new ZipOutputStream(outputStream);
        zipStream.setLevel(ZipOutputStream.STORED);
        zipStream.setComment("MyTunesRSS v" + MyTunesRss.VERSION + " (http://www.codewave.de)");
        byte[] buffer = new byte[102400];
        Set<String> entryNames = new HashSet<String>();
        PlaylistBuilder playlistBuilder = WebConfig.PlaylistType.Xspf.name().equals(getWebConfig().getPlaylistType()) ? new XspfPlaylistsBuilder() : new M3uPlaylistsBuilder();

        int trackCount = 0;
        boolean quotaExceeded = false;
        for (Track track = tracks.nextResult(); track != null; track = tracks.nextResult()) {
            if (track.getFile().exists() && !user.isQuotaExceeded()) {
                String trackArtist = track.getArtist();
                if (trackArtist.equals(InsertTrackStatement.UNKNOWN)) {
                    trackArtist = "unknown";
                }
                String trackAlbum = track.getAlbum();
                if (trackAlbum.equals(InsertTrackStatement.UNKNOWN)) {
                    trackAlbum = "unknown";
                }
                int number = 1;
                String entryNameWithoutSuffix = StringUtils.strip(MyTunesFunctions.getLegalFileName(trackArtist), ".") + "/" + StringUtils.strip(
                        MyTunesFunctions.getLegalFileName(trackAlbum),
                        ".") + "/";
                if (track.getTrackNumber() > 0) {
                    entryNameWithoutSuffix += StringUtils.leftPad(Integer.toString(track.getTrackNumber()), 2, "0") + " ";
                }
                entryNameWithoutSuffix += StringUtils.strip(MyTunesFunctions.getLegalFileName(track.getName()), ".");
                String entryName = entryNameWithoutSuffix + "." + FilenameUtils.getExtension(track.getFile().getName());
                while (entryNames.contains(entryName)) {
                    entryName = entryNameWithoutSuffix + " " + number + "." + FilenameUtils.getExtension(track.getFile().getName());
                    number++;
                }
                // media file
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
                // image
//                if (track.getMediaType() != MediaType.Image && StringUtils.isNotEmpty(track.getImageHash())) {
//                    byte[] data = getTransaction().executeQuery(new FindImageQuery(track.getImageHash(), 256));
//                    if (data != null) {
//                        entry = new ZipEntry(baseName + "/" + entryName + ".jpg");
//                        zipStream.putNextEntry(entry);
//                        zipStream.write(data);
//                        file.close();
//                    }
//                }
                zipStream.closeEntry();
                playlistBuilder.add(track, trackArtist, trackAlbum, entryName);
                trackCount++;
                if (counter != null) {
                    counter.add((int) entry.getCompressedSize());
                }
            } else if (user.isQuotaExceeded()) {
                quotaExceeded = true;
                MyTunesRss.ADMIN_NOTIFY.notifyQuotaExceeded(user);
            }
        }
        if (trackCount > 0) {
            ZipEntry m3uPlaylistEntry = new ZipEntry(baseName + "/" + baseName + "." + playlistBuilder.getSuffix());
            zipStream.putNextEntry(m3uPlaylistEntry);
            zipStream.write(playlistBuilder.toString().getBytes("UTF-8"));
            zipStream.closeEntry();
            if (counter != null) {
                counter.add((int) m3uPlaylistEntry.getCompressedSize());
            }
        }
        if (quotaExceeded) {
            ZipEntry quotaExceededInfoEntry = new ZipEntry(baseName + "/Readme.txt");
            zipStream.putNextEntry(quotaExceededInfoEntry);
            zipStream.write("This archive is not complete since your download limit has been reached!".getBytes("UTF-8"));
            zipStream.closeEntry();
            if (counter != null) {
                counter.add((int) quotaExceededInfoEntry.getCompressedSize());
            }
        } else if (trackCount == 0) {
            ZipEntry noFilesInfoEntry = new ZipEntry(baseName + "/Readme.txt");
            zipStream.putNextEntry(noFilesInfoEntry);
            zipStream.write(
                    "This archive does not contain any files from MyTunesRSS! If you think it should, please contact the MyTunesRSS server administrator or - in case you are the administrator - contact Codewave support.".getBytes(
                            "UTF-8"));
            zipStream.closeEntry();
            if (counter != null) {
                counter.add((int) noFilesInfoEntry.getCompressedSize());
            }
        }
        zipStream.close();
    }

    private static interface PlaylistBuilder {
        void add(Track track, String trackArtist, String trackAlbum, String entryName);
        String getSuffix();
    }

    private static class M3uPlaylistsBuilder implements PlaylistBuilder {

        private String lineSeparator = System.getProperty("line.separator");

        private StringBuilder playlist = new StringBuilder().append("#EXTM3U").append(lineSeparator);

        public void add(Track track, String trackArtist, String trackAlbum, String entryName) {
            playlist.append("#EXTINF:").append(track.getTime()).append(",").append(trackArtist).append(" - ").append(track.getName()).append(
                    lineSeparator);
            playlist.append(entryName).append(lineSeparator);
        }

        public String toString() {
            return playlist.toString();
        }

        public String getSuffix() {
            return "m3u";
        }
    }

    private class XspfPlaylistsBuilder implements PlaylistBuilder {

        private String lineSeparator = System.getProperty("line.separator");

        private StringBuilder playlist = new StringBuilder();

        private XspfPlaylistsBuilder() {
            playlist.append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">").append(lineSeparator);
            playlist.append("  <creator>Codewave MyTunesRSS</creator>").append(lineSeparator);
            playlist.append("  <info>http://www.codewave.de</info>").append(lineSeparator);
            playlist.append("  <trackList>").append(lineSeparator);
        }

        public void add(Track track, String trackArtist, String trackAlbum, String entryName) {
            playlist.append("      <track>").append(lineSeparator);
            playlist.append("        <location>file:").append(StringEscapeUtils.escapeXml(entryName)).append("</location>").append(lineSeparator);
            playlist.append("        <creator>").append(StringEscapeUtils.escapeXml(trackArtist)).append("</creator>").append(lineSeparator);
            playlist.append("        <album>").append(StringEscapeUtils.escapeXml(trackAlbum)).append("</album>").append(lineSeparator);
            playlist.append("        <title>").append(StringEscapeUtils.escapeXml(track.getName())).append("</title>").append(lineSeparator);
            if (StringUtils.isNotEmpty(track.getGenre())) {
                playlist.append("        <annotation>").append(StringEscapeUtils.escapeXml(track.getGenre())).append("</annotation>").append(lineSeparator);
            }
            playlist.append("        <duration>").append(track.getTime() * 1000).append("</duration>").append(lineSeparator);
//            if (track.getMediaType() != MediaType.Image && StringUtils.isNotEmpty(track.getImageHash())) {
//                playlist.append("        <image>").append(StringEscapeUtils.escapeXml(entryName + ".jpg")).append("</image>").append(lineSeparator);
//            }
            playlist.append("      </track>").append(lineSeparator);
        }

        public String toString() {
            return playlist.toString() + lineSeparator + "  </tracklist>" + lineSeparator + "</playlist>" + lineSeparator;
        }

        public String getSuffix() {
            return "xspf";
        }
    }


}