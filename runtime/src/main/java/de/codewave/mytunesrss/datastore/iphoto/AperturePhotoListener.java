/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.config.ApertureDatasourceConfig;
import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.Collection;
import java.util.Map;

public class AperturePhotoListener extends PhotoListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AperturePhotoListener.class);

    public AperturePhotoListener(ApertureDatasourceConfig datasourceConfig, Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<String, String> photoIdToPersId,
                                 Map<String, Long> photoTsUpdate) throws SQLException {
        super(datasourceConfig, watchdogThread, queue, libraryListener, photoIdToPersId, photoTsUpdate);
        myXmlModDate = new File(datasourceConfig.getDefinition(), ApertureDatasourceConfig.APERTURE_XML_FILE_NAME).lastModified();
    }
}
