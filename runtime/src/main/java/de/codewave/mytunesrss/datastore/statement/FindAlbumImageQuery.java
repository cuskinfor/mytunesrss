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
 * de.codewave.mytunesrss.datastore.statement.FindAlbumImageQuery
 */
public class FindAlbumImageQuery extends DataStoreQuery<Image> {
    private String myAlbum;
    private int mySize;

    public FindAlbumImageQuery(String album, int size) {
        myAlbum = album;
        mySize = size;
    }

    public Image execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAlbumImage");
        statement.setString("album", myAlbum);
        statement.setInt("size", mySize);
        QueryResult<Image> images = execute(statement, new ResultBuilder<Image>() {
            public Image create(ResultSet resultSet) throws SQLException {
                return new Image("image/jpeg", resultSet.getBytes("DATA"));
            }
        });
        return images.getResultSize() == 0 ? null : images.nextResult();
    }
}