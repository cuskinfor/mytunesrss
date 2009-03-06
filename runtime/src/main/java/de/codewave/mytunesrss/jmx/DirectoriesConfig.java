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

    public String getAlbumFallback() {
        return MyTunesRss.CONFIG.getAlbumFallback();
    }

    public void setAlbumFallback(String albumFallback) {
        MyTunesRss.CONFIG.setAlbumFallback(albumFallback);
        onChange();
    }

    public String getArtistFallback() {
        return MyTunesRss.CONFIG.getArtistFallback();
    }

    public void setArtistFallback(String artistFallback) {
        MyTunesRss.CONFIG.setArtistFallback(artistFallback);
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