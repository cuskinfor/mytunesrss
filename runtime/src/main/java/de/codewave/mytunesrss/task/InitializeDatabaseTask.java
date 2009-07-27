/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssTask;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement;
import de.codewave.mytunesrss.datastore.statement.MigrationStatement;
import de.codewave.utils.Version;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class InitializeDatabaseTask extends MyTunesRssTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeDatabaseTask.class);
    
    private Version myVersion;
    private Exception myException;
    private boolean myThrowException;

    public InitializeDatabaseTask() {
        this(false);
    }
    
    public InitializeDatabaseTask(boolean throwException) {
        myThrowException = throwException;
    }
    
    public void execute() {
        try {
            MyTunesRss.STORE = new MyTunesRssDataStore();
            MyTunesRss.STORE.init();
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            loadVersion(session);
            if (myVersion == null) {
                session.executeStatement(new CreateAllTablesStatement());
                DatabaseBuilderTask.doCheckpoint(session, true);
                loadVersion(session);
            } else {
                if (myVersion.compareTo(new Version(MyTunesRss.VERSION)) < 0) {
                    session.executeStatement(new MigrationStatement());
                    DatabaseBuilderTask.doCheckpoint(session, true);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not initialize database.", e);
            MyTunesRss.STORE = null;
            myException = e;
        } catch (SQLException e) {
            MyTunesRss.STORE = null;
            LOGGER.error("Could not initialize database.", e);
            myException = e;
        }
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

