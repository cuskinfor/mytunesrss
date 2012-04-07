/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import java.util.HashSet;
import java.util.Set;

public abstract class PhotoDatasourceConfig extends DatasourceConfig {

    private Set<ReplacementRule> myPathReplacements = new HashSet<ReplacementRule>();

    public PhotoDatasourceConfig(String id, String definition) {
        super(id, definition);
    }

    public PhotoDatasourceConfig(PhotoDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<ReplacementRule>(source.getPathReplacements());
    }

    public Set<ReplacementRule> getPathReplacements() {
        return new HashSet<ReplacementRule>(myPathReplacements);
    }

    public void clearPathReplacements() {
        myPathReplacements.clear();
    }

    public void addPathReplacement(ReplacementRule pathReplacement) {
        myPathReplacements.add(pathReplacement);
    }

}