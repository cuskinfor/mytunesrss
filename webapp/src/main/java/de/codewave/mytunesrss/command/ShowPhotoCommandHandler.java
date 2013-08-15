/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
                        String mimeType = MyTunesRssUtils.IMAGE_TO_MIME.get(FilenameUtils.getExtension(photoFile.getName()).toLowerCase());
                        StreamSender sender = null;
                        if (mimeType != null) {
                            Image scaledImage = MyTunesRssUtils.resizeImageWithMaxSize(photoFile, getIntegerRequestParameter("size", Integer.MAX_VALUE), (float)getIntegerRequestParameter("jpegQuality", MyTunesRss.CONFIG.getJpegQuality()), "photo=" + photoFile.getAbsolutePath());
                            sender = new StreamSender(new ByteArrayInputStream(scaledImage.getData()), scaledImage.getMimeType(), scaledImage.getData().length);
                            // no need to close the byte array input stream later
                        } else {
                            sender = new FileSender(photoFile, "image/" + StringUtils.lowerCase(FilenameUtils.getExtension(photo.myFile), Locale.ENGLISH), photoFile.length());
                        }
                        sender.setCounter((StreamSender.ByteSentCounter) SessionManager.getSessionInfo(getRequest()));
                        getResponse().setDateHeader("Last-Modified", photo.myLastImageUpdate);
                        getResponse().setHeader("Cache-Control", "max-age=0, no-cache, must-revalidate");
                        sendResponse(sender, MyTunesRssUtils.getLegalFileName(photoFile.getName()));
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
