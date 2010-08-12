/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public abstract class DatasourceConfig implements Comparable<DatasourceConfig> {

    public static DatasourceConfig create(String definition) {
        if (StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(definition), "xml")) {
            return new ItunesDatasourceConfig(definition);
        } else {
            return new WatchfolderDatasourceConfig(definition);
        }
    }

    private String myDefinition;

    public DatasourceConfig(DatasourceConfig source) {
        myDefinition = source.getDefinition();
    }

    public DatasourceConfig(String definition) {
        setDefinition(definition);
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
            case Watchfolder:
                return new WatchfolderDatasourceConfig((WatchfolderDatasourceConfig)config);
            default:
                throw new IllegalArgumentException("Illegal datasource type.");
        }
    }
}
