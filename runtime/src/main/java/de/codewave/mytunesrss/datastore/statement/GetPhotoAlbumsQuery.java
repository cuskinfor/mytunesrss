/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetPhotoAlbumsQuery extends DataStoreQuery<QueryResult<PhotoAlbum>> {

    private List<String> myRestrictedPhotoAlbumIds = Collections.emptyList();
    private List<String> myExcludedPhotoAlbumIds = Collections.emptyList();
    private List<String> myExcludedDataSourceIds = Collections.emptyList();

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
        myExcludedDataSourceIds = user.getExcludedDataSourceIds();
    }

    @Override
    public QueryResult<PhotoAlbum> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<>();
        conditionals.put("excluded", !myExcludedPhotoAlbumIds.isEmpty());
        conditionals.put("restricted", !myRestrictedPhotoAlbumIds.isEmpty());
        conditionals.put("excludedDatasources", !myExcludedDataSourceIds.isEmpty());
        conditionals.put("restricted_or_excluded", !myRestrictedPhotoAlbumIds.isEmpty() || !myExcludedPhotoAlbumIds.isEmpty() || !myExcludedDataSourceIds.isEmpty());
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotoAlbums", conditionals);
        statement.setItems("restrictedPhotoAlbumIds", myRestrictedPhotoAlbumIds);
        statement.setItems("excludedPhotoAlbumIds", myExcludedPhotoAlbumIds);
        statement.setItems("excludedDataSourceIds", myExcludedDataSourceIds);
        return execute(statement, new ResultBuilder<PhotoAlbum>() {
            @Override
            public PhotoAlbum create(ResultSet resultSet) throws SQLException {
                return new PhotoAlbum(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getLong("first_date"),
                        resultSet.getLong("last_date"),
                        resultSet.getInt("photo_count")
                );
            }
        });
    }
}
