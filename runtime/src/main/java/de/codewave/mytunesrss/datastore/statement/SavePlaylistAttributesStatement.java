/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement
 */
public class SavePlaylistAttributesStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(SavePlaylistStatement.class);

    protected String myId;
    private String myName;
    private String myUserOwner;
    private boolean myUserPrivate;
    private boolean myHidden;

    public boolean isHidden() {
        return myHidden;
    }

    public void setHidden(boolean hidden) {
        myHidden = hidden;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public String getUserOwner() {
        return myUserOwner;
    }

    public void setUserOwner(String userOwner) {
        myUserOwner = userOwner;
    }

    public boolean isUserPrivate() {
        return myUserPrivate;
    }

    public void setUserPrivate(boolean userPrivate) {
        myUserPrivate = userPrivate;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updatePlaylistAttributes");
        statement.setString("id", myId);
        statement.setString("name", myName);
        statement.setBoolean("user_private", myUserPrivate);
        statement.setString("user_owner", myUserOwner);
        statement.setBoolean("hidden", myHidden);
        statement.execute();
    }
}