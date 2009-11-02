/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.PathReplacement;
import de.codewave.mytunesrss.CompiledPathReplacement;
import org.apache.commons.lang.StringUtils;

import javax.management.NotCompliantMBeanException;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

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

    public String[] getPathReplacements() {
        Set<PathReplacement> pathReplacements = MyTunesRss.CONFIG.getPathReplacements();
        String[] result = new String[pathReplacements.size()];
        int i = 0;
        for (PathReplacement pathReplacement : pathReplacements) {
            result[i++] = pathReplacement.getSearchPattern() + " --> " + pathReplacement.getReplacement();
        }
        return result;
    }

    public String addPathReplacement(String searchPattern, String replacement) {
        PathReplacement pathReplacement = new PathReplacement(searchPattern, replacement);
        try {
            new CompiledPathReplacement(pathReplacement);
            MyTunesRss.CONFIG.addPathReplacement(pathReplacement);
            onChange();
            return MyTunesRssUtils.getBundleString("ok");
        } catch (PatternSyntaxException e) {
            return MyTunesRssUtils.getBundleString("jmx.invalidPathReplacementPattern");
        }
    }

    public String removePathReplacement(String searchPattern) {
        if (MyTunesRss.CONFIG.removePathReplacement(new PathReplacement(searchPattern, null))) {
            onChange();
            return MyTunesRssUtils.getBundleString("ok");
        } else {
            return MyTunesRssUtils.getBundleString("jmx.pathReplacementPatternNotFound");
        }
    }
}