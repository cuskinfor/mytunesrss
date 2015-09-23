package de.codewave.utils.sql;

import java.sql.*;

/**
 * de.codewave.utils.sql.DataStoreStatement
 */
public interface DataStoreStatement {
    void execute(Connection connection) throws SQLException;
}
