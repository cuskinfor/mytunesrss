package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class RecreateHelpTablesStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(RecreateHelpTablesStatement.class);

    public void execute(Connection connection) throws SQLException {
        // SmartStatement statement = MyTunesRssUtils.createStatement(connection, "recreateHelpTables");
        // statement.setObject("mapping", new FindAlbumArtistMappingQuery().execute(connection));
        long startTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recreating help tables.");
        }
//        statement.execute();
        long endTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Time for building help tables: " + (endTime - startTime));
        }
    }
}