/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveMyTunesSmartPlaylistStatement extends SavePlaylistStatement {
    private Collection<SmartInfo> mySmartInfos;
    private String myExecutedId;

    public SaveMyTunesSmartPlaylistStatement(String userName, boolean userPrivate, Collection<SmartInfo> smartInfos) {
        super(null);
        setType(PlaylistType.MyTunesSmart);
        setUserName(userName);
        setUserPrivate(userPrivate);
        mySmartInfos = smartInfos;
    }

    public void execute(Connection connection) throws SQLException {
        handleIdAndUpdate(connection);
        super.execute(connection);
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, isUpdate() ? "updateSmartInfo" : "insertSmartInfo");
        statement.setString("playlist_id", getId());
        Collection<List<Object>> params = new ArrayList<List<Object>>();
        for (SmartInfo smartInfo : mySmartInfos) {
            params.add(Arrays.asList(new Object[] {smartInfo.getFieldType().name(), smartInfo.getPattern(), smartInfo.isInvert()}));
        }
        statement.setObject("param", params);
        statement.execute();
        myExecutedId = getId();
    }

    public String getPlaylistIdAfterExecute() {
        if (myExecutedId == null) {
            throw new IllegalStateException("Statement not yet executed.");
        }
        return myExecutedId;
    }

    protected void handleIdAndUpdate(Connection connection) throws SQLException {
        if (StringUtils.isEmpty(myId)) {
            ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "nextPlaylistId").executeQuery();
            if (resultSet.next()) {
                int playlistId = resultSet.getInt("ID");
                setId("MyTunesRSS" + playlistId);
            }
        } else {
            setUpdate(true);
        }
    }
}
