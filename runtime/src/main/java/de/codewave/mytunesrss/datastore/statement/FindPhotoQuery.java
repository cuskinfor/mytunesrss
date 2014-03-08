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
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQuery
 */
public class FindPhotoQuery extends DataStoreQuery<QueryResult<Photo>> {

    public static FindPhotoQuery getForAlbum(User user, String photoAlbumId) {
        FindPhotoQuery query = new FindPhotoQuery();
        query.myUser = user;
        query.myAlbumId = photoAlbumId;
        return query;
    }

    private User myUser;
    private String myAlbumId;

    private FindPhotoQuery() {
        // intentionally left blank
    }

    public QueryResult<Photo> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<>();
        conditionals.put("excluded", myUser != null && !myUser.getExcludedPhotoAlbumIds().isEmpty());
        conditionals.put("restricted", myUser != null && !myUser.getRestrictedPhotoAlbumIds().isEmpty());
        conditionals.put("excludedDatasources", myUser != null && !myUser.getExcludedDataSourceIds().isEmpty());
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findPhotos", conditionals);
        statement.setString("album_id", myAlbumId);
        if (myUser != null) {
            statement.setItems("restrictedPhotoAlbumIds", myUser.getRestrictedPhotoAlbumIds());
            statement.setItems("excludedPhotoAlbumIds", myUser.getExcludedPhotoAlbumIds());
            statement.setItems("excludedDataSourceIds", myUser.getExcludedDataSourceIds());
        }
        return execute(statement, new ResultBuilder<Photo>() {
            public Photo create(ResultSet resultSet) throws SQLException {
                Photo photo = new Photo();
                photo.setId(resultSet.getString("id"));
                photo.setName(resultSet.getString("name"));
                photo.setFile(resultSet.getString("file"));
                photo.setDate(resultSet.getLong("date"));
                photo.setImageHash(StringUtils.trimToNull(resultSet.getString("image_hash")));
                photo.setLastImageUpdate(resultSet.getLong("last_image_update"));
                return photo;
            }
        });
    }
}
