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
import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistIdsQuery
 */
public class FindPhotoAlbumIdsQuery extends DataStoreQuery<Collection<String>> {

    public Collection<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotoAlbumIds");
        return execute(statement, new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString("ID");
            }
        }).getResults();
    }
}