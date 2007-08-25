package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.mp3.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

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
        List<Image> images = execute(statement, new ResultBuilder<Image>() {
            public Image create(ResultSet resultSet) throws SQLException {
                return new Image("image/jpeg", resultSet.getBytes("DATA"));
            }
        });
        return images.isEmpty() ? null : images.get(0);
    }
}