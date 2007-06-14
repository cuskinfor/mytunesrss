/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.DirectoriesConfigMBean
 */
public interface DirectoriesConfigMBean {
    String[] getDatasources();

    boolean isCreateUserSubdir();

    void setCreateUserSubdir(boolean createUserSubdir);

    String getFolderStructure();

    void setFolderStructureToNone();

    void setFolderStructureToAlbumAndArtist();

    void setFolderStructureToArtistAndAlbum();

    void setFolderStructureToNoneAndAlbum();

    void setFolderStructureToNoneAndArtist();

    String getUploadDir();

    void setUploadDir(String uploadDir);

    String addDatasource(String datasource);

    String removeDatasource(String datasource);
}