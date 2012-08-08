package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.FindImageQuery;
import de.codewave.mytunesrss.datastore.statement.HandlePhotoImagesStatement;
import de.codewave.mytunesrss.datastore.statement.HandleTrackImagesStatement;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.command.ShowTrackImageCommandHandler
 */
public class ShowImageCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ShowImageCommandHandler.class);

    private static class SimplePhoto {
        private String myImageHash;
        private File myFile;

        private SimplePhoto(String imageHash, File file) {
            myImageHash = imageHash;
            myFile = file;
        }
    }

    private Map<Integer, Image> myDefaultImages = new HashMap<Integer, Image>();

    private Image getDefaultImage(int size) {
        if (size <= 0) {
            return null; // TODO: maybe better an IllegalArgumentException?
        }
        if (myDefaultImages.get(size) == null) {
            synchronized (myDefaultImages) {
                if (myDefaultImages.get(size) == null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = MyTunesRssCommandHandler.class.getClassLoader().getResourceAsStream("de/codewave/mytunesrss/default_rss_image.png");
                        Image image = new Image("image/png", inputStream);
                        myDefaultImages.put(size, MyTunesRssUtils.resizeImageWithMaxSize(image, size));
                    } catch (IOException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not copy default image data into byte array.", e);
                        }
                    } finally {
                        IOUtils.close(inputStream);
                    }
                }
            }
        }
        return myDefaultImages.get(size);
    }

    protected void sendDefaultImage(int size) throws IOException {
        Image defaultImage = getDefaultImage(size);
        if (defaultImage != null) {
            getResponse().setContentType(defaultImage.getMimeType());
            getResponse().setContentLength(defaultImage.getData().length);
            getResponse().setHeader("Cache-Control", "max-age=" + MyTunesRss.CONFIG.getImageExpirationMillis());
            getResponse().setDateHeader("Expires", System.currentTimeMillis() + MyTunesRss.CONFIG.getImageExpirationMillis());
            getResponse().getOutputStream().write(defaultImage.getData());
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
            getResponse().setHeader("Cache-Control", "max-age=" + MyTunesRss.CONFIG.getImageExpirationMillis());
            getResponse().setDateHeader("Expires", System.currentTimeMillis() + MyTunesRss.CONFIG.getImageExpirationMillis());
        }
    }

    protected void sendImage(Image image) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending image with mime type \"" + image.getMimeType() + "\".");
        }
        getResponse().setContentType(image.getMimeType());
        getResponse().setContentLength(image.getData().length);
        getResponse().setHeader("Cache-Control", "max-age=" + MyTunesRss.CONFIG.getImageExpirationMillis());
        getResponse().setDateHeader("Expires", System.currentTimeMillis() + MyTunesRss.CONFIG.getImageExpirationMillis());
        getResponse().getOutputStream().write(image.getData());
    }

    @Override
    public void executeAuthorized() throws Exception {
        Image image = null;
        String photoId = getRequest().getParameter("photoId");
        String hash = getRequest().getParameter("hash");
        int size = getIntegerRequestParameter("size", -1);
        if (!isRequestAuthorized()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Not authorized to request image, sending default MyTunesRSS image.");
            }
        } else {
            if (StringUtils.isNotBlank(hash)) {
                image = getTransaction().executeQuery(new FindImageQuery(hash, size));
            } else if (StringUtils.isNotBlank(photoId)) {
                image = getImageForPhotoId(photoId, size);
            }
        }
        if (image == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("No tracks recognized in request or no images found in recognized tracks, sending default MyTunesRSS image.");
            }
            sendDefaultImage(size);
        } else {
            sendImage(image);
        }
    }

    private Image getImageForPhotoId(final String photoId, int size) throws SQLException {
        LOG.debug("Trying to generate thumbnail for photo \"" + photoId + "\".");
        DataStoreSession session = getTransaction();
        SimplePhoto photo = session.executeQuery(new DataStoreQuery<SimplePhoto>() {
            @Override
            public SimplePhoto execute(Connection connection) throws SQLException {
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotoImageHashAndFile");
                statement.setString("id", photoId);
                QueryResult<SimplePhoto> queryResult = execute(statement, new ResultBuilder<SimplePhoto>() {
                    public SimplePhoto create(ResultSet resultSet) throws SQLException {
                        return new SimplePhoto(resultSet.getString("image_hash"), new File(resultSet.getString("file")));
                    }
                });
                return queryResult.getResultSize() == 1 ? queryResult.getResult(0) : null;
            }
        });
        if (photo != null && !"".equals(photo.myImageHash) && photo.myFile != null && photo.myFile.exists()) {
            LOG.debug("Photo file is \"" + photo.myFile.getAbsolutePath() + "\".");
            HandlePhotoImagesStatement handlePhotoImagesStatement = new HandlePhotoImagesStatement(photo.myFile, photoId);
            session.executeStatement(handlePhotoImagesStatement);
            LOG.debug("Photo image hash is \"" + handlePhotoImagesStatement.getImageHash() + "\".");
            if (StringUtils.isNotBlank(handlePhotoImagesStatement.getImageHash())) {
                return session.executeQuery(new FindImageQuery(handlePhotoImagesStatement.getImageHash(), size));
            }
        }
        return null;
    }

}