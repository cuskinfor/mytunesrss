/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.ResultSetType;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQuery
 */
public class FindPhotoQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Photo>> {

    public static FindPhotoQuery getForAlbum(String photoAlbumId) {
        FindPhotoQuery query = new FindPhotoQuery();
        query.myAlbumId = photoAlbumId;
        return query;
    }

    private String myAlbumId;

    private FindPhotoQuery() {
        // intentionally left blank
    }

    public QueryResult<Photo> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findPhotos");
        statement.setString("album_id", myAlbumId);
        return execute(statement, new ResultBuilder<Photo>() {
            public Photo create(ResultSet resultSet) throws SQLException {
                Photo photo = new Photo();
                photo.setId(resultSet.getString("id"));
                photo.setName(resultSet.getString("name"));
                photo.setFile(resultSet.getString("file"));
                photo.setDate(resultSet.getLong("date"));
                photo.setImageHash(resultSet.getString("image_hash"));
                photo.setLastImageUpdate(resultSet.getLong("last_image_update"));
                return photo;
            }
        });
    }
}