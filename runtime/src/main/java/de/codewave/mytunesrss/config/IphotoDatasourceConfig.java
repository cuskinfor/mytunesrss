/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

public class IphotoDatasourceConfig extends PhotoDatasourceConfig {

    private boolean myImportRolls = true;
    private boolean myImportAlbums;

    public static final String IPHOTO_XML_FILE_NAME = "AlbumData.xml";

    public IphotoDatasourceConfig(String id, String name, String definition) {
        super(id, name, definition);
    }

    public IphotoDatasourceConfig(IphotoDatasourceConfig source) {
        super(source);
        myImportRolls = source.isImportRolls();
        myImportAlbums = source.isImportAlbums();
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Iphoto;
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