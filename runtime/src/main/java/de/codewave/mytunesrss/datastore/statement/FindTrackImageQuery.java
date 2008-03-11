package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        QueryResult<byte[]> results = execute(statement, new ResultBuilder<byte[]>() {
            public byte[] create(ResultSet resultSet) throws SQLException {
                return resultSet.getBytes("DATA");
            }
        });
        return results.getResultSize() == 0 ? null : results.nextResult();
    }
}