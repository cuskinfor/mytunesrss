/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.ModalInfoDialog;
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

import javax.swing.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
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
                    session.commit();
                    loadVersion(session);
                } else {
                    LOGGER.debug("Version found.");
                    if (myVersion.compareTo(new Version(MyTunesRss.VERSION)) < 0) {
                        if (MyTunesRss.REGISTRATION.isExpiredVersion()) {
                            if (MyTunesRssUtils.isHeadless()) {
                                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.registrationExpiredVersion"));
                                MyTunesRssUtils.shutdownGracefully();
                            } else {
                                int result = JOptionPane.showConfirmDialog(null, MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.licenseExpiredVersion"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.title"), JOptionPane.YES_NO_OPTION);
                                if (result == JOptionPane.YES_OPTION) {
                                    MyTunesRssUtils.shutdownGracefully();
                                }
                            }
                        }
                        ModalInfoDialog info = new ModalInfoDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "taskinfo.migratingDatabase"));
                        info.show(2000L);
                        try {
                            session.executeStatement(new MigrationStatement());
                            session.commit();
                        } finally {
                            info.destroy();
                        }
                    }
                }
                session.commit();
                LOGGER.debug("Database successfully migrated.");
            } finally {
                session.rollback();
            }
        } catch (IOException e) {
            LOGGER.error("Could not initialize database.", e);
            MyTunesRss.STORE.destroy();
            MyTunesRss.STORE = new MyTunesRssDataStore();
            myException = e;
        } catch (SQLException e) {
            MyTunesRss.STORE.destroy();
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
            session.commit();
        }
    }

    public Version getDatabaseVersion() {
        return myVersion;
    }
}

