/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.settings.*;

/**
 * de.codewave.mytunesrss.jmx.DirectoriesConfig
 */
public class DirectoriesConfig implements DirectoriesConfigMBean {
    public String getArtistDropWords() {
        return MyTunesRss.CONFIG.getArtistDropWords();
    }

    public void setArtistDropWords(String artistDropWords) {
        MyTunesRss.CONFIG.setArtistDropWords(artistDropWords);
    }

    public String getBaseDir() {
        return MyTunesRss.CONFIG.getBaseDir();
    }

    public void setBaseDir(String baseDir) {
        MyTunesRss.CONFIG.setBaseDir(baseDir);
    }

    public boolean isCreateUserSubdir() {
        return MyTunesRss.CONFIG.isUploadCreateUserDir();
    }

    public void setCreateUserSubdir(boolean createUserSubdir) {
        MyTunesRss.CONFIG.setUploadCreateUserDir(createUserSubdir);
    }

    public String getFileTypes() {
        return MyTunesRss.CONFIG.getFileTypes();
    }

    public void setFileTypes(String fileTypes) {
        MyTunesRss.CONFIG.setFileTypes(fileTypes);
    }

    public boolean isRemoveMissingItunesTracks() {
        return MyTunesRss.CONFIG.isItunesDeleteMissingFiles();
    }

    public void setRemoveMissingItunesTracks(boolean removeMissingTracks) {
        MyTunesRss.CONFIG.setItunesDeleteMissingFiles(removeMissingTracks);
    }

    public String getUploadDir() {
        return MyTunesRss.CONFIG.getUploadDir();
    }

    public void setUploadDir(String uploadDir) {
        MyTunesRss.CONFIG.setUploadDir(uploadDir);
    }

    public String getItunesMusicLibraryXmlPath() {
        return MyTunesRss.CONFIG.getLibraryXml();
    }

    public void setItunesMusicLibraryXmlPath(String path) {
        MyTunesRss.CONFIG.setLibraryXml(path);
    }

    public String getFolderStructure() {
        StringBuffer buffer = new StringBuffer();
        if (MyTunesRss.CONFIG.getFileSystemAlbumNameFolder() == 2) {
            buffer.append(Directories.FolderStructureRole.Album);
        } else if (MyTunesRss.CONFIG.getFileSystemArtistNameFolder() == 2) {
            buffer.append(Directories.FolderStructureRole.Artist);
        } else {
            buffer.append(Directories.FolderStructureRole.None);
        }
        buffer.append(" / ");
        if (MyTunesRss.CONFIG.getFileSystemAlbumNameFolder() == 1) {
            buffer.append(Directories.FolderStructureRole.Album);
        } else if (MyTunesRss.CONFIG.getFileSystemArtistNameFolder() == 1) {
            buffer.append(Directories.FolderStructureRole.Artist);
        } else {
            buffer.append(Directories.FolderStructureRole.None);
        }
        buffer.append(" / ").append(MyTunesRss.BUNDLE.getString("settings.folderStructureTrack"));
        return buffer.toString();
    }

    public void setFolderStructureToAlbumAndArtist() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(2);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(1);
    }

    public void setFolderStructureToArtistAndAlbum() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(1);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(2);
    }

    public void setFolderStructureToNone() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(0);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(0);
    }

    public void setFolderStructureToNoneAndAlbum() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(1);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(0);
    }

    public void setFolderStructureToNoneAndArtist() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(0);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(1);
    }
}