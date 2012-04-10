/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class SaveSystemSmartPlaylistStatement extends SaveMyTunesSmartPlaylistStatement {

    public SaveSystemSmartPlaylistStatement(String id, Collection<SmartInfo> smartInfos) {
        super(null, true, smartInfos);
        setId(id);
        setName(id);
        setType(PlaylistType.System);
    }

    @Override
    protected void handleIdAndUpdate(Connection connection) throws SQLException {
        setUpdate(false);
    }
}