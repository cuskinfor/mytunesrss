/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;

import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindArtistPagesStatement
 */
public class FindAlbumPagesStatement extends DataStoreQuery {
    public Collection execute(Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT key AS key, value AS value FROM album_pager ORDER by index");
        List<Pager.Page> pages = new ArrayList<Pager.Page>();
        while (resultSet.next()) {
            pages.add(new Pager.Page(resultSet.getString("KEY"), resultSet.getString("VALUE")));
        }
        return pages.isEmpty() ? null : pages;
    }
}