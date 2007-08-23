package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class InsertImageStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(InsertImageStatement.class);

    private String myTrackId;
    private int mySize;
    private byte[] myData;
    private SmartStatement myStatement;

    public InsertImageStatement(String trackId, int size, byte[] data) {
        myTrackId = trackId;
        mySize = size;
        myData = data;
    }

    public void setData(byte[] data) {
        myData = data;
    }

    public void setSize(int size) {
        mySize = size;
    }

    public void setTrackId(String trackId) {
        myTrackId = trackId;
    }

    public synchronized void execute(Connection connection) throws SQLException {
        try {
            if (myStatement == null) {
                myStatement = MyTunesRssUtils.createStatement(connection, "insertImage");
            }
            myStatement.clearParameters();
            myStatement.setString("track_id", myTrackId);
            myStatement.setInt("size", mySize);
            myStatement.setObject("data", myData);
            myStatement.execute();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("Could not insert image for track with ID \"%s\" into database.", myTrackId), e);
            }
        }
    }

    public void clear() {
        myTrackId = null;
        mySize = 0;
        myData = null;
    }
}