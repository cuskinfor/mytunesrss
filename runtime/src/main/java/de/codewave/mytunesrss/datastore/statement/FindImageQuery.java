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
public class FindImageQuery extends DataStoreQuery<byte[]> {
    private String myHash;
    private int mySize;

    public FindImageQuery(String hash, int size) {
        myHash = hash;
        mySize = size;
    }

    public byte[] execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findImage");
        statement.setString("hash", myHash);
        statement.setInt("size", mySize);
        QueryResult<byte[]> results = execute(statement, new ResultBuilder<byte[]>() {
            public byte[] create(ResultSet resultSet) throws SQLException {
                return resultSet.getBytes("DATA");
            }
        });
        return results.getResultSize() == 0 ? null : results.nextResult();
    }
}