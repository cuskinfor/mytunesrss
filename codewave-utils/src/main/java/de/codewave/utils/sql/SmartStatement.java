package de.codewave.utils.sql;

import de.codewave.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.utils.sql.SmartStatement
 */
public class SmartStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmartStatement.class);

    private String myName;
    private final SmartStatementDescription myDescription;
    private final List<SmartSql> mySmartSqls;
    private final PreparedStatement[] myPreparedStatements;
    private final Map<String, Object> myParameters = new HashMap<String, Object>();
    private final Connection myConnection;
    private final Map<PreparedStatement, Set<String>> myStatementParameters = new WeakHashMap<PreparedStatement, Set<String>>();
    private final Map<String, Integer> myCollectionSizes = new HashMap<String, Integer>();
    private final Map<String, Boolean> myConditionals;

    SmartStatement(String name, Connection connection, SmartStatementDescription description, Map<String, Boolean> conditionals) throws SQLException {
        myName = name;
        myDescription = description;
        mySmartSqls = description.getSqls();
        myConnection = connection;
        myConditionals = conditionals;
        myPreparedStatements = new PreparedStatement[mySmartSqls.size()];
    }

    public void clearParameters() throws SQLException {
        for (PreparedStatement preparedStatement : myPreparedStatements) {
            if (preparedStatement != null) {
                preparedStatement.clearParameters();
            }
        }
        myParameters.clear();
        myStatementParameters.clear();
        myCollectionSizes.clear();
    }

    public void setBoolean(String name, boolean value) throws SQLException {
        setObject(name, value);
    }

    public void setInt(String name, int value) throws SQLException {
        setObject(name, value);
    }

    public void setLong(String name, long value) throws SQLException {
        setObject(name, value);
    }

    public void setObject(String name, Object value) throws SQLException {
        myParameters.put(name, value);
        for (int s = 0; s < myPreparedStatements.length; s++) {
            SmartSql smartSql = mySmartSqls.get(s);
            PreparedStatement statement = myPreparedStatements[s];
            if (statement != null) {
                setStatementParameters(smartSql, statement, name, value);
            }
        }
    }

    public <T> void setItems(String name, Iterable<T> iterable) throws SQLException {
        int index = 0;
        if (iterable != null) {
            for (T item : iterable) {
                setObject(name + "[" + (index++) + "]", item);
            }
        }
        setInt("size_of_" + name, index);
        myCollectionSizes.put(name, index);
    }

    public <T> void setItems(String name, T[] array) throws SQLException {
        int index = 0;
        if (array != null) {
            for (T item : array) {
                setObject(name + "[" + (index++) + "]", item);
            }
        }
        setInt("size_of_" + name, index);
        myCollectionSizes.put(name, index);
    }

    private void setStatementParameters(SmartSql smartSql, PreparedStatement statement, String name, Object value) throws SQLException {
        if (smartSql.isParameterName(name)) {
            rememberStatementParameter(statement, name);
            for (Integer i : smartSql.getIndexListForParameter(name)) {
                setStatementParameter(statement, name, i, value);
            }
        }
    }

    private void rememberStatementParameter(PreparedStatement statement, String name) {
        Set<String> names = myStatementParameters.get(statement);
        if (names == null) {
            names = new HashSet<String>();
            myStatementParameters.put(statement, names);
        }
        names.add(name);
    }

    private void setStatementParameter(PreparedStatement statement, String name, int i, Object value) throws SQLException {
        Class clazz = String.class;
        String className = myDescription.getParamType(name);
        if (StringUtils.isNotEmpty(className)) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not get class for parameter type \"" + myDescription.getParamType(name) + "\".");
                }
            }
        } else if (value != null) {
            clazz = value.getClass();
        }
        if (String.class.equals(clazz)) {
            // always use composed form of UTF-8 since at least MySQL on Linux cannot handle the decomposed form
            statement.setString(i, MiscUtils.compose((String)value));
        } else if (Integer.class.equals(clazz) || Integer.TYPE.equals(clazz)) {
            if (value != null) {
                statement.setInt(i, (Integer)value);
            } else {
                statement.setNull(i, Types.INTEGER);
            }
        } else if (String.class.equals(clazz) || Boolean.TYPE.equals(clazz)) {
            if (value != null) {
                statement.setBoolean(i, (Boolean)value);
            } else {
                statement.setNull(i, Types.BOOLEAN);
            }
        } else {
            statement.setObject(i, value);
        }
    }

    public void setString(String name, String value) throws SQLException {
        setObject(name, value);
    }

    public void execute() throws SQLException {
        for (int i = 0; i < myPreparedStatements.length; i++) {
            execute(i);
        }
    }

    private void ensureAllParametersSet(int index) {
        SmartSql smartSql = mySmartSqls.get(index);
        for (String name : smartSql.getParameterNames()) {
            if (myStatementParameters.isEmpty() || !myStatementParameters.get(myPreparedStatements[index]).contains(name)) {
                try {
                    setObject(name, myDescription.getDefaults().get(name));
                } catch (SQLException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could not set parameter!", e);
                    }
                }
            }
        }
    }

    private void execute(int sqlIndex) throws SQLException {
        SmartSql sql = mySmartSqls.get(sqlIndex);
        PreparedStatement statement = getPreparedStatement(ResultSetType.TYPE_FORWARD_ONLY, sqlIndex);
        statement.setFetchSize(1);
        String loopParameter = sql.getLoopParameter();
        if (StringUtils.isEmpty(loopParameter)) {
            ensureAllParametersSet(sqlIndex);
            LOGGER.trace("Executing SQL statement [" + myName + "]: \"{}\".", statement);
            statement.execute();
            if (statement.getResultSet() != null) {
                DataStore.OPEN_RESULT_SETS.add(statement.getResultSet());
                LOGGER.debug("Opening result set (now open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
            }
        } else {
            Object loopObject = myParameters.get(loopParameter);
            int loopIndex = 0;
            if (loopObject != null && loopObject instanceof Collection && !((Collection)loopObject).isEmpty()) {
                LOGGER.debug("Executing " + ((Collection)loopObject).size() + " statements in a loop.");
                for (Object value : (Collection)loopObject) {
                    if (value instanceof Collection) {
                        Iterator iter = ((Collection)value).iterator();
                        for (int i = 0; i < ((Collection)value).size(); i++) {
                            setStatementParameters(sql, statement, "loopitem_" + loopParameter + "[" + i + "]", iter.next());
                        }
                    } else {
                        setStatementParameters(sql, statement, "loopitem_" + loopParameter, value);
                    }
                    setStatementParameters(sql, statement, "loopindex_" + loopParameter, loopIndex++);
                    ensureAllParametersSet(sqlIndex);
                    executeSqlForLoop(sql, statement, loopIndex);
                }
                if (sql.getLoopBatchCount() > 0 && loopIndex % sql.getLoopBatchCount() != 0) {
                    LOGGER.debug("Executing loop batch after " + loopIndex + " statements.");
                    executeBatch(statement);
                }
            } else if (loopObject != null && loopObject instanceof Map && !((Map) loopObject).isEmpty()) {
                for (Object entry : ((Map)loopObject).entrySet()) {
                    setStatementParameters(sql, statement, "loopitem_" + loopParameter + "_key", ((Map.Entry)entry).getKey());
                    setStatementParameters(sql, statement, "loopitem_" + loopParameter + "_value", ((Map.Entry)entry).getValue());
                    setStatementParameters(sql, statement, "loopindex_" + loopParameter, loopIndex++);
                    ensureAllParametersSet(sqlIndex);
                    executeSqlForLoop(sql, statement, loopIndex);
                }
                if (sql.getLoopBatchCount() > 0 && loopIndex % sql.getLoopBatchCount() != 0) {
                    LOGGER.debug("Executing loop batch after " + loopIndex + " statements.");
                    executeBatch(statement);
                }
            }
        }
    }

    private void executeSqlForLoop(SmartSql sql, PreparedStatement statement, int loopIndex) throws SQLException {
        if (sql.getLoopBatchCount() > 0) {
            statement.addBatch();
            if (loopIndex % sql.getLoopBatchCount() == 0) {
                LOGGER.debug("Executing loop batch after " + loopIndex + " statements.");
                executeBatch(statement);
            }
        } else {
            statement.execute();
            if (statement.getResultSet() != null) {
                DataStore.OPEN_RESULT_SETS.add(statement.getResultSet());
                LOGGER.debug("Opening result set (now open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
            }
        }
        if (sql.getLoopCommitCount() > 0 && loopIndex % sql.getLoopCommitCount() == 0) {
            LOGGER.debug("Committing loop transaction after " + loopIndex + " statements.");
            myConnection.commit();
        }
    }

    private void executeBatch(PreparedStatement statement) throws SQLException {
        long start = System.currentTimeMillis();
        try {
            statement.executeBatch();
            LOGGER.debug("Batch executed in " + (System.currentTimeMillis() - start) + " milliseconds.");
        } catch (SQLException e) {
            SQLException nextException = e.getNextException();
            throw nextException != null ? nextException : e;
        }
    }

    public void execute(SmartStatementExceptionHandler handler) {
        for (int i = 0; i < myPreparedStatements.length; i++) {
            try {
                execute(i);
            } catch (SQLException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Handling SQLException during statement execution.");
                }
                handler.handleException(e, i == myPreparedStatements.length - 1);
            }
        }
    }

    public ResultSet executeQuery() throws SQLException {
        return executeQuery(ResultSetType.TYPE_SCROLL_INSENSITIVE, 0);
    }

    public ResultSet executeQuery(ResultSetType resultSetType, int fetchSize) throws SQLException {
        for (int i = 0; i < myPreparedStatements.length - 1; i++) {
            execute(i);
        }
        PreparedStatement statement = getPreparedStatement(resultSetType, myPreparedStatements.length - 1);
        ensureAllParametersSet(myPreparedStatements.length - 1);
        statement.setFetchSize(fetchSize);
        if (statement.getResultSet() != null) {
            try {
                if (DataStore.OPEN_RESULT_SETS.remove(statement.getResultSet())) {
                    LOGGER.debug("Closing result set (remaining open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
                }
                statement.getResultSet().close();
            } catch (SQLException e) {
                LOGGER.warn("Could not close result set.", e);
            }
        }
        LOGGER.trace("Executing SQL query [" + myName + "]: \"{}\".", statement);
        ResultSet resultSet = statement.executeQuery();
        DataStore.OPEN_RESULT_SETS.add(resultSet);
        LOGGER.debug("Opening result set (now open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
        return resultSet;
    }

    public ResultSet executeQuery(SmartStatementExceptionHandler handler) {
        return executeQuery(handler, ResultSetType.TYPE_SCROLL_INSENSITIVE, 0);
    }

    public ResultSet executeQuery(SmartStatementExceptionHandler handler, ResultSetType resultSetType, int fetchSize) {
        for (int i = 0; i < myPreparedStatements.length - 1; i++) {
            try {
                execute(i);
            } catch (SQLException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Handling SQLException during statement execution.");
                }
                handler.handleException(e, false);
            }
        }
        try {
            PreparedStatement statement = getPreparedStatement(resultSetType, myPreparedStatements.length - 1);
            ensureAllParametersSet(myPreparedStatements.length - 1);
            statement.setFetchSize(fetchSize);
            if (statement.getResultSet() != null) {
                try {
                    if (DataStore.OPEN_RESULT_SETS.remove(statement.getResultSet())) {
                        LOGGER.debug("Closing result set (remaining open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
                    }
                    statement.getResultSet().close();
                } catch (SQLException e) {
                    LOGGER.warn("Could not close result set.", e);
                }
            }
            LOGGER.trace("Executing SQL query [" + myName + "]: \"{}\".", statement);
            ResultSet resultSet = statement.executeQuery();
            DataStore.OPEN_RESULT_SETS.add(resultSet);
            LOGGER.debug("Opening result set (now open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
            return resultSet;
        } catch (SQLException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Handling SQLException during statement execution.");
            }
            handler.handleException(e, true);
        }
        return null;
    }

    private PreparedStatement getPreparedStatement(ResultSetType resultSetType, int index) throws SQLException {
        PreparedStatement preparedStatement = myPreparedStatements[index];
        if (preparedStatement == null || preparedStatement.getResultSetType() != resultSetType.getJdbcType() || preparedStatement.getResultSetConcurrency() != resultSetType.getJdbcConcurrency()) {
            preparedStatement = mySmartSqls.get(index).prepareStatement(myConnection, myCollectionSizes, myConditionals, resultSetType);
            for (Map.Entry<String, Object> entry : myParameters.entrySet()) {
                setStatementParameters(mySmartSqls.get(index), preparedStatement, entry.getKey(), entry.getValue());
            }
            myPreparedStatements[index] = preparedStatement;
        }
        return preparedStatement;
    }
}
