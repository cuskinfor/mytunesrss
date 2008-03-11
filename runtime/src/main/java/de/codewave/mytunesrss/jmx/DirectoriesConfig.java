/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.settings.Directories;

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
        onChange();
    }

    public String getUploadDir() {
        return MyTunesRss.CONFIG.getUploadDir();
    }

    public void setUploadDir(String uploadDir) {
        MyTunesRss.CONFIG.setUploadDir(uploadDir);
        onChange();
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
        onChange();
    }

    public void setFolderStructureToArtistAndAlbum() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(1);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(2);
        onChange();
    }

    public void setFolderStructureToNone() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(0);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(0);
        onChange();
    }

    public void setFolderStructureToNoneAndAlbum() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(1);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(0);
        onChange();
    }

    public void setFolderStructureToNoneAndArtist() {
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(0);
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(1);
        onChange();
    }

    public String addDatasource(String watchFolder) {
        String error = MyTunesRss.CONFIG.addDatasource(watchFolder);
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String removeDatasource(String watchFolder) {
        String error = MyTunesRss.CONFIG.removeDatasource(watchFolder);
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }
}