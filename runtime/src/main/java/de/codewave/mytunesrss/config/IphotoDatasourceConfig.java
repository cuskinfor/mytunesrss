/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import java.util.HashSet;
import java.util.Set;

public class IphotoDatasourceConfig extends DatasourceConfig {

    private Set<ReplacementRule> myPathReplacements = new HashSet<ReplacementRule>();
    private boolean myImportRolls = true;
    private boolean myImportAlbums;

    public static final String XML_FILE_NAME = "AlbumData.xml";

    public IphotoDatasourceConfig(String definition) {
        super(definition);
    }

    public IphotoDatasourceConfig(IphotoDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<ReplacementRule>(source.getPathReplacements());
        myImportRolls = source.isImportRolls();
        myImportAlbums = source.isImportAlbums();
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Iphoto;
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

    public boolean isImportAlbums() {
        return myImportAlbums;
    }

    public void setImportAlbums(boolean importAlbums) {
        myImportAlbums = importAlbums;
    }

    public boolean isImportRolls() {
        return myImportRolls;
    }

    public void setImportRolls(boolean importRolls) {
        myImportRolls = importRolls;
    }
}