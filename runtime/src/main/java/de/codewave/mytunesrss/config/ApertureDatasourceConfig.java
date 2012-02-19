/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import java.util.HashSet;
import java.util.Set;

public class ApertureDatasourceConfig extends PhotoDatasourceConfig {

    public static final String APERTURE_XML_FILE_NAME = "ApertureData.xml";

    public ApertureDatasourceConfig(String definition) {
        super(definition);
    }

    public ApertureDatasourceConfig(ApertureDatasourceConfig source) {
        super(source);
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Aperture;
    }

}