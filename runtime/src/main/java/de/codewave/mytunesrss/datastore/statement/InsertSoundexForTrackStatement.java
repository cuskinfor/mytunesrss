package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertSoundexForTrackStatement
 */
public class InsertSoundexForTrackStatement implements DataStoreStatement {

    private String myTrackId;
    private Collection<String> mySoundexCodes;

    public InsertSoundexForTrackStatement(String trackId, Collection<String> soundexCodes) {
        myTrackId = trackId;
        mySoundexCodes = soundexCodes;
    }

    protected String getStatementName() {
        return "insertSoundexForTrack";
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, getStatementName());
        statement.setString("track_id", myTrackId);
        statement.setObject("code", mySoundexCodes);
        statement.execute();
    }
}