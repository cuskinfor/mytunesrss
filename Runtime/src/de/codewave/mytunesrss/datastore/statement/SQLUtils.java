/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SQLUtils
 */
public class SQLUtils {
    public static String escapeLikeString(String aString) {
        return aString.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public static String createParameters(int parameterCount) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < parameterCount; i++) {
            buffer.append("?");
            if (i + 1 < parameterCount) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }
}