/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Playlist;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PlaylistRepresentation extends Playlist {

    private Map<String, URI> myUri = new HashMap<String, URI>();

    public PlaylistRepresentation(Playlist playlist) {
        setName(playlist.getName());
        setContainerId(playlist.getContainerId());
        setHidden(playlist.isHidden());
        setId(playlist.getId());
        setTrackCount(playlist.getTrackCount());
        setType(playlist.getType());
        setUserOwner(playlist.getUserOwner());
        setUserPrivate(playlist.isUserPrivate());
    }

    public Map<String, URI> getUri() {
        return myUri;
    }
}
