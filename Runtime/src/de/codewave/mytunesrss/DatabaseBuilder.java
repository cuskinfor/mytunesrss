/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

import javax.swing.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.DatabaseBuilder
 */
public class DatabaseBuilder {
    private static final Log LOG = LogFactory.getLog(DatabaseBuilder.class);

    private JPanel myRootPanel;
    private JProgressBar myProgress;
    private String myLibraryXml;

    public boolean needsCreation() {
        try {
            return MyTunesRss.STORE.executeQuery(new DataStoreQuery<Boolean>() {
                public Collection<Boolean> execute(Connection connection) throws SQLException {
                    connection.createStatement().executeQuery("SELECT COUNT(*) FROM track");
                    return Collections.singletonList(Boolean.FALSE);
                }
            }).iterator().next();
        } catch (SQLException e) {
            return true;
        }
    }

    public boolean needsUpdate(URL iTunesLibraryXml) throws SQLException {
        if (iTunesLibraryXml != null) {
            if (myLibraryXml == null) {
                myLibraryXml = iTunesLibraryXml.getPath();
            }
            if (needsCreation() || !myLibraryXml.equals(iTunesLibraryXml.getPath())) {
                return true;
            }
            Collection<Long> lastUpdate = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Long>() {
                public Collection<Long> execute(Connection connection) throws SQLException {
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT lastupdate FROM itunes");
                    if (resultSet.next()) {
                        return Collections.singletonList(resultSet.getLong(1));
                    }
                    return null;
                }
            });
            if (lastUpdate == null || lastUpdate.isEmpty() || new File(iTunesLibraryXml.getPath()).lastModified() > lastUpdate.iterator().next()) {
                return true;
            }
        }
        return false;
    }

    public void createDatabase() throws SQLException {
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        try {
            storeSession.executeStatement(new CreateAllTablesStatement());
            storeSession.commit();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create tables.", e);
            }
            storeSession.rollback();
            throw e;
        }
    }

    public void loadFromITunes(JFrame frame, final URL iTunesLibraryXml) throws SQLException {
        final JDialog dialog = new JDialog(frame, null, true);
        myProgress.setValue(0);
        myProgress.setString("0 %");
        new Thread(new Runnable() {
            public void run() {
                DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
                storeSession.begin();
                try {
                    if (needsCreation()) {
                        createDatabase();
                    }
                    storeSession.executeStatement(new ClearAllTablesStatement());
                    final long updateTime = System.currentTimeMillis();
                    ITunesUtils.loadFromITunes(iTunesLibraryXml, storeSession, myProgress);
                    storeSession.executeStatement(new DataStoreStatement() {
                        public void execute(Connection connection) throws SQLException {
                            connection.createStatement().execute("UPDATE itunes SET lastupdate = " + updateTime);
                        }
                    });
                    storeSession.commit();
                    myLibraryXml = iTunesLibraryXml.getPath();
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not load iTunes library.", e);
                    }
                    try {
                        storeSession.rollback();
                    } catch (SQLException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not rollback transaction.", e1);
                        }
                    }
                } finally {
                    if (dialog != null) {
                        dialog.dispose();
                    }
                }
            }
        }).start();
        dialog.add(myRootPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
}