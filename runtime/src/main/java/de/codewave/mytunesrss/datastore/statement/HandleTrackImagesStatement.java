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

    public HandleTrackImagesStatement(File file, String trackId) {
        myFile = file;
        myTrackId = trackId;
    }

    public void execute(Connection connection) throws SQLException {
        if (FileSupportUtils.isMp3(myFile) || FileSupportUtils.isMp4(myFile)) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
                }
                Image image = null;
                if (FileSupportUtils.isMp3(myFile)) {
                    image = MyTunesRssMp3Utils.getImage(myFile);
                } else {
                    image = MyTunesRssMp4Utils.getImage(myFile);
                }
                boolean existing = new FindTrackImageQuery(myTrackId, 32).execute(connection) != null;
                if (image != null  && image.getData() != null && !existing) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Original image size is " + image.getData().length + " bytes.");
                    }
                    new InsertImageStatement(myTrackId, 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                    new InsertImageStatement(myTrackId, 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                    new InsertImageStatement(myTrackId, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                    new InsertImageStatement(myTrackId, 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                } else if (image != null && image.getData() != null && existing) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Original image size is " + image.getData().length + " bytes.");
                    }
                    new UpdateImageStatement(myTrackId, 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                    new UpdateImageStatement(myTrackId, 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                    new UpdateImageStatement(myTrackId, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                    new UpdateImageStatement(myTrackId, 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                } else if ((image == null || image.getData() == null) && existing) {
                    new DeleteImageStatement(myTrackId).execute(connection);
                }
                new LastImageUpdateTimeStatement(myTrackId).execute(connection);
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Could not extract image from MP3 file.", e);
                }
            }
        }
    }
}