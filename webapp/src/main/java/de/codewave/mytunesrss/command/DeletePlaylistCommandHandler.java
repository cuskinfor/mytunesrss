/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.sql.*;

/**
 * de.codewave.mytunesrss.command.DeletePlaylistCommandHandler
 */
public class DeletePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            String playlistId = getRequestParameter("playlist", null);
            DataStoreSession storeSession = getDataStore().getTransaction();
            DeletePlaylistStatement statement = new DeletePlaylistStatement();
            statement.setId(playlistId);
            storeSession.executeStatement(statement);
            storeSession.commit();
            forward(MyTunesRssCommand.ShowPlaylistManager);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}