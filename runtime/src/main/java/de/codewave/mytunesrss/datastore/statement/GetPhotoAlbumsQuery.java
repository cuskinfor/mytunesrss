/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

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
public class GetPhotoAlbumsQuery extends DataStoreQuery<DataStoreQuery.QueryResult<PhotoAlbum>> {

    public QueryResult<PhotoAlbum> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotoAlbums");
        return execute(statement, new ResultBuilder<PhotoAlbum>() {
            public PhotoAlbum create(ResultSet resultSet) throws SQLException {
                Long firstDate = resultSet.getLong("first_date");
                if (resultSet.wasNull()) {
                    firstDate = null;
                }
                Long lastDate = resultSet.getLong("last_date");
                if (resultSet.wasNull()) {
                    lastDate = null;
                }
                PhotoAlbum photoAlbum = new PhotoAlbum(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        firstDate,
                        lastDate
                );
                return photoAlbum;
            }
        });
    }
}
