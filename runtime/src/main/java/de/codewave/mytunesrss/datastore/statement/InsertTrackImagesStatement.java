package de.codewave.mytunesrss.datastore.statement;

import de.codewave.camel.mp3.*;
import de.codewave.mytunesrss.mp3.*;
import de.codewave.utils.graphics.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class InsertTrackImagesStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(InsertTrackImagesStatement.class);

    private List<Track> myTracks;

    public InsertTrackImagesStatement(List<Track> tracks) {
        myTracks = tracks;
    }

    public void execute(Connection connection) throws SQLException {
        for (Track track : myTracks) {
            try {
                Image image = ID3Utils.getImage(Mp3Utils.readId3v2Tag(track.getFile()));
                if (image != null) {
                    new InsertImageStatement(track.getId(), 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                    new InsertImageStatement(track.getId(), 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                    new InsertImageStatement(track.getId(), 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                    new InsertImageStatement(track.getId(), 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Could not extract image from MP3 file.");
                }
            }
        }

    }
}