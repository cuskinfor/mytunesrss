package de.codewave.utils.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.utils.sql.DataStoreStatement
 */
public interface DataStoreStatement {
    void execute(Connection connection) throws SQLException;
}
