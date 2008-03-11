/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.DeletePlaylistStatement;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.DeletePlaylistCommandHandler
 */
public class DeletePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isCreatePlaylists()) {
            String playlistId = getRequestParameter("playlist", null);
            DeletePlaylistStatement statement = new DeletePlaylistStatement();
            statement.setId(playlistId);
            getTransaction().executeStatement(statement);
            forward(MyTunesRssCommand.ShowPlaylistManager);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}