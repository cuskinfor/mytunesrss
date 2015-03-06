/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public abstract class DatasourceConfig implements Comparable<DatasourceConfig> {

    public static DatasourceConfig create(String id, String name, String definition) {
        File file = new File(definition);
        if (file.isFile() && StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(definition), "xml")) {
            return new ItunesDatasourceConfig(id, name, definition);
        } else if (file.isDirectory() && new File(file, IphotoDatasourceConfig.IPHOTO_XML_FILE_NAME).isFile()) {
            return new IphotoDatasourceConfig(id, name, definition);
        } else if (file.isDirectory() && new File(file, ApertureDatasourceConfig.APERTURE_XML_FILE_NAME).isFile()) {
            return new ApertureDatasourceConfig(id, name, definition);
        } else if (file.isDirectory()) {
            return new WatchfolderDatasourceConfig(id, name, definition);
        } else {
            return null;
        }
    }

    private String myName;
    private String myDefinition;
    private String myId;
    private long myLastUpdate;
    private boolean myUpload;

    public DatasourceConfig(DatasourceConfig source) {
        myId = source.getId();
        myName = source.myName;
        myDefinition = source.getDefinition();
        myUpload = source.isUpload();
    }

    public DatasourceConfig(String id, String name, String definition) {
        setId(id);
        setName(name);
        setDefinition(definition);
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public String getName() {
        return StringUtils.defaultIfBlank(myName, getId());
    }

    public void setName(String name) {
        myName = name;
    }

    public String getDefinition() {
        return myDefinition;
    }

    public void setDefinition(String definition) {
        if (StringUtils.isBlank(definition)) {
            throw new NullPointerException("Datasource definition must not be blank.");
        }
        myDefinition = StringUtils.trim(definition);
    }

    public abstract DatasourceType getType();

    @Override
    public int compareTo(DatasourceConfig other) {
        return getDefinition().compareTo(other.getDefinition());
    }

    public static DatasourceConfig copy(DatasourceConfig config) {
        switch (config.getType()) {
            case Itunes:
                return new ItunesDatasourceConfig((ItunesDatasourceConfig)config);
            case Iphoto:
                return new IphotoDatasourceConfig((IphotoDatasourceConfig)config);
            case Aperture:
                return new ApertureDatasourceConfig((ApertureDatasourceConfig)config);
            case Watchfolder:
                return new WatchfolderDatasourceConfig((WatchfolderDatasourceConfig)config);
            default:
                throw new IllegalArgumentException("Illegal datasource type.");
        }
    }

    public long getLastUpdate() {
        return myLastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        myLastUpdate = lastUpdate;
    }

    public boolean isUpload() {
        return myUpload;
    }

    public void setUpload(boolean upload) {
        myUpload = upload;
    }

    public boolean isUploadable() {
        return false;
    }

    public long getDefinitionLastModified() {
        File file = new File(myDefinition);
        return file.exists() ? file.lastModified() : 0;
    }

    public abstract boolean isUseSingleImageInFolder();
}
