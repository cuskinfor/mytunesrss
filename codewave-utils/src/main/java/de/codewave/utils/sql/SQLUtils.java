package de.codewave.utils.sql;

/**
 * de.codewave.utils.sql.SQLUtils
 */
public class SQLUtils {
    public static String escapeLikeString(String aString, String escapeCharacter) {
        return aString.replace(escapeCharacter, escapeCharacter + escapeCharacter).replace("%", escapeCharacter + "%").replace("_", escapeCharacter + "_");
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
