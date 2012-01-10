package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class FindImageQuery extends DataStoreQuery<Image> {
    private String myHash;
    private int mySize;

    public FindImageQuery(String hash, int size) {
        myHash = hash;
        mySize = size;
    }

    public Image execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, mySize > 0 ? "findImage" : "findMaxSizeImage");
        statement.setString("hash", myHash);
        statement.setInt("size", mySize);
        QueryResult<Image> results = execute(statement, new ResultBuilder<Image>() {
            public Image create(ResultSet resultSet) throws SQLException {
                return new Image(resultSet.getString("MIMETYPE"), resultSet.getBytes("DATA"));
            }
        });
        return results.getResultSize() == 0 ? null : results.nextResult();
    }
}