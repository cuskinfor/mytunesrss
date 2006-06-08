/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.datastore.*;

import java.util.*;
import java.sql.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class InitializeDatabaseTask extends PleaseWait.NoCancelTask {
    private static final Log LOG = LogFactory.getLog(InitializeDatabaseTask.class);

    private boolean myExistent;

    public void execute() {
        try {
            MyTunesRss.STORE.executeQuery(new DataStoreQuery<Boolean>() {
                public Boolean execute(Connection connection) throws SQLException {
                    ResultSet resultSet = connection.createStatement().executeQuery(
                            "SELECT COUNT(*) FROM information_schema.system_tables WHERE table_schem = 'PUBLIC' AND table_name = 'TRACK'");
                    if (resultSet.next() && resultSet.getInt(1) == 1) {
                        myExistent = true;
                        return Boolean.TRUE;
                    }
                    myExistent = false;
                    return Boolean.FALSE;
                }
            });
            if (!myExistent) {
                DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
                storeSession.begin();
                try {
                    storeSession.executeStatement(new CreateAllTablesStatement());
                    storeSession.commit();
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not create tables.", e);
                    }
                    try {
                        storeSession.rollback();
                    } catch (SQLException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not rollback transaction.", e1);
                        }
                    }
                }
            } else {
                DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
                storeSession.begin();
                try {
                    storeSession.executeStatement(new MigrationStatement());
                    storeSession.commit();
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not migrate database.", e);
                    }
                    try {
                        storeSession.rollback();
                    } catch (SQLException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not rollback transaction.", e1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            myExistent = false;
        }
    }

}

