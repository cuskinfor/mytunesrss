/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.DirectoriesConfigMBean
 */
public interface DatasourcesConfigMBean {
    String[] getDatasources();

    boolean isCreateUserSubdir();

    void setCreateUserSubdir(boolean createUserSubdir);

    String getAlbumFallback();

    void setAlbumFallback(String albumFallback);

    String getArtistFallback();

    void setArtistFallback(String artistFallback);

    String getUploadDir();

    void setUploadDir(String uploadDir);

    String addDatasource(String datasource);

    String removeDatasource(String datasource);
}