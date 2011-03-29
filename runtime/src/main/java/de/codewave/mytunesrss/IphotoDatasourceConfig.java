/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;

import java.util.HashSet;
import java.util.Set;

public class IphotoDatasourceConfig extends DatasourceConfig {

    private Set<PathReplacement> myPathReplacements = new HashSet<PathReplacement>();

    public static final String XML_FILE_NAME = "AlbumData.xml";

    public IphotoDatasourceConfig(String definition) {
        super(definition);
    }

    public IphotoDatasourceConfig(IphotoDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<PathReplacement>(source.getPathReplacements());
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Iphoto;
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

}