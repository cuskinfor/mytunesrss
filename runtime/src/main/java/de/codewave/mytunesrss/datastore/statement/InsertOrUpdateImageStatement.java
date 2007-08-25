package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public abstract class InsertOrUpdateImageStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(InsertOrUpdateImageStatement.class);

    private String myTrackId;
    private int mySize;
    private byte[] myData;
    private SmartStatement myStatement;

    public InsertOrUpdateImageStatement(String trackId, int size, byte[] data) {
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
                myStatement = MyTunesRssUtils.createStatement(connection, getStatementName());
            }
            myStatement.clearParameters();
            myStatement.setString("track_id", myTrackId);
            myStatement.setInt("size", mySize);
            myStatement.setObject("data", myData);
            myStatement.execute();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("Could not update image for track with ID \"%s\" in database.", myTrackId), e);
            }
        }
    }

    protected abstract String getStatementName();

    public void clear() {
        myTrackId = null;
        mySize = 0;
        myData = null;
    }
}