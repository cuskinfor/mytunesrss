package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.MyTunesRssMp4Utils;
import de.codewave.utils.graphics.ImageUtils;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class HandleTrackImagesStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(HandleTrackImagesStatement.class);

    private File myFile;
    private String myTrackId;
    private Image myImage;
    private static final int MAX_IMAGE_DATA_SIZE = 1024 * 1000 * 2; // maximum image size is 2 MB

    public HandleTrackImagesStatement(File file, String trackId) {
        myFile = file;
        myTrackId = trackId;
    }

    public HandleTrackImagesStatement(File file, String trackId, Image image) {
        myFile = file;
        myTrackId = trackId;
        myImage = image;
    }

    public void execute(Connection connection) throws SQLException {
        if (myImage != null || FileSupportUtils.isMp3(myFile) || FileSupportUtils.isMp4(myFile)) {
            try {
                Image image = myImage;
                if (image == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
                    }
                    if (FileSupportUtils.isMp3(myFile)) {
                        image = MyTunesRssMp3Utils.getImage(myFile);
                    } else {
                        image = MyTunesRssMp4Utils.getImage(myFile);
                    }
                }
                if (image != null && image.getData() != null && image.getData().length > MAX_IMAGE_DATA_SIZE) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Ignoring overly large image from file \"" + myFile.getAbsolutePath() + "\" (size = " + image.getData().length +
                                ").");
                    }
                    image = null;
                }
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
            } catch (Throwable t) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Could not extract image from file \"" + myFile.getAbsolutePath() + "\".", t);
                }
            }
        }
    }
}