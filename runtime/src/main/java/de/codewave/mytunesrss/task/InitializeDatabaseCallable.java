/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement;
import de.codewave.mytunesrss.datastore.statement.MigrationStatement;
import de.codewave.utils.Version;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseCallable
 */
public class InitializeDatabaseCallable implements Callable<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeDatabaseCallable.class);

    private Version myVersion;
    private Exception myException;

    public Void call() throws IOException, SQLException {
        try {
            LOGGER.debug("Initializing the database.");
            MyTunesRss.STORE.init();
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            try {
                loadVersion(session);
                if (myVersion == null) {
                    LOGGER.debug("No version found. Creating all tables.");
                    session.executeStatement(new CreateAllTablesStatement());
                    DatabaseBuilderCallable.doCheckpoint(session, true);
                    loadVersion(session);
                } else {
                    LOGGER.debug("Version found.");
                    if (myVersion.compareTo(new Version(MyTunesRss.VERSION)) < 0) {
                        session.executeStatement(new MigrationStatement());
                        DatabaseBuilderCallable.doCheckpoint(session, true);
                    }
                    MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
                }
                session.commit();
                LOGGER.debug("Database now has version \"" + myVersion + "\".");
            } finally {
                session.rollback();
            }
        } catch (IOException e) {
            LOGGER.error("Could not initialize database.", e);
            MyTunesRss.STORE = new MyTunesRssDataStore();
            myException = e;
        } catch (SQLException e) {
            MyTunesRss.STORE = new MyTunesRssDataStore();
            LOGGER.error("Could not initialize database.", e);
            myException = e;
        }
        return null;
    }

    /**
     * Get the exception that occurred in the initialiazation task.
     *
     * @return The initializaton task exception or NULL if no exception occurred.
     */
    public Exception getException() {
        return myException;
    }

    private void loadVersion(DataStoreSession session) throws SQLException {
        try {
            myVersion = session.executeQuery(new DataStoreQuery<Version>() {
                public Version execute(Connection connection) throws SQLException {
                    try {
                        ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "initialize").executeQuery();
                        if (resultSet.next()) {
                            resultSet = MyTunesRssUtils.createStatement(connection, "getVersion").executeQuery();
                            if (resultSet.next()) {
                                return new Version(resultSet.getString("version"));
                            }
                        }
                    } catch (SQLException e) {
                        // intentionally left blank
                    }
                    return null;
                }
            });
        } finally {
            DatabaseBuilderCallable.doCheckpoint(session, true);
        }
    }

    public Version getDatabaseVersion() {
        return myVersion;
    }
}

