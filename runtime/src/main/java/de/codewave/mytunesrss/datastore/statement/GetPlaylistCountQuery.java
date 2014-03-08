/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.ResultSetType;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class GetPlaylistCountQuery extends AbstractFindPlaylistQuery<Integer> {

    public GetPlaylistCountQuery(List<PlaylistType> types, String id, String containerId, boolean includeHidden) {
        super(types, id, containerId, includeHidden);
    }

    public GetPlaylistCountQuery(User user, List<PlaylistType> types, String id, String containerId, boolean includeHidden, boolean matchingOwnerOnly) {
        super(user, types, id, containerId, includeHidden, matchingOwnerOnly);
    }

    public Integer execute(Connection connection) throws SQLException {
        setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        Map<String, Boolean> conditionals = getConditionals();
        conditionals.put("selectAll", false);
        conditionals.put("selectId", true);
        SmartStatement statement = createStatement(connection, conditionals);
        ResultSet resultSet = statement.executeQuery();
        int count = 0;
        while (resultSet.next()) {
            count++;
        }
        return count;
    }

}
