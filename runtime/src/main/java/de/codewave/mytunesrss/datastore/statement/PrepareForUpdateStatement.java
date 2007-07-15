/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.PrepareForUpdateStatement
 */
public class PrepareForUpdateStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "prepareForUpdate");
        statement.setString("itunes", PlaylistType.ITunes.name());
        statement.setString("m3ufile", PlaylistType.M3uFile.name());
        statement.execute();
    }
}