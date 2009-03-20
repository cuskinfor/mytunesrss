/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;

import javax.management.NotCompliantMBeanException;

import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.jmx.DirectoriesConfig
 */
public class DatasourcesConfig extends MyTunesRssMBean implements DatasourcesConfigMBean {
    DatasourcesConfig() throws NotCompliantMBeanException {
        super(DatasourcesConfigMBean.class);
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
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUploadDir());
    }

    public void setUploadDir(String uploadDir) {
        MyTunesRss.CONFIG.setUploadDir(StringUtils.trimToNull(uploadDir));
        onChange();
    }

    public String getAlbumFallback() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getAlbumFallback());
    }

    public void setAlbumFallback(String albumFallback) {
        MyTunesRss.CONFIG.setAlbumFallback(StringUtils.trimToNull(albumFallback));
        onChange();
    }

    public String getArtistFallback() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getArtistFallback());
    }

    public void setArtistFallback(String artistFallback) {
        MyTunesRss.CONFIG.setArtistFallback(StringUtils.trimToNull(artistFallback));
        onChange();
    }

    public String addDatasource(String watchFolder) {
        String error = MyTunesRss.CONFIG.addDatasource(StringUtils.trimToEmpty(watchFolder));
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String removeDatasource(String watchFolder) {
        String error = MyTunesRss.CONFIG.removeDatasource(StringUtils.trimToEmpty(watchFolder));
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }
}