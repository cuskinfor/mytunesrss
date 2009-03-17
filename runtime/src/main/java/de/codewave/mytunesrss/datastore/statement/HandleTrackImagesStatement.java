package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.datastore.external.YouTubeLoader;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.MyTunesRssMp4Utils;
import de.codewave.utils.graphics.ImageUtils;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.google.gdata.util.ServiceException;
import com.google.gdata.data.media.mediarss.MediaThumbnail;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class HandleTrackImagesStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(HandleTrackImagesStatement.class);
    private static Map<String, String> IMAGE_TO_MIME = new HashMap<String, String>();
    private static final Image IMAGE_UP_TO_DATE = new Image(null, null);
    private static final int MAX_IMAGE_DATA_SIZE = 1024 * 1000 * 2; // maximum image size is 2 MB

    static {
        IMAGE_TO_MIME.put("jpg", "image/jpeg");
        IMAGE_TO_MIME.put("gif", "image/gif");
        IMAGE_TO_MIME.put("png", "image/png");
    }

    private long myLastUpdateTime;
    private File myFile;
    private String myTrackId;
    private Image myImage;
    private TrackSource mySource;

    public HandleTrackImagesStatement(TrackSource source, File file, String trackId, long lastUpdateTime) {
        myLastUpdateTime = lastUpdateTime;
        myFile = file;
        myTrackId = trackId;
        mySource = source;
    }

    public HandleTrackImagesStatement(File file, String trackId, Image image, long lastUpdateTime) {
        myLastUpdateTime = lastUpdateTime;
        myFile = file;
        myTrackId = trackId;
        myImage = image;
    }

    public void execute(Connection connection) throws SQLException {
        try {
            Image image = getImage();
            if (image != IMAGE_UP_TO_DATE) {
//                if (image != null && image.getData() != null && image.getData().length > MAX_IMAGE_DATA_SIZE) {
//                    if (LOG.isInfoEnabled()) {
//                        LOG.info("Ignoring overly large image for file \"" + myFile.getAbsolutePath() + "\" (size = " + image.getData().length + ").");
//                    }
//                    image = null;
//                }
                String imageHash =
                        image != null && image.getData() != null ? MyTunesRssBase64Utils.encode(MyTunesRss.MD5_DIGEST.digest(image.getData())) : null;
                if (imageHash != null) {
                    LOG.debug("Image hash is \"" + imageHash + "\".");
                    boolean existing = new FindImageQuery(imageHash, 32).execute(connection) != null;
                    if (existing) {
                        LOG.debug("Image with hash \"" + imageHash + "\" already exists in database.");
                    } else {
                        LOG.debug("Image with hash \"" + imageHash + "\" does not exist in database.");
                    }
                    if (image != null && image.getData() != null && image.getData().length > 0 && !existing) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Original image size is " + image.getData().length + " bytes.");
                        }
                        new InsertImageStatement(imageHash, 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                        new InsertImageStatement(imageHash, 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                        new InsertImageStatement(imageHash, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                        new InsertImageStatement(imageHash, 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                    }
                }
                new UpdateImageForTrackStatement(myTrackId, imageHash).execute(connection);
            }
        } catch (Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not extract image from file \"" + myFile.getAbsolutePath() + "\".", t);
            }
        }
    }

    private Image getImage() throws IOException {
        Image image = myImage;
        if (image == null) {
            if (mySource == TrackSource.YouTube) {
                try {
                    MediaThumbnail thumbnail = YouTubeLoader.getMediaThumbnail(StringUtils.substringAfter(myTrackId, "youtube_"));
                    GetMethod method = new GetMethod(thumbnail.getUrl());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        if (new HttpClient().executeMethod(method) == 200) {
                            IOUtils.copy(method.getResponseBodyAsStream(), baos);
                            return new Image(IMAGE_TO_MIME.get(StringUtils.lowerCase(StringUtils.substringAfterLast(thumbnail.getUrl(), "/"))), baos.toByteArray());
                        }
                    } finally {
                        IOUtils.closeQuietly(baos);
                        method.releaseConnection();
                    }

                } catch (ServiceException e) {
                    LOG.error("Could not read youtube image.", e);
                }
                return null;
            }
            File imageFile = findImageFile(myFile);
            if (imageFile != null) {
                if (imageFile.lastModified() >= myLastUpdateTime) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Reading image information from file \"" + imageFile.getAbsolutePath() + "\".");
                    }
                    image = new Image(IMAGE_TO_MIME.get(FilenameUtils.getExtension(imageFile.getName()).toLowerCase()), FileUtils.readFileToByteArray(
                            imageFile));
                } else {
                    image = IMAGE_UP_TO_DATE;
                }
            } else {
                if (FileSupportUtils.isMp3(myFile)) {
                    if (myFile.lastModified() >= myLastUpdateTime) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
                        }
                        image = MyTunesRssMp3Utils.getImage(myFile);
                    } else {
                        image = IMAGE_UP_TO_DATE;
                    }
                } else if (FileSupportUtils.isMp4(myFile)) {
                    if (myFile.lastModified() >= myLastUpdateTime) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
                        }
                        image = MyTunesRssMp4Utils.getImage(myFile);
                    } else {
                        image = IMAGE_UP_TO_DATE;
                    }
                }
            }
        }
        return image;
    }

    private File findImageFile(File file) {
        for (String suffix : IMAGE_TO_MIME.keySet()) {
            File imageFile;
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase(suffix)) {
                imageFile = file;
            } else {
                imageFile = new File(file.getAbsolutePath() + "." + suffix);
            }
            if (imageFile.isFile()) {
                return imageFile;
            }
        }
        return null;
    }
}