/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import java.util.HashSet;
import java.util.Set;

public class ItunesDatasourceConfig extends DatasourceConfig {

    private Set<PathReplacement> myPathReplacements = new HashSet<PathReplacement>();

    public ItunesDatasourceConfig(String definition) {
        super(definition);
    }

    public ItunesDatasourceConfig(ItunesDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<PathReplacement>(source.getPathReplacements());
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

}