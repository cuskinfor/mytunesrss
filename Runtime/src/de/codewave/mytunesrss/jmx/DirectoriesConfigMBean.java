/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.DirectoriesConfigMBean
 */
public interface DirectoriesConfigMBean {
    String[] getWatchFolders();

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

    String getItunesMusicLibraryXmlPath();

    void setItunesMusicLibraryXmlPath(String path);

    String addWatchFolder(String watchFolder);

    String removeWatchFolder(String watchFolder);
}