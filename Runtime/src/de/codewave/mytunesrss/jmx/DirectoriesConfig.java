/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.settings.*;

import javax.management.NotCompliantMBeanException;

/**
 * de.codewave.mytunesrss.jmx.DirectoriesConfig
 */
public class DirectoriesConfig extends MyTunesRssMBean implements DirectoriesConfigMBean {
  DirectoriesConfig() throws NotCompliantMBeanException {
    super(DirectoriesConfigMBean.class);
  }

    public String[] getDatasources() {
        return MyTunesRss.CONFIG.getDatasources();
    }

    public boolean isCreateUserSubdir() {
        return MyTunesRss.CONFIG.isUploadCreateUserDir();
    }

    public void setCreateUserSubdir(boolean createUserSubdir) {
        MyTunesRss.CONFIG.setUploadCreateUserDir(createUserSubdir);
    }

    public String getUploadDir() {
        return MyTunesRss.CONFIG.getUploadDir();
    }

    public void setUploadDir(String uploadDir) {
        MyTunesRss.CONFIG.setUploadDir(uploadDir);
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
        buffer.append(" / ").append(MyTunesRssUtils.getBundleString("settings.folderStructureTrack"));
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

    public String addDatasource(String watchFolder) {
        String error = MyTunesRss.CONFIG.addDatasource(watchFolder);
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String removeDatasource(String watchFolder) {
        String error = MyTunesRss.CONFIG.removeDatasource(watchFolder);
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }
}