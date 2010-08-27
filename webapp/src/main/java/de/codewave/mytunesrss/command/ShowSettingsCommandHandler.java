/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.AddonsUtils;
import de.codewave.mytunesrss.FlashPlayerConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.ShowSettingsCommandHandler
 */
public class ShowSettingsCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isEditWebSettings()) {
            getRequest().setAttribute("themes", AddonsUtils.getThemes(true));
            getRequest().setAttribute("playlists", getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                       null,
                                                                                                       null,
                                                                                                       null,
                                                                                                       false,
                                                                                                       false)).getResults());
            List<FlashPlayerConfig> flashPlayerConfigs = new ArrayList<FlashPlayerConfig>(FlashPlayerConfig.getDefaults());
            flashPlayerConfigs.addAll(MyTunesRss.CONFIG.getFlashPlayers());
            Collections.sort(flashPlayerConfigs);
            getRequest().setAttribute("flashplayers", flashPlayerConfigs);
            forward(MyTunesRssResource.Settings);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}