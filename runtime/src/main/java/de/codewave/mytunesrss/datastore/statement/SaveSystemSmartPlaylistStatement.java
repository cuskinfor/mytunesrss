/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.Connection;
import java.sql.SQLException;

public class SaveSystemSmartPlaylistStatement extends SaveMyTunesSmartPlaylistStatement {

    public SaveSystemSmartPlaylistStatement(String id, SmartInfo smartInfo) {
        super(null, true, smartInfo);
        setId(id);
        setName(id);
        setType(PlaylistType.System);
    }

    @Override
    protected void handleIdAndUpdate(Connection connection) throws SQLException {
        setUpdate(false);
    }
}