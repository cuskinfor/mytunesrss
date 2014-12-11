package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;

public class FindTracksWithMissingContentTypeQuery extends DataStoreQuery<QueryResult<Track>> {

    @Override
    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        statement = MyTunesRssUtils.createStatement(connection, "findTracksWithMissingContentType");
        return execute(statement, new TrackResultBuilder());
    }
}
