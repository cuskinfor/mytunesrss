package de.codewave.utils.sql;

import java.sql.SQLException;

/**
 * de.codewave.utils.sql.SmartStatementExceptionHandler
 */
public interface SmartStatementExceptionHandler {
    void handleException(SQLException e, boolean lastStatement);
}