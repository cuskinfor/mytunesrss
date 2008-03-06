/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;

/**
 * de.codewave.mytunesrss.command.ShowSettingsCommandHandler
 */
public class ShowSettingsCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isEditWebSettings()) {
            getRequest().setAttribute("themes", AddonsUtils.getThemes());
            getRequest().setAttribute("playlists", getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, null, false, false)).getResults());
            forward(MyTunesRssResource.Settings);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}