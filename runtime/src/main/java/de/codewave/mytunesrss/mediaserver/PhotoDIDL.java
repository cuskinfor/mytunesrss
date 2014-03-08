/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.GetPhotoAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.datastore.statement.PhotoAlbum;
import de.codewave.utils.sql.*;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.item.Item;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class PhotoDIDL extends MyTunesRssDIDL {

    static final int DEFAULT_PHOTO_SIZE = 1024;
    private long myTotalMatches;

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) {
        // no children available, it is not a container
        myTotalMatches = 0;
    }

    @Override
    void createMetaData(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        Photo photo = tx.executeQuery(new DataStoreQuery<QueryResult<Photo>>() {
            @Override
            public QueryResult<Photo> execute(Connection connection) throws SQLException {
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhoto");
                statement.setString("id", decode(oidParams).get(1));
                return execute(statement, new ResultBuilder<Photo>() {
                    public Photo create(ResultSet resultSet) throws SQLException {
                        Photo photo = new Photo();
                        photo.setId(resultSet.getString("ID"));
                        photo.setName(resultSet.getString("NAME"));
                        photo.setFile(resultSet.getString("FILE"));
                        photo.setDate(resultSet.getLong("DATE"));
                        photo.setImageHash(resultSet.getString("IMAGE_HASH"));
                        photo.setLastImageUpdate(resultSet.getLong("LAST_IMAGE_UPDATE"));
                        return photo;
                    }
                });
            }
        }).getResult(0);
        PhotoAlbum photoAlbum = tx.executeQuery(new GetPhotoAlbumQuery(decode(oidParams).get(0))).nextResult();
        Item item = createPhotoItem(photo, photoAlbum, new SimpleDateFormat("yyyy-dd-mm"), user, getInt(decode(oidParams).get(0), DEFAULT_PHOTO_SIZE));
        if (item != null) {
            addItem(item);
            myTotalMatches = 1;
        } else {
            myTotalMatches = 0;
        }
    }

    @Override
    long getTotalMatches() {
        return myTotalMatches;
    }

}
