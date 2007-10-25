package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistIdsQuery
 */
public class FindPlaylistIdsQuery extends DataStoreQuery<Collection<String>> {
    private Collection<String> myTypes;


    public FindPlaylistIdsQuery(String... types) {
        if (types != null && types.length > 0) {
            myTypes = Arrays.asList(types);
        }
    }

    public FindPlaylistIdsQuery(Collection<String> types) {
        myTypes = types;
    }

    public Collection<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findPlaylistIds");
        statement.setItems("types", myTypes);
        return execute(statement, new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString("ID");
            }
        }).getResults();
    }
}