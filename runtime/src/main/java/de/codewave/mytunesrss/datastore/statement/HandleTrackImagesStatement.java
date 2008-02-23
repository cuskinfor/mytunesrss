package de.codewave.mytunesrss.datastore.statement;

import de.codewave.camel.mp3.*;
import de.codewave.camel.mp4.*;
import de.codewave.mytunesrss.meta.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.graphics.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class HandleTrackImagesStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(HandleTrackImagesStatement.class);

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
                boolean existing = new FindTrackImageQuery(myTrackId, 32).execute(connection) != null;
                if (image != null && image.getData() != null && image.getData().length > MAX_IMAGE_DATA_SIZE) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Ignoring overly large image from file \"" + myFile.getAbsolutePath() + "\" (size = " + image.getData().length + ").");
                    }
                    image = null;
                }
                if (image != null  && image.getData() != null && image.getData().length > 0 && !existing) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Original image size is " + image.getData().length + " bytes.");
                    }
                    new InsertImageStatement(myTrackId, 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                    new InsertImageStatement(myTrackId, 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                    new InsertImageStatement(myTrackId, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                    new InsertImageStatement(myTrackId, 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                } else if (image != null && image.getData() != null && image.getData().length > 0 && existing) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Original image size is " + image.getData().length + " bytes.");
                    }
                    new UpdateImageStatement(myTrackId, 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                    new UpdateImageStatement(myTrackId, 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                    new UpdateImageStatement(myTrackId, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                    new UpdateImageStatement(myTrackId, 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                } else if ((image == null || image.getData() == null || image.getData().length == 0) && existing) {
                    new DeleteImageStatement(myTrackId).execute(connection);
                }
                new LastImageUpdateTimeStatement(myTrackId).execute(connection);
            } catch (Throwable t) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Could not extract image from MP3 file.", t);
                }
            }
        }
    }
}