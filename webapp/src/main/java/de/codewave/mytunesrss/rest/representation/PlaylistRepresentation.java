/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement
public class PlaylistRepresentation {

    private URI myTracksUri;
    private URI myChildrenUri;
    private URI myParentUri;
    private URI myTagsUri;
    private URI myDownloadUri;
    private String myName;
    private String myContainerId;
    private boolean myHidden;
    private String myId;
    private int myTrackCount;
    private PlaylistType myType;
    private String myOwner;
    private boolean myPrivate;

    public PlaylistRepresentation() {
    }

    public PlaylistRepresentation(Playlist playlist) {
        setName(playlist.getName());
        setContainerId(playlist.getContainerId());
        setHidden(playlist.isHidden());
        setId(playlist.getId());
        setTrackCount(playlist.getTrackCount());
        setType(playlist.getType());
        setOwner(playlist.getUserOwner());
        setPrivate(playlist.isUserPrivate());
    }

    public URI getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(URI tracksUri) {
        myTracksUri = tracksUri;
    }

    public URI getChildrenUri() {
        return myChildrenUri;
    }

    public void setChildrenUri(URI childrenUri) {
        myChildrenUri = childrenUri;
    }

    public URI getParentUri() {
        return myParentUri;
    }

    public void setParentUri(URI parentUri) {
        myParentUri = parentUri;
    }

    public URI getTagsUri() {
        return myTagsUri;
    }

    public void setTagsUri(URI tagsUri) {
        myTagsUri = tagsUri;
    }

    public URI getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(URI downloadUri) {
        myDownloadUri = downloadUri;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public String getContainerId() {
        return myContainerId;
    }

    public void setContainerId(String containerId) {
        myContainerId = containerId;
    }

    public boolean isHidden() {
        return myHidden;
    }

    public void setHidden(boolean hidden) {
        myHidden = hidden;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }

    public PlaylistType getType() {
        return myType;
    }

    public void setType(PlaylistType type) {
        myType = type;
    }

    public String isUserOwner() {
        return myOwner;
    }

    public void setOwner(String owner) {
        myOwner = owner;
    }

    public boolean isPrivate() {
        return myPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        myPrivate = aPrivate;
    }
}
