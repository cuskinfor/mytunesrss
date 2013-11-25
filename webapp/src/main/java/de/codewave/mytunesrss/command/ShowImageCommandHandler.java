package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.DatabaseImage;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * de.codewave.mytunesrss.command.ShowTrackImageCommandHandler
 */
public class ShowImageCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ShowImageCommandHandler.class);

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
                        File tempFile = MyTunesRssUtils.createTempFile("jpg");
                        try {
                            MyTunesRssUtils.resizeImageWithMaxSize(image, tempFile, size, (float)MyTunesRss.CONFIG.getJpegQuality(), "default image");
                            myDefaultImages.put(size, new Image("image/jpg", FileUtils.readFileToByteArray(tempFile)));
                        } finally {
                            tempFile.delete();
                        }
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
            long ifModifiedSince = getRequest().getDateHeader("If-Modified-Since");
            if (ifModifiedSince != -1 && (MyTunesRss.STARTUP_TIME / 1000) <= (ifModifiedSince / 1000)) {
                getResponse().setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            } else {
                getResponse().setContentType(defaultImage.getMimeType());
                getResponse().setContentLength(defaultImage.getData().length);
                getResponse().getOutputStream().write(defaultImage.getData());
            }
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    protected void sendImage(DatabaseImage image) throws IOException {
        long ifModifiedSince = getRequest().getDateHeader("If-Modified-Since");
        if (ifModifiedSince != -1 && (image.getLastUpdate() / 1000) <= (ifModifiedSince / 1000)) {
            getResponse().setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending image with mime type \"" + image.getMimeType() + "\".");
            }
            getResponse().setContentType(image.getMimeType());
            getResponse().setContentLength(image.getData().length);
            getResponse().setHeader("Cache-Control", MyTunesRssWebUtils.createCacheControlValue(MyTunesRss.CONFIG.getImageExpirationMillis() / 1000));
            getResponse().setDateHeader("Expires", System.currentTimeMillis() + MyTunesRss.CONFIG.getImageExpirationMillis());
            getResponse().getOutputStream().write(image.getData());
        }
    }

    @Override
    public void executeAuthorized() throws Exception {
        DatabaseImage image = null;
        String photoId = getRequest().getParameter("photoId");
        String hash = getRequest().getParameter("hash");
        int size = getIntegerRequestParameter("size", -1);
        if (!isRequestAuthorized()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Not authorized to request image, sending default MyTunesRSS image.");
            }
        } else {
            if (StringUtils.isNotBlank(hash)) {
                File file = MyTunesRssUtils.getImage(hash, size);
                if (file != null) {
                    image = new DatabaseImage(URLConnection.guessContentTypeFromName(file.getName()), new FileInputStream(file), file.lastModified());
                }
            } else if (StringUtils.isNotBlank(photoId)) {
                image = getImageForPhotoId(photoId, size);
            } else {
                LOG.warn("Neither photo id nor image hash found in request.");
            }
        }
        if (image == null) {
            LOG.warn("No image available, sending default image.");
            sendDefaultImage(size);
        } else {
            sendImage(image);
        }
    }

    private DatabaseImage getImageForPhotoId(final String photoId, int size) throws SQLException, IOException {
        LOG.debug("Trying to generate thumbnail for photo \"" + photoId + "\".");
        DataStoreSession session = getTransaction();
        File photoFile = session.executeQuery(new DataStoreQuery<File>() {
            @Override
            public File execute(Connection connection) throws SQLException {
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotoImageHashAndFile");
                statement.setString("id", photoId);
                QueryResult<File> queryResult = execute(statement, new ResultBuilder<File>() {
                    public File create(ResultSet resultSet) throws SQLException {
                        return new File(resultSet.getString("file"));
                    }
                });
                return queryResult.getResultSize() == 1 ? queryResult.getResult(0) : null;
            }
        });
        if (photoFile != null && photoFile.exists()) {
            LOG.debug("Photo file is \"" + photoFile.getAbsolutePath() + "\".");
            Future<String> result = MyTunesRss.EXECUTOR_SERVICE.generatePhotoThumbnail(photoId, photoFile);
            try {
                String imageHash = result.get(MyTunesRss.CONFIG.getOnDemandThumbnailGenerationTimeoutSeconds() * 1000, TimeUnit.MILLISECONDS);
                LOG.debug("Photo image hash is \"" + imageHash + "\".");
                if (StringUtils.isNotBlank(imageHash)) {
                    File file = MyTunesRssUtils.getImage(imageHash, size);
                    if (file != null) {
                        return new DatabaseImage(URLConnection.guessContentTypeFromName(file.getName()), new FileInputStream(file), file.lastModified());
                    }
                }
            } catch (InterruptedException e) {
                LOG.warn("On-demand photo thumbnail generation interrupted.", e);
            } catch (ExecutionException e) {
                LOG.warn("On-demand photo thumbnail generation failed.", e);
            } catch (TimeoutException e) {
                LOG.warn("On-demand photo thumbnail generation timeout.", e);
            }
        } else {
            LOG.warn("No photo file found for photo id \"" + photoId + "\".");
        }
        return null;
    }

}
