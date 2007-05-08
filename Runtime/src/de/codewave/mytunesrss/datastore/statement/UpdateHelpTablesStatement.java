package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class UpdateHelpTablesStatement implements DataStoreStatement {
    Map<String, String> myAlbumArtistMapping;

    public UpdateHelpTablesStatement(Collection<Map.Entry<String, String>> albumArtistMapping) {
        myAlbumArtistMapping = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : albumArtistMapping) {
            myAlbumArtistMapping.put(entry.getKey(), entry.getValue());
        }
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updateHelpTables");
        statement.setObject("mapping", myAlbumArtistMapping);
        statement.execute();
    }
}