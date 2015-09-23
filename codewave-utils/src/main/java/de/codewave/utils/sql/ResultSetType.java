package de.codewave.utils.sql;

import java.sql.ResultSet;

/**
 * Enum wrapper for JDBC result set types.
 */
public enum ResultSetType {
    TYPE_FORWARD_ONLY(), TYPE_SCROLL_INSENSITIVE(), TYPE_SCROLL_SENSITIVE(),
    TYPE_FORWARD_ONLY_UPDATABLE(), TYPE_SCROLL_INSENSITIVE_UPDATABLE(), TYPE_SCROLL_SENSITIVE_UPDATABLE();

    public int getJdbcType() {
        switch (this) {
            case TYPE_FORWARD_ONLY:
            case TYPE_FORWARD_ONLY_UPDATABLE:
                return ResultSet.TYPE_FORWARD_ONLY;
            case TYPE_SCROLL_INSENSITIVE:
            case TYPE_SCROLL_INSENSITIVE_UPDATABLE:
                return ResultSet.TYPE_SCROLL_INSENSITIVE;
            case TYPE_SCROLL_SENSITIVE:
            case TYPE_SCROLL_SENSITIVE_UPDATABLE:
                return ResultSet.TYPE_SCROLL_SENSITIVE;
            default:
                throw new RuntimeException("Unsupported ResultSetType \"" + name() + "\".");
        }
    }
    
    public int getJdbcConcurrency() {
        switch (this) {
            case TYPE_FORWARD_ONLY:
            case TYPE_SCROLL_INSENSITIVE:
            case TYPE_SCROLL_SENSITIVE:
                return ResultSet.CONCUR_READ_ONLY;
            case TYPE_FORWARD_ONLY_UPDATABLE:
            case TYPE_SCROLL_INSENSITIVE_UPDATABLE:
            case TYPE_SCROLL_SENSITIVE_UPDATABLE:
                return ResultSet.CONCUR_UPDATABLE;
            default:
                throw new RuntimeException("Unsupported ResultSetType \"" + name() + "\".");
        }
    }
}
