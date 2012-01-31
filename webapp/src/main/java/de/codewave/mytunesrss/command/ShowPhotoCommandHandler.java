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

public class ShowPhotoCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowPhotoCommandHandler.class);
    private static Map<String, String> IMAGE_TO_MIME = new HashMap<String, String>();

    static {
        IMAGE_TO_MIME.put("jpg", "image/jpeg");
        IMAGE_TO_MIME.put("gif", "image/gif");
        IMAGE_TO_MIME.put("png", "image/png");
    }

    @Override
    public void executeAuthorized() throws Exception {
        final String id = getRequestParameter("photo", null);
        if (StringUtils.isNotBlank(id)) {
            String filename = getTransaction().executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<String>>() {
                @Override
                public QueryResult<String> execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhoto");
                    statement.setString("id", id);
                    return execute(statement, new ResultBuilder<String>() {
                        public String create(ResultSet resultSet) throws SQLException {
                            return resultSet.getString("file");
                        }
                    });
                }
            }).getResult(0);
            if (!getAuthUser().isQuotaExceeded()) {
                File photoFile = new File(filename);
                if (StringUtils.isNotBlank(filename) && photoFile.isFile()) {
                    String mimeType = IMAGE_TO_MIME.get(FilenameUtils.getExtension(photoFile.getName()).toLowerCase());
                    StreamSender sender = null;
                    if (mimeType != null) {
                        Image image = new Image(mimeType, FileUtils.readFileToByteArray(photoFile));
                        Image scaledImage = MyTunesRssUtils.resizeImageWithMaxSize(image, getIntegerRequestParameter("size", Integer.MAX_VALUE));
                        sender = new StreamSender(new ByteArrayInputStream(scaledImage.getData()), scaledImage.getMimeType(), scaledImage.getData().length);
                        // no need to close the byte array input stream later
                    } else {
                        sender = new FileSender(photoFile, "image/" + StringUtils.lowerCase(FilenameUtils.getExtension(filename), Locale.ENGLISH), photoFile.length());
                    }
                    sender.setCounter((StreamSender.ByteSentCounter) SessionManager.getSessionInfo(getRequest()));
                    sendResponse(sender, photoFile.getName());
                } else {
                    getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                LOGGER.warn("User limit exceeded, sending response code SC_CONFLICT instead.");
                MyTunesRss.ADMIN_NOTIFY.notifyQuotaExceeded(getAuthUser());
                getResponse().sendError(HttpServletResponse.SC_CONFLICT, "QUOTA EXCEEDED");
            }
        } else {
            getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected void sendResponse(StreamSender sender, String filename) throws IOException {
        sender.sendGetResponse(getRequest(), getResponse(), false);
    }
}
