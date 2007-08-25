package de.codewave.mytunesrss.datastore.statement;

import de.codewave.camel.mp3.*;
import de.codewave.mytunesrss.mp3.*;
import de.codewave.utils.graphics.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class HandleTrackImagesStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(HandleTrackImagesStatement.class);

    private File myFile;
    private String myTrackId;
    private boolean myUpdated;

    public HandleTrackImagesStatement(File file, String trackId) {
        myFile = file;
        myTrackId = trackId;
    }

    public boolean isUpdated() {
        return myUpdated;
    }

    public void execute(Connection connection) throws SQLException {
        try {
            Image image = ID3Utils.getImage(Mp3Utils.readId3v2Tag(myFile));
            boolean existing = new FindTrackImageQuery(myTrackId, 32).execute(connection) != null;
            if (image != null && !existing) {
                new InsertImageStatement(myTrackId, 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                new InsertImageStatement(myTrackId, 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                new InsertImageStatement(myTrackId, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                new InsertImageStatement(myTrackId, 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                myUpdated = true;
            } else if (image != null && existing) {
                new InsertImageStatement(myTrackId, 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                new InsertImageStatement(myTrackId, 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                new InsertImageStatement(myTrackId, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                new InsertImageStatement(myTrackId, 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                myUpdated = true;
            } else if (image == null && existing) {
                new DeleteImageStatement(myTrackId).execute(connection);
                myUpdated = true;
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not extract image from MP3 file.");
            }
        }
    }
}