/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetPhotoQuery extends DataStoreQuery<Photo> {

    private String myId;

    public GetPhotoQuery(String id) {
        myId = id;
    }

    @Override
    public Photo execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhoto");
        statement.setString("id", myId);
        return execute(statement, new ResultBuilder<Photo>() {
            @Override
            public Photo create(ResultSet resultSet) throws SQLException {
                Photo photo = new Photo();
                photo.setId(resultSet.getString("id"));
                photo.setName(resultSet.getString("name"));
                photo.setFile(resultSet.getString("file"));
                photo.setDate(resultSet.getLong("date"));
                photo.setImageHash(StringUtils.trimToNull(resultSet.getString("image_hash")));
                photo.setLastImageUpdate(resultSet.getLong("last_image_update"));
                photo.setWidth(resultSet.getLong("width"));
                photo.setHeight(resultSet.getLong("height"));
                return photo;
            }
        }).getResult(0);
    }
}
