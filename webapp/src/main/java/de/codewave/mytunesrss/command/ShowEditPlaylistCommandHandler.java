/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.service.EditPlaylistService;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.EditPlaylistCommandHandler
 */
public class ShowEditPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            forward(MyTunesRssResource.EditPlaylist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}