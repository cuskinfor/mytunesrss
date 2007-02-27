/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.DirectoriesConfigMBean
 */
public interface DirectoriesConfigMBean {
    String getArtistDropWords();

    void setArtistDropWords(String artistDropWords);

    String getBaseDir();

    void setBaseDir(String baseDir);

    boolean isCreateUserSubdir();

    void setCreateUserSubdir(boolean createUserSubdir);

    String getFileTypes();

    void setFileTypes(String fileTypes);

    String getFolderStructure();

    void setFolderStructureToNone();

    void setFolderStructureToAlbumAndArtist();

    void setFolderStructureToArtistAndAlbum();

    void setFolderStructureToNoneAndAlbum();

    void setFolderStructureToNoneAndArtist();

    boolean isRemoveMissingItunesTracks();

    void setRemoveMissingItunesTracks(boolean removeMissingTracks);

    String getUploadDir();

    void setUploadDir(String uploadDir);

    String getItunesMusicLibraryXmlPath();

    void setItunesMusicLibraryXmlPath(String path);
}