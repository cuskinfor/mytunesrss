/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertOrUpdateTrackStatement
 */
public abstract class InsertOrUpdatePhotoStatement implements DataStoreStatement {

    private String myId;
    private String myName;
    private long myDate;
    private String myFile;
    private SmartStatement myStatement;

    public void setId(String id) {
        myId = id;
    }

    public void setName(String name) {
        myName = name;
    }

    public void setDate(long date) {
        myDate = date;
    }

    public void setFile(String file) {
        myFile = file;
    }

    public void setStatement(SmartStatement statement) {
        myStatement = statement;
    }

    public synchronized void execute(Connection connection) throws SQLException {
        try {
            if (myStatement == null) {
                myStatement = MyTunesRssUtils.createStatement(connection, getStatementName());
            }
            myStatement.clearParameters();
            myStatement.setString("id", myId);
            myStatement.setString("name", myName);
            myStatement.setLong("date", myDate);
            myStatement.setString("file", myFile);
            myStatement.execute();
        } catch (SQLException e) {
            logError(myId, e);
        }
    }

    protected abstract void logError(String id, SQLException e);

    protected abstract String getStatementName();

    public void clear() {
        myId = null;
        myName = null;
        myDate = 0L;
        myFile = null;
    }
}