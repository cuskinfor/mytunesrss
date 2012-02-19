/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.config.ApertureDatasourceConfig;
import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.Collection;
import java.util.Map;

public class AperturePhotoListener extends PhotoListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AperturePhotoListener.class);

    private Connection mySqliteConnection;
    private PreparedStatement myQuery;
    private File myMastersFolder;

    public AperturePhotoListener(ApertureDatasourceConfig datasourceConfig, Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<String, String> photoIdToPersId,
                                 Collection<String> photoIds, Connection sqliteConnection) throws SQLException {
        super(datasourceConfig, watchdogThread, queue, libraryListener, photoIdToPersId, photoIds);
        myXmlModDate = new File(datasourceConfig.getDefinition(), ApertureDatasourceConfig.APERTURE_XML_FILE_NAME).lastModified();
        mySqliteConnection = sqliteConnection;
        myQuery = sqliteConnection.prepareStatement("SELECT m.fileIsReference, m.imagePath FROM RKMaster m, RKVersion v WHERE v.uuid = ? AND v.masterUuid = m.uuid");
        myMastersFolder = new File(datasourceConfig.getDefinition(), "Masters");
    }

    protected String getImagePath(String id, Map photo) {
        String originalPath = (String) photo.get("OriginalPath");
        if (StringUtils.isBlank(originalPath)) {
            ResultSet rs = null;
            try {
                myQuery.setString(1, id);
                rs = myQuery.executeQuery();
                if (rs.next()) {
                    Integer fileIsReference = rs.getInt("fileIsReference");
                    String imagePath = rs.getString("imagePath");
                    if (fileIsReference > 0) {
                        return new File("/", imagePath).getAbsolutePath();
                    } else {
                        return new File(myMastersFolder, imagePath).getAbsolutePath();
                    }
                }
            } catch (SQLException e) {
                LOGGER.warn("Could not query sqlite3 database.", e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LOGGER.warn("Could not close result set after successful query.", e);
                    }
                }
            }
            return StringUtils.defaultIfBlank(originalPath, (String) photo.get("ImagePath"));
        }
        return originalPath;
    }
}
