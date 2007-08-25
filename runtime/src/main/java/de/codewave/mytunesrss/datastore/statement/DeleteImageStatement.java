package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class DeleteImageStatement implements DataStoreStatement {
    private String myTrackId;

    public DeleteImageStatement(String trackId) {
        myTrackId = trackId;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "deleteImage");
        statement.setString("track_id", myTrackId);
        statement.execute();
    }
}