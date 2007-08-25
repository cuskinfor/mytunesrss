package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class FindTrackImageQuery extends DataStoreQuery<byte[]> {
    private String myTrackId;
    private int mySize;

    public FindTrackImageQuery(String trackId, int size) {
        myTrackId = trackId;
        mySize = size;
    }

    public byte[] execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findTrackImage");
        statement.setString("track_id", myTrackId);
        statement.setInt("size", mySize);
        List<byte[]> results = execute(statement, new ResultBuilder<byte[]>() {
            public byte[] create(ResultSet resultSet) throws SQLException {
                return resultSet.getBytes("DATA");
            }
        });
        return results.isEmpty() ? null : results.get(0);
    }
}