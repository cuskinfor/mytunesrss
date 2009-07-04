/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssTask;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement;
import de.codewave.mytunesrss.datastore.statement.MigrationStatement;
import de.codewave.mytunesrss.datastore.statement.TuneDatabaseStatement;
import de.codewave.utils.Version;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class InitializeDatabaseTask extends MyTunesRssTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeDatabaseTask.class);

    private Version myVersion;

    public void execute() throws IOException, SQLException {
        LOGGER.debug("Initializing the database.");
        MyTunesRss.STORE.init();
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        loadVersion(session);
        if (myVersion == null) {
            LOGGER.debug("No version found. Creating all tables.");
            session.executeStatement(new CreateAllTablesStatement());
            DatabaseBuilderTask.doCheckpoint(session, true);
            loadVersion(session);
        } else {
            LOGGER.debug("Version found.");
            if (myVersion.compareTo(new Version(MyTunesRss.VERSION)) < 0) {
                LOGGER.debug("Migrating database.");
                session.executeStatement(new MigrationStatement());
                DatabaseBuilderTask.doCheckpoint(session, true);
            }
//            session.executeStatement(new TuneDatabaseStatement());
            MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
        }
        LOGGER.debug("Database now has version \"" + myVersion + "\".");
    }

    private void loadVersion(DataStoreSession session) throws SQLException {
        try {
            myVersion = session.executeQuery(new DataStoreQuery<Version>() {
                public Version execute(Connection connection) throws SQLException {
                    try {
                        ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "initialize").executeQuery();
                        if (resultSet.next()) {
                            resultSet = MyTunesRssUtils.createStatement(connection, "getSystemInformation").executeQuery();
                            if (resultSet.next()) {
                                return new Version(resultSet.getString("VERSION"));
                            }
                        }
                    } catch (SQLException e) {
                        // intentionally left blank
                    }
                    return null;
                }
            });
        } finally {
            DatabaseBuilderTask.doCheckpoint(session, true);
        }
    }

    public Version getDatabaseVersion() {
        return myVersion;
    }
}

