package de.codewave.utils.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
* Created by mdescher on 08.03.14.
*/
public class StandardQueryResult<E> implements QueryResult<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardQueryResult.class);

    private ResultSet myResultSet;
    private ResultBuilder<E> myResultBuilder;

    public StandardQueryResult(ResultSet resultSet, ResultBuilder<E>builder) {
        myResultSet = resultSet;
        myResultBuilder = builder;
    }

    public int getResultSize() {
        try {
            int row = myResultSet.getRow();
            myResultSet.last();
            int count = myResultSet.getRow();
            if (row > 0) {
                myResultSet.absolute(row);
            } else {
                myResultSet.beforeFirst();
            }
            return count;
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get result set size.", e);
            }
        }
        return 0;
    }

    public List<E> getResults() {
        return getResults(false);
    }

    public List<E> getResults(boolean keepResultSetOpen) {
        List<E> results = new ArrayList<E>();
        try {
            if (myResultSet.first()) {
                do {
                    results.add(myResultBuilder.create(myResultSet));
                } while (myResultSet.next());
            }
            if (!keepResultSetOpen) {
                close();
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while fetching results.", e);
            }
            close();
        }
        return results;
    }

    public List<E> getRemainingResults() {
        return getRemainingResults(false);
    }

    public List<E> getRemainingResults(boolean keepResultSetOpen) {
        List<E> results = new ArrayList<E>();
        try {
            while (myResultSet.next()) {
                results.add(myResultBuilder.create(myResultSet));
            }
            if (!keepResultSetOpen) {
                close();
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while fetching results.", e);
            }
            close();
        }
        return results;
    }

    public List<E> getNextResults(int count) {
        return getNextResults(count, false);
    }

    public List<E> getNextResults(int count, boolean keepResultSetOpen) {
        List<E> results = new ArrayList<E>();
        if (count > 0) {
            try {
                while (myResultSet.next() && count > 0) {
                    results.add(myResultBuilder.create(myResultSet));
                    count--;
                }
                if (!keepResultSetOpen) {
                    close();
                }
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while fetching results.", e);
                }
                close();
            }
        }
        return results;
    }

    public List<E> getResults(int start, int count) throws SQLException {
        return getResults(start, count, false);
    }

    public List<E> getResults(int start, int count, boolean keepResultSetOpen) throws SQLException {
        List<E> results = new ArrayList<E>();
        if (count > 0) {
            try {
                if (myResultSet.absolute(start + 1)) {
                    do {
                        results.add(myResultBuilder.create(myResultSet));
                        count--;
                    } while (myResultSet.next() && count > 0);
                }
                if (!keepResultSetOpen) {
                    close();
                }
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while fetching results.", e);
                }
                close();
            }
        }
        return results;
    }

    public void addResults(Collection<E> target) {
        addResults(target, false);
    }

    public void addResults(Collection<E> target, boolean keepResultSetOpen) {
        try {
            if (myResultSet.first()) {
                do {
                    target.add(myResultBuilder.create(myResultSet));
                } while (myResultSet.next());
            }
            if (!keepResultSetOpen) {
                close();
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while fetching results.", e);
            }
            close();
        }
    }

    public void addRemainingResults(Collection<E> target) {
        addRemainingResults(target, false);
    }

    public void addRemainingResults(Collection<E> target, boolean keepResultSetOpen) {
        try {
            while (myResultSet.next()) {
                target.add(myResultBuilder.create(myResultSet));
            }
            if (!keepResultSetOpen) {
                close();
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while fetching results.", e);
            }
            close();
        }
    }

    public void addNextResults(Collection<E> target, int count) {
        addNextResults(target, count, false);
    }

    public void addNextResults(Collection<E> target, int count, boolean keepResultSetOpen) {
        if (count > 0) {
            try {
                while (myResultSet.next() && count > 0) {
                    target.add(myResultBuilder.create(myResultSet));
                    count--;
                }
                if (!keepResultSetOpen) {
                    close();
                }
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while fetching results.", e);
                }
                close();
            }
        }
    }

    public void addResults(Collection<E> target, int start, int count) throws SQLException {
        addResults(target, start, count, false);
    }

    public void addResults(Collection<E> target, int start, int count, boolean keepResultSetOpen) throws SQLException {
        if (count > 0) {
            try {
                if (myResultSet.absolute(start + 1)) {
                    do {
                        target.add(myResultBuilder.create(myResultSet));
                        count--;
                    } while (myResultSet.next() && count > 0);
                }
                if (!keepResultSetOpen) {
                    close();
                }
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while fetching results.", e);
                }
                close();
            }
        }
    }

    public void processResults(DataStoreQuery.ResultProcessor<E> processor) {
        processResults(processor, false);
    }

    public void processResults(DataStoreQuery.ResultProcessor<E> processor, boolean keepResultSetOpen) {
        try {
            if (myResultSet.first()) {
                do {
                    processor.process(myResultBuilder.create(myResultSet));
                } while (myResultSet.next());
            }
            if (!keepResultSetOpen) {
                close();
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while fetching results.", e);
            }
            close();
        }
    }

    public void processRemainingResults(DataStoreQuery.ResultProcessor<E> processor) {
        processRemainingResults(processor, false);
    }

    public void processRemainingResults(DataStoreQuery.ResultProcessor<E> processor, boolean keepResultSetOpen) {
        try {
            while (myResultSet.next()) {
                processor.process(myResultBuilder.create(myResultSet));
            }
            if (!keepResultSetOpen) {
                close();
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while fetching results.", e);
            }
            close();
        }
    }

    public void processNextResults(DataStoreQuery.ResultProcessor<E> processor, int count) {
        processNextResults(processor, count, false);
    }

    public void processNextResults(DataStoreQuery.ResultProcessor<E> processor, int count, boolean keepResultSetOpen) {
        if (count > 0) {
            try {
                while (myResultSet.next() && count > 0) {
                    processor.process(myResultBuilder.create(myResultSet));
                    count--;
                }
                if (!keepResultSetOpen) {
                    close();
                }
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while fetching results.", e);
                }
                close();
            }
        }
    }

    public void processResults(DataStoreQuery.ResultProcessor<E> processor, int start, int count) throws SQLException {
        processResults(processor, start, count, false);
    }

    public void processResults(DataStoreQuery.ResultProcessor<E> processor, int start, int count, boolean keepResultSetOpen) throws SQLException {
        if (count > 0) {
            try {
                if (myResultSet.absolute(start + 1)) {
                    do {
                        processor.process(myResultBuilder.create(myResultSet));
                        count--;
                    } while (myResultSet.next() && count > 0);
                }
                if (!keepResultSetOpen) {
                    close();
                }
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while fetching results.", e);
                }
                close();
            }
        }
    }

    public void close() {
        if (myResultSet != null) {
            try {
                if (DataStore.OPEN_RESULT_SETS.remove(myResultSet)) {
                    LOGGER.debug("Closing result set (remaining open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
                }
                myResultSet.close();
            } catch (SQLException e) {
                LOGGER.warn("Could not close result set, will try again when transaction finishes.");
            }
        }
    }

    public void reset() {
        try {
            myResultSet.last();
            myResultSet.beforeFirst();
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not reset result set.", e);
            }
        }
    }

    public E nextResult() {
        try {
            if (myResultSet.next()) {
                return myResultBuilder.create(myResultSet);
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while fetching next result.", e);
            }
        }
        return null;
    }

    public E getResult(int index) {
        try {
            if (myResultSet.absolute(index + 1)) {
                return myResultBuilder.create(myResultSet);
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while fetching next result.", e);
            }
        }
        return null;
    }
}
