/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DatasourceConfig implements Comparable<DatasourceConfig> {

    public static DatasourceConfig create(String id, String definition) {
        File file = new File(definition);
        if (file.isFile() && StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(definition), "xml")) {
            return new ItunesDatasourceConfig(id, definition);
        } else if (file.isDirectory() && new File(file, IphotoDatasourceConfig.IPHOTO_XML_FILE_NAME).isFile()) {
            return new IphotoDatasourceConfig(id, definition);
        } else if (file.isDirectory() && new File(file, ApertureDatasourceConfig.APERTURE_XML_FILE_NAME).isFile()) {
            return new ApertureDatasourceConfig(id, definition);
        } else if (file.isDirectory()) {
            return new WatchfolderDatasourceConfig(id, definition);
        } else {
            return null;
        }
    }

    private String myDefinition;
    private String myId;
    private List<FileType> myFileTypes = new ArrayList<FileType>();

    public DatasourceConfig(DatasourceConfig source) {
        myId = source.getId();
        myDefinition = source.getDefinition();
        myFileTypes = source.getFileTypes();
    }

    public DatasourceConfig(String id, String definition) {
        setId(id);
        setDefinition(definition);
        setFileTypes(getDefaultFileTypes());
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
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

    public List<FileType> getFileTypes() {
        return new ArrayList<FileType>(myFileTypes);
    }

    public void setFileTypes(List<FileType> fileTypes) {
        myFileTypes = new ArrayList<FileType>(fileTypes);
    }

    public FileType getFileType(String suffix) {
        if (suffix != null) {
            for (FileType type : myFileTypes) {
                if (suffix.equalsIgnoreCase(type.getSuffix())) {
                    return type;
                }
            }
        }
        return null;
    }

    public boolean isProtected(String filename) {
        FileType type = getFileType(FileSupportUtils.getFileSuffix(filename));
        return type != null && type.isProtected();
    }

    public String getContentType(String filename) {
        FileType type = getFileType(FileSupportUtils.getFileSuffix(filename));
        if (type != null) {
            return type.getMimeType();
        }
        return "application/octet-stream";
    }

    public boolean isSupported(String filename) {
        return isSuffixSupported(FileSupportUtils.getFileSuffix(filename));
    }

    private boolean isSuffixSupported(String suffix) {
        FileType type = getFileType(suffix);
        return type != null && type.isActive();
    }

    public abstract List<FileType> getDefaultFileTypes();
}
