/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a playlist.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PlaylistRepresentation implements RestRepresentation {

    /**
     * @exclude from swagger docs
     */
    private String myTracksUri;
    /**
     * @exclude from swagger docs
     */
    private String myChildrenUri;
    /**
     * @exclude from swagger docs
     */
    private String myParentUri;
    /**
     * @exclude from swagger docs
     */
    private String myDownloadUri;
    /**
     * @exclude from swagger docs
     */
    private String myName;
    /**
     * @exclude from swagger docs
     */
    private String myContainerId;
    /**
     * @exclude from swagger docs
     */
    private Boolean myHidden;
    /**
     * @exclude from swagger docs
     */
    private String myId;
    /**
     * @exclude from swagger docs
     */
    private Integer myTrackCount;
    /**
     * @exclude from swagger docs
     */
    private PlaylistType myType;
    /**
     * @exclude from swagger docs
     */
    private String myOwner;
    /**
     * @exclude from swagger docs
     */
    private Boolean myPrivate;

    public PlaylistRepresentation() {
    }

    public PlaylistRepresentation(Playlist playlist) {
        if (IncludeExcludeInterceptor.isAttr("name")) {
            setName(playlist.getName());
        }
        if (IncludeExcludeInterceptor.isAttr("containerId")) {
            setContainerId(playlist.getContainerId());
        }
        if (IncludeExcludeInterceptor.isAttr("hidden")) {
            setHidden(playlist.isHidden());
        }
        if (IncludeExcludeInterceptor.isAttr("id")) {
            setId(playlist.getId());
        }
        if (IncludeExcludeInterceptor.isAttr("trackCount")) {
            setTrackCount(playlist.getTrackCount());
        }
        if (IncludeExcludeInterceptor.isAttr("type")) {
            setType(playlist.getType());
        }
        if (IncludeExcludeInterceptor.isAttr("owner")) {
            setOwner(playlist.getUserOwner());
        }
        if (IncludeExcludeInterceptor.isAttr("private")) {
            setPrivate(playlist.isUserPrivate());
        }
    }

    /**
     * URI to the tracks of the playlist.
     */
    public String getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(String tracksUri) {
        myTracksUri = tracksUri;
    }

    /**
     * URI to the child playlists.
     */
    public String getChildrenUri() {
        return myChildrenUri;
    }

    public void setChildrenUri(String childrenUri) {
        myChildrenUri = childrenUri;
    }

    /**
     * URI to the parent playlist.
     */
    public String getParentUri() {
        return myParentUri;
    }

    public void setParentUri(String parentUri) {
        myParentUri = parentUri;
    }

    /**
     * URI for downloading a ZIP archive with all tracks of the playlist.
     */
    public String getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        myDownloadUri = downloadUri;
    }

    /**
     * Name of the playlist.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * Playlist ID of the parent playlist.
     */
    public String getContainerId() {
        return myContainerId;
    }

    public void setContainerId(String containerId) {
        myContainerId = containerId;
    }

    /**
     * TRUE if the playlist should be hidden from user interfaces.
     */
    public Boolean isHidden() {
        return myHidden;
    }

    public void setHidden(Boolean hidden) {
        myHidden = hidden;
    }

    /**
     * ID of the playlist.
     */
    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    /**
     * Number of tracks in the playlist.
     */
    public Integer getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(Integer trackCount) {
        myTrackCount = trackCount;
    }

    /**
     * Type of the playlist.
     */
    public PlaylistType getType() {
        return myType;
    }

    public void setType(PlaylistType type) {
        myType = type;
    }

    /**
     * Owner of the playlist (user name).
     */
    public String getOwner() {
        return myOwner;
    }

    public void setOwner(String owner) {
        myOwner = owner;
    }

    /**
     * TRUE if the playlist is a private playlist of the owner or FALSE for a public playlist.
     */
    public Boolean isPrivate() {
        return myPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        myPrivate = aPrivate;
    }
}
