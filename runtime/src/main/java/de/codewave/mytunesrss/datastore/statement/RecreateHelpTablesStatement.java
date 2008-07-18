package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class RecreateHelpTablesStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(RecreateHelpTablesStatement.class);

    public void execute(Connection connection) throws SQLException {
        SmartStatement statementAlbum = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesAlbum");
        SmartStatement statementArtist = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesArtist");
        SmartStatement statementGenre = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesGenre");
        long startTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recreating help tables.");
        }
        connection.commit();
        statementAlbum.execute();
        connection.commit();
        statementArtist.execute();
        connection.commit();
        statementGenre.execute();
        connection.commit();
        long endTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Time for building help tables: " + (endTime - startTime));
        }
    }
}