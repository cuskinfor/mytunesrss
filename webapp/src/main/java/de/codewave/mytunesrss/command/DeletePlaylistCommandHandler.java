/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.DeletePlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.DeletePlaylistCommandHandler
 */
public class DeletePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isCreatePlaylists()) {
            String playlistId = getRequestParameter("playlist", null);
            FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(getAuthUser(), null, playlistId, null, true, true);
            if (getTransaction().executeQuery(findPlaylistQuery).getRemainingResults().isEmpty()) {
                throw new IllegalArgumentException("Cannot delete playlist. Current user \"" + getAuthUser().getName() + "\" is not owner of playlist with ID \"" + playlistId + "\".");
            }
            DeletePlaylistStatement statement = new DeletePlaylistStatement();
            statement.setId(playlistId);
            getTransaction().executeStatement(statement);
            MyTunesRssUtils.updateUserDatabaseReferences(getTransaction());
            forward(MyTunesRssCommand.ShowPlaylistManager);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}