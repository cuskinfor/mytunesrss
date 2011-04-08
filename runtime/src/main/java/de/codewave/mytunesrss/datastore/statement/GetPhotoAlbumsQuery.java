/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class GetPhotoAlbumsQuery extends DataStoreQuery<DataStoreQuery.QueryResult<PhotoAlbum>> {

    private List<String> myRestrictedPhotoAlbumIds = Collections.emptyList();
    private List<String> myExcludedPhotoAlbumIds = Collections.emptyList();

    /**
     * Get all photo albums
     */
    public GetPhotoAlbumsQuery() {
        // intentionally left blank
    }

    /**
     * Get only photo albums visible to the specified user.
     *
     * @param user A user.
     */
    public GetPhotoAlbumsQuery(User user) {
        myRestrictedPhotoAlbumIds = user.getRestrictedPhotoAlbumIds();
        myExcludedPhotoAlbumIds = user.getExcludedPhotoAlbumIds();
    }

    public QueryResult<PhotoAlbum> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        conditionals.put("excluded", !myExcludedPhotoAlbumIds.isEmpty());
        conditionals.put("restricted", !myRestrictedPhotoAlbumIds.isEmpty());
        conditionals.put("restricted_or_excluded", !myRestrictedPhotoAlbumIds.isEmpty() || !myExcludedPhotoAlbumIds.isEmpty());
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotoAlbums", conditionals);
        statement.setItems("restrictedPhotoAlbumIds", myRestrictedPhotoAlbumIds);
        statement.setItems("excludedPhotoAlbumIds", myExcludedPhotoAlbumIds);
        return execute(statement, new ResultBuilder<PhotoAlbum>() {
            public PhotoAlbum create(ResultSet resultSet) throws SQLException {
                PhotoAlbum photoAlbum = new PhotoAlbum(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getLong("first_date"),
                        resultSet.getLong("last_date")
                );
                return photoAlbum;
            }
        });
    }
}
