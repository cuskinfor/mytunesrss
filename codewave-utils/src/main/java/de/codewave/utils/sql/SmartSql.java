/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.sql;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * de.codewave.utils.sql.SmartSql
 */
public class SmartSql {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmartSql.class);

    private static final Pattern PATTERN = Pattern.compile(":[a-zA-Z][a-zA-Z0-9_]*(\\[[0-9]*\\])?");
    private static final Pattern PATTERN_DYNAMIC = Pattern.compile(":[a-zA-Z][a-zA-Z0-9_]*\\[\\]");
    private static final Pattern PATTERN_CONDITIONAL_NAME = Pattern.compile("</([a-zA-Z0-9_]+)>");

    private String myTemplateSql;
    private String mySql;
    private String myLoopParameter;
    private int myLoopBatchCount;
    private int myLoopCommitCount;
    private Map<String, Collection<Integer>> myParamMapping = new HashMap<>();
    private Set<String> myConditionalNames = new HashSet<>();

    SmartSql(SmartSql other) {
        myTemplateSql = other.myTemplateSql;
        mySql = other.mySql;
        myLoopParameter = other.myLoopParameter;
        myLoopBatchCount = other.myLoopBatchCount;
        myLoopCommitCount = other.myLoopCommitCount;
        myParamMapping = new HashMap<>();
        for (Map.Entry<String, Collection<Integer>> entry : other.myParamMapping.entrySet()) {
            myParamMapping.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        initConditionalNames();
    }

    SmartSql(String sql, String loopParameter, int loopBatchCount, int loopCommitCount) {
        myTemplateSql = sql;
        myLoopParameter = loopParameter;
        myLoopBatchCount = loopBatchCount;
        myLoopCommitCount = loopCommitCount;
        if (loopBatchCount != 0 && loopCommitCount != 0 && (loopCommitCount < loopBatchCount || loopCommitCount % loopBatchCount != 0)) {
            throw new IllegalArgumentException("Loop commit count (was " + loopCommitCount + ") must be greater than and a multiple of loop batch count (" + loopBatchCount + ").");
        }
        if (!PATTERN_DYNAMIC.matcher(sql).find()) {
            mySql = generateSqlAndMappings(myTemplateSql);
        }
        initConditionalNames();
    }

    private void initConditionalNames() {
        Matcher matcher = PATTERN_CONDITIONAL_NAME.matcher(myTemplateSql);
        while (matcher.find()) {
            myConditionalNames.add(matcher.group(1));
        }
    }

    private String generateSqlAndMappings(String sql) {
        myParamMapping.clear();
        Matcher matcher = PATTERN.matcher(sql);
        int i = 1;
        while (matcher.find()) {
            String match = matcher.group();
            String paramName = match.substring(1);
            Collection<Integer> indexList = myParamMapping.get(paramName);
            if (indexList == null) {
                indexList = new HashSet<>();
                myParamMapping.put(paramName, indexList);
            }
            indexList.add(i++);
        }
        sql = sql.replace((char) 10, ' ');
        sql = sql.replace((char) 13, ' ');
        // We have to process the keys in descending key length order to make sure we don't replace parts of keys where
        // two keys start with the same prefix and the shorter one is processed first!
        List<String> paramNames = new ArrayList<>(myParamMapping.keySet());
        Collections.sort(paramNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        for (String paramName : paramNames) {
            sql = sql.replace(":" + paramName, "?");
        }
        return sql;
    }

    public String getSql() {
        return mySql;
    }

    String getLoopParameter() {
        return myLoopParameter;
    }

    public int getLoopBatchCount() {
        return myLoopBatchCount;
    }

    public int getLoopCommitCount() {
        return myLoopCommitCount;
    }

    Collection<String> getParameterNames() {
        return new ArrayList<>(myParamMapping.keySet());
    }

    boolean isParameterName(String parameterName) {
        return myParamMapping.keySet().contains(parameterName);
    }

    Collection<Integer> getIndexListForParameter(String parameterName) {
        if (isParameterName(parameterName)) {
            return new ArrayList<>(myParamMapping.get(parameterName));
        }
        return null;
    }

    PreparedStatement prepareStatement(Connection connection, Map<String, Boolean> conditionals, ResultSetType resultSetType) throws SQLException {
        if (!PATTERN_DYNAMIC.matcher(myTemplateSql).find()) {
            String sql = myConditionalNames.isEmpty() ? mySql : generateSqlAndMappings(handleConditionals(myTemplateSql, conditionals));
            PreparedStatement preparedStatement = connection.prepareStatement(sql,
                    resultSetType.getJdbcType(),
                    resultSetType.getJdbcConcurrency(),
                    ResultSet.CLOSE_CURSORS_AT_COMMIT);
            DataStore.addPreparedStatement(connection, preparedStatement);
            return preparedStatement;
        }
        return null;
    }

    PreparedStatement prepareStatement(Connection connection, Map<String, Integer> collectionSizes, Map<String, Boolean> conditionals, ResultSetType resultSetType) throws SQLException {
        String templateSql = new String(myTemplateSql);
        for (Map.Entry<String, Integer> entry : collectionSizes.entrySet()) {
            while (templateSql.contains(":" + entry.getKey() + "[]")) {
                StringBuffer buffer = new StringBuffer();
                if (entry.getValue() > 0) {
                    for (int i = 0; i < entry.getValue(); i++) {
                        buffer.append(", :").append(entry.getKey()).append("[").append(i).append("]");
                    }
                } else {
                    buffer.append(", null");
                }
                templateSql = templateSql.replace(":" + entry.getKey() + "[]", buffer.substring(2));
            }
        }
        String sql = generateSqlAndMappings(handleConditionals(templateSql, conditionals));
        PreparedStatement preparedStatement = connection.prepareStatement(sql,
                resultSetType.getJdbcType(),
                resultSetType.getJdbcConcurrency(),
                ResultSet.CLOSE_CURSORS_AT_COMMIT);
        DataStore.addPreparedStatement(connection, preparedStatement);
        return preparedStatement;
    }

    private String handleConditionals(String input, Map<String, Boolean> conditionals) {
        for (String conditionalName : myConditionalNames) {
            String startTag = "<" + conditionalName + ">";
            String endTag = "</" + conditionalName + ">";
            String[] strings = StringUtils.substringsBetween(input, startTag, endTag);
            if (strings != null) {
                for (String token : strings) {
                    input = StringUtils.replace(input, startTag + token + endTag, conditionals.get(conditionalName) ? token : "");
                }
            }
        }
        return input;
    }
}
