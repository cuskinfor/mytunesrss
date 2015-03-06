/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssSendCounter;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.SessionManager.SessionInfo;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.GetZipArchiveCommandHandler
 */
public class GetZipArchiveCommandHandler extends BandwidthThrottlingCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetZipArchiveCommandHandler.class);

    private static final long ARCHIVE_CACHE_TIMEOUT = 1000L * 60L * 60L; // timeout = 60 minutes

    @Override
    public void executeAuthorized() throws Exception {
        User user = getAuthUser();
        if (isRequestAuthorized() && user.isDownload()) {
            String baseName = getRequestParameter("_cda", "MyTunesRSS");
            baseName = MyTunesRssUtils.getLegalFileName(baseName.substring(0, baseName.lastIndexOf(".")));
            String tracklist = getRequestParameter("tracklist", null);
            QueryResult<Track> tracks;
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
                    cachedFile = new File(MyTunesRss.TEMP_CACHE.getBaseDir(), fileIdentifier);
                } else {
                    LOGGER.debug("Result set has \"" + tracks.getResultSize() + "\" results which is too much for archive file ID generation.");
                }
                File tempFile = null;
                if (cachedFile == null || !cachedFile.isFile()) {
                    LOGGER.debug("No archive with ID \"" + fileIdentifier + "\" found in cache.");
                    tempFile = File.createTempFile("mytunesrss_", ".zip", MyTunesRss.TEMP_CACHE.getBaseDir());
                    try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                        createZipArchive(user, outputStream, tracks, baseName, null);
                    } catch (RuntimeException e) {
                        if (!tempFile.delete()) {
                            LOGGER.debug("Could not delete file \"" + tempFile.getAbsolutePath() + "\".");
                        }
                        throw e;
                    }
                } else {
                    LOGGER.debug("Using archive with ID \"" + fileIdentifier + "\" from cache.");
                }
                File sendFile = cachedFile != null && cachedFile.isFile() ? cachedFile : tempFile;
                FileSender fileSender = new FileSender(sendFile, "application/zip", sendFile.length());
                fileSender.setCounter(new MyTunesRssSendCounter(user, sessionInfo));
                fileSender.sendGetResponse(getRequest(), getResponse(), false);
            } else {
                getResponse().setContentType("application/zip");
                createZipArchive(user, getResponse().getOutputStream(), tracks, baseName, new MyTunesRssSendCounter(user, sessionInfo));
            }
        } else {
            getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
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
        List<String> trackIds = new ArrayList<>();
        for (Track track = tracks.nextResult(); track != null; track = tracks.nextResult()) {
            trackIds.add(track.getId());
        }
        tracks.reset();
        Collections.sort(trackIds);
        return Long.toString(StringUtils.join(trackIds, "").hashCode());
    }

    private void createZipArchive(User user, OutputStream outputStream, QueryResult<Track> tracks, String baseName,
                                  FileSender.ByteSentCounter counter) throws IOException {
        ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(outputStream);
        zipStream.setLevel(ZipArchiveOutputStream.STORED);
        zipStream.setComment("MyTunesRSS v" + MyTunesRss.VERSION + " (http://www.codewave.de)");
        byte[] buffer = new byte[102400];
        Set<String> entryNames = new HashSet<>();
        PlaylistBuilder playlistBuilder = WebConfig.PlaylistType.Xspf.name().equals(getWebConfig().getPlaylistType()) ? new XspfPlaylistsBuilder() : new M3uPlaylistsBuilder();

        int trackCount = 0;
        boolean quotaExceeded = false;
        for (Track track = tracks.nextResult(); track != null; track = tracks.nextResult()) {
            if (track.getFile().exists() && !user.isQuotaExceeded()) {
                String trackArtist = track.getArtist();
                if (MyTunesRssUtils.isUnknown(trackArtist)) {
                    trackArtist = "Unknown artist";
                }
                String trackAlbum = track.getAlbum();
                if (MyTunesRssUtils.isUnknown(trackAlbum)) {
                    trackAlbum = "Unknown Album";
                }
                int number = 1;
                String entryNameWithoutSuffix = StringUtils.strip(MyTunesRssUtils.getLegalFileName(trackArtist), ".") + "/" + StringUtils.strip(
                        MyTunesRssUtils.getLegalFileName(trackAlbum),
                        ".") + "/";
                if (track.getTrackNumber() > 0) {
                    entryNameWithoutSuffix += StringUtils.leftPad(Integer.toString(track.getTrackNumber()), 2, "0") + " ";
                }
                entryNameWithoutSuffix += StringUtils.strip(MyTunesRssUtils.getLegalFileName(track.getName()), ".");
                String entryName = entryNameWithoutSuffix + "." + FilenameUtils.getExtension(track.getFile().getName());
                while (entryNames.contains(entryName)) {
                    entryName = entryNameWithoutSuffix + " " + number + "." + FilenameUtils.getExtension(track.getFile().getName());
                    number++;
                }
                // media file
                entryNames.add(entryName);
                ZipArchiveEntry entry = new ZipArchiveEntry(baseName + "/" + entryName);
                zipStream.putArchiveEntry(entry);
                try (InputStream file = new FileInputStream(track.getFile())) {
                    for (int length = file.read(buffer); length >= 0; length = file.read(buffer)) {
                        if (length > 0) {
                            zipStream.write(buffer, 0, length);
                        }
                    }
                }
                zipStream.closeArchiveEntry();
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
            ZipArchiveEntry m3uPlaylistEntry = new ZipArchiveEntry(baseName + "/" + baseName + "." + playlistBuilder.getSuffix());
            zipStream.putArchiveEntry(m3uPlaylistEntry);
            zipStream.write(playlistBuilder.toString().getBytes("UTF-8"));
            zipStream.closeArchiveEntry();
            if (counter != null) {
                counter.add((int) m3uPlaylistEntry.getCompressedSize());
            }
        }
        if (quotaExceeded) {
            ZipArchiveEntry quotaExceededInfoEntry = new ZipArchiveEntry(baseName + "/Readme.txt");
            zipStream.putArchiveEntry(quotaExceededInfoEntry);
            zipStream.write("This archive is not complete since your download limit has been reached!".getBytes("UTF-8"));
            zipStream.closeArchiveEntry();
            if (counter != null) {
                counter.add((int) quotaExceededInfoEntry.getCompressedSize());
            }
        } else if (trackCount == 0) {
            ZipArchiveEntry noFilesInfoEntry = new ZipArchiveEntry(baseName + "/Readme.txt");
            zipStream.putArchiveEntry(noFilesInfoEntry);
            zipStream.write(
                    "This archive does not contain any files from MyTunesRSS! If you think it should, please contact the MyTunesRSS server administrator or - in case you are the administrator - contact Codewave support.".getBytes(
                            "UTF-8"));
            zipStream.closeArchiveEntry();
            if (counter != null) {
                counter.add((int) noFilesInfoEntry.getCompressedSize());
            }
        }
        zipStream.close();
    }

    private interface PlaylistBuilder {
        void add(Track track, String trackArtist, String trackAlbum, String entryName);
        String getSuffix();
    }

    private static class M3uPlaylistsBuilder implements PlaylistBuilder {

        private String lineSeparator = System.getProperty("line.separator");

        private StringBuilder playlist = new StringBuilder().append("#EXTM3U").append(lineSeparator);

        @Override
        public void add(Track track, String trackArtist, String trackAlbum, String entryName) {
            playlist.append("#EXTINF:").append(track.getTime()).append(",").append(trackArtist).append(" - ").append(track.getName()).append(
                    lineSeparator);
            playlist.append(entryName).append(lineSeparator);
        }

        public String toString() {
            return playlist.toString();
        }

        @Override
        public String getSuffix() {
            return "m3u";
        }
    }

    private static class XspfPlaylistsBuilder implements PlaylistBuilder {

        private String lineSeparator = System.getProperty("line.separator");

        private StringBuilder playlist = new StringBuilder();

        private XspfPlaylistsBuilder() {
            playlist.append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">").append(lineSeparator);
            playlist.append("  <creator>Codewave MyTunesRSS</creator>").append(lineSeparator);
            playlist.append("  <info>http://www.codewave.de</info>").append(lineSeparator);
            playlist.append("  <trackList>").append(lineSeparator);
        }

        @Override
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
            return playlist + lineSeparator + "  </tracklist>" + lineSeparator + "</playlist>" + lineSeparator;
        }

        @Override
        public String getSuffix() {
            return "xspf";
        }
    }


}
