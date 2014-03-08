/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetPhotoAlbumQuery extends DataStoreQuery<QueryResult<PhotoAlbum>> {

    private String myId;

    public GetPhotoAlbumQuery(String id) {
        myId = id;
    }

    public QueryResult<PhotoAlbum> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotoAlbum");
        statement.setString("id", myId);
        return execute(statement, new ResultBuilder<PhotoAlbum>() {
            public PhotoAlbum create(ResultSet resultSet) throws SQLException {
                PhotoAlbum photoAlbum = new PhotoAlbum(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getLong("first_date"),
                        resultSet.getLong("last_date"),
                        resultSet.getInt("photo_count")
                );
                return photoAlbum;
            }
        });
    }
}
