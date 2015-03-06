package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Rename all genres in the track table according to the specified rules and
 * recreate the genres help table.
 */
public class RenameGenresStatement implements DataStoreStatement {

    private Map<String, String> myMappings;

    public RenameGenresStatement(Map<String, String> mappings) {
        myMappings = mappings;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        StopWatch.start("Renaming genres");
        try {
            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "renameGenres");
            statement.setObject("mappings", myMappings);
            statement.execute();
        } finally {
            StopWatch.stop();
        }
        new RecreateHelpTablesStatement(false, false, true, true).execute(connection);
    }

}
