/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

public class IphotoPhotoListener extends PhotoListener {
    public IphotoPhotoListener(IphotoDatasourceConfig datasourceConfig, Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<String, String> photoIdToPersId,
                               Map<String, Long> photoTsUpdate, Map<String, String> photoSourceId) throws SQLException {
        super(datasourceConfig, watchdogThread, queue, libraryListener, photoIdToPersId, photoTsUpdate, photoSourceId);
        myXmlModDate = new File(datasourceConfig.getDefinition(), IphotoDatasourceConfig.IPHOTO_XML_FILE_NAME).lastModified();
    }
}
