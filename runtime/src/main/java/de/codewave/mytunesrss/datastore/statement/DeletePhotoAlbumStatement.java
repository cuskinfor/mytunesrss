/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.DeletePhotoAlbumStatement
 */
public class DeletePhotoAlbumStatement implements DataStoreStatement {
    private String myId;

    public void setId(String id) {
        myId = id;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "deletePhotoAlbumById");
        statement.setString("id", myId);
        statement.execute();
    }
}