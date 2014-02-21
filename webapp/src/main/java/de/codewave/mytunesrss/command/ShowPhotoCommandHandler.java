/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class ShowPhotoCommandHandler extends BandwidthThrottlingCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowPhotoCommandHandler.class);

    private static final class SimplePhoto {
        private String myFile;
        private long myLastImageUpdate;

        private SimplePhoto(String file, long lastImageUpdate) {
            myFile = file;
            myLastImageUpdate = lastImageUpdate;
        }
    }

    @Override
    public void executeAuthorized() throws Exception {
        final String id = getRequestParameter("photo", null);
        if (StringUtils.isNotBlank(id)) {
            SimplePhoto photo = getTransaction().executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<SimplePhoto>>() {
                @Override
                public QueryResult<SimplePhoto> execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhoto");
                    statement.setString("id", id);
                    return execute(statement, new ResultBuilder<SimplePhoto>() {
                        public SimplePhoto create(ResultSet resultSet) throws SQLException {
                            return new SimplePhoto(resultSet.getString("file"), resultSet.getLong("last_image_update"));
                        }
                    });
                }
            }).getResult(0);
            long ifModifiedSince = getRequest().getDateHeader("If-Modified-Since");
            File photoFile = new File(photo.myFile);
            if (ifModifiedSince > -1 && (photoFile.lastModified() / 1000) <= (ifModifiedSince / 1000)) {
                getResponse().setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            } else {
                if (!getAuthUser().isQuotaExceeded()) {
                    if (StringUtils.isNotBlank(photo.myFile) && photoFile.isFile()) {
                        StreamSender sender = null;
                        int requestedImageSize;
                        int maxImageSize = 0;
                        try {
                            maxImageSize = MyTunesRssUtils.getMaxImageSize(photoFile);
                            requestedImageSize = getIntegerRequestParameter("size", Integer.MAX_VALUE);
                        } catch (IOException e) {
                            requestedImageSize = 0;
                        }
                        String mimeType;
                        if (requestedImageSize > 0 && maxImageSize > requestedImageSize) {
                            File tempFile = MyTunesRssUtils.createTempFile("jpg");
                            byte[] image;
                            try {
                                MyTunesRssUtils.resizeImageWithMaxSize(photoFile, tempFile, requestedImageSize, (float)getIntegerRequestParameter("jpegQuality", MyTunesRss.CONFIG.getJpegQuality()), "photo=" + photoFile.getAbsolutePath());
                                image = FileUtils.readFileToByteArray(tempFile);
                            } finally {
                                tempFile.delete();
                            }
                            mimeType = "image/jpg";
                            sender = new StreamSender(new ByteArrayInputStream(image), mimeType, tempFile.length());
                            // no need to close the byte array input stream later
                        } else {
                            mimeType = MyTunesRssUtils.guessContentType(photoFile);
                            sender = new FileSender(photoFile, mimeType, photoFile.length());
                        }
                        sender.setCounter((StreamSender.ByteSentCounter) SessionManager.getSessionInfo(getRequest()));
                        getResponse().setDateHeader("Last-Modified", photo.myLastImageUpdate);
                        getResponse().setHeader("Cache-Control", MyTunesRssWebUtils.createCacheControlValue(0));
                        sendResponse(sender, MyTunesRssUtils.getLegalFileName(FilenameUtils.getBaseName(photoFile.getName()) + "." + MyTunesRssUtils.getSuffixForMimeType(mimeType)));
                    } else {
                        LOGGER.warn("Photo file \"" + photoFile + "\" not found.");
                        getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    LOGGER.warn("User limit exceeded, sending response code SC_CONFLICT instead.");
                    MyTunesRss.ADMIN_NOTIFY.notifyQuotaExceeded(getAuthUser());
                    getResponse().sendError(HttpServletResponse.SC_CONFLICT, "QUOTA EXCEEDED");
                }
            }
        } else {
            getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected void sendResponse(StreamSender sender, String filename) throws IOException {
        getResponse().setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        sender.sendGetResponse(getRequest(), getResponse(), false);
    }
}
