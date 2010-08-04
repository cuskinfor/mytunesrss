/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class ItunesDatasourceConfig extends DatasourceConfig {

    private Set<PathReplacement> myPathReplacements = new HashSet<PathReplacement>();
    private boolean myDeleteMissingFiles = true;
    private Set<ItunesPlaylistType> myIgnorePlaylists = new HashSet<ItunesPlaylistType>();

    public ItunesDatasourceConfig(String definition) {
        super(definition);
    }

    public ItunesDatasourceConfig(ItunesDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<PathReplacement>(source.getPathReplacements());
        myIgnorePlaylists = new HashSet<ItunesPlaylistType>(source.getIgnorePlaylists());
        myDeleteMissingFiles = source.isDeleteMissingFiles();
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Itunes;
    }

    public Set<PathReplacement> getPathReplacements() {
        return new HashSet<PathReplacement>(myPathReplacements);
    }

    public void clearPathReplacements() {
        myPathReplacements.clear();
    }

    public void addPathReplacement(PathReplacement pathReplacement) {
        myPathReplacements.add(pathReplacement);
    }

    public boolean isDeleteMissingFiles() {
        return myDeleteMissingFiles;
    }

    public void setDeleteMissingFiles(boolean deleteMissingFiles) {
        myDeleteMissingFiles = deleteMissingFiles;
    }

    public Set<ItunesPlaylistType> getIgnorePlaylists() {
        return new HashSet<ItunesPlaylistType>(myIgnorePlaylists);
    }

    public void addIgnorePlaylist(ItunesPlaylistType type) {
        myIgnorePlaylists.add(type);
    }

    public void removeIgnorePlaylist(ItunesPlaylistType type) {
        myIgnorePlaylists.remove(type);
    }

    public void clearIgnorePlaylists() {
        myIgnorePlaylists.clear();
    }

}