package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class UpdateImageForTrackStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateImageForTrackStatement.class);

    private String myTrackId;
    private String myHash;
    private SmartStatement myStatement;

    public UpdateImageForTrackStatement(String trackId, String hash) {
        myTrackId = trackId;
        myHash = hash;
    }

    public synchronized void execute(Connection connection) throws SQLException {
            try {
                if (myStatement == null) {
                    myStatement = MyTunesRssUtils.createStatement(connection, "updateImageForTrack");
                }
                myStatement.clearParameters();
                myStatement.setString("track_id", myTrackId);
                myStatement.setString("hash", myHash);
                myStatement.setLong("updateTime", System.currentTimeMillis());
                myStatement.execute();
            } catch (SQLException e) {
                LOG.error("Could not update image for track \"" + myTrackId + "\".", e);
            }
    }

    public void clear() {
        myTrackId = null;
        myHash = null;
    }
}