package de.codewave.utils.sql;

import java.sql.*;

/**
 * de.codewave.utils.sql.ResultBuilder
 */
public interface ResultBuilder<T> {
    T create(ResultSet resultSet) throws SQLException;
}
