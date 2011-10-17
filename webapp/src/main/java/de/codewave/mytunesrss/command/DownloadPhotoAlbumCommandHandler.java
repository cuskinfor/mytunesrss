/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssSendCounter;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.utils.io.ZipUtils;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.GetZipArchiveCommandHandler
 */
public class DownloadPhotoAlbumCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadPhotoAlbumCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        User user = getAuthUser();
        if (isRequestAuthorized() && user.isDownloadPhotoAlbum()) {
            String photoAlbumId = MyTunesRssBase64Utils.decodeToString(getRequestParameter("photoalbumid", null));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Getting photos for album with ID \"" + photoAlbumId + "\".");
            }
            DataStoreQuery.QueryResult<Photo> photoResult = getTransaction().executeQuery(FindPhotoQuery.getForAlbum(getAuthUser(), photoAlbumId));
            getResponse().setContentType("application/zip");
            getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + MyTunesRssBase64Utils.decodeToString(getRequestParameter("photoalbum", "cGhvdG9z")) + ".zip\""); // cGhvdG9z => photos
            createZipArchive(getResponse().getOutputStream(), photoResult.getResults(), new MyTunesRssSendCounter(user, SessionManager.getSessionInfo(getRequest())));
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void createZipArchive(OutputStream outputStream, List<Photo> results, MyTunesRssSendCounter sendCounter) throws IOException {
        ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(outputStream);
        try {
            zipStream.setLevel(ZipArchiveOutputStream.STORED);
            zipStream.setComment("MyTunesRSS v" + MyTunesRss.VERSION + " (http://www.codewave.de)");
            for (Photo photo : results) {
                File photoFile = new File(photo.getFile());
                if (photoFile.isFile()) {
                    FileInputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(photoFile);
                        ZipArchiveEntry archiveEntry = new ZipArchiveEntry(photoFile.getName());
                        archiveEntry.setSize(photoFile.length());
                        archiveEntry.setTime(photoFile.lastModified());
                        zipStream.putArchiveEntry(archiveEntry);
                        IOUtils.copy(inputStream, zipStream);
                        zipStream.closeArchiveEntry();
                        if (sendCounter != null) {
                            sendCounter.add((int) archiveEntry.getCompressedSize());
                        }
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                } else {
                    byte[] textFile = "Image file was not found!".getBytes("UTF-8");
                    ZipArchiveEntry archiveEntry = new ZipArchiveEntry(photoFile.getName() + ".txt");
                    archiveEntry.setSize(textFile.length);
                    archiveEntry.setTime(System.currentTimeMillis());
                    zipStream.putArchiveEntry(archiveEntry);
                    zipStream.write(textFile);
                    zipStream.closeArchiveEntry();
                }
            }
        } finally {
            zipStream.close();
        }
    }
}
