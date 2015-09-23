package de.codewave.utils.sql;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface QueryResult<E> {
    int getResultSize();

    List<E> getResults();

    List<E> getResults(boolean keepResultSetOpen);

    List<E> getRemainingResults();

    List<E> getRemainingResults(boolean keepResultSetOpen);

    List<E> getNextResults(int count);

    List<E> getNextResults(int count, boolean keepResultSetOpen);

    List<E> getResults(int start, int count) throws SQLException;

    List<E> getResults(int start, int count, boolean keepResultSetOpen) throws SQLException;

    void addResults(Collection<E> target);

    void addResults(Collection<E> target, boolean keepResultSetOpen);

    void addRemainingResults(Collection<E> target);

    void addRemainingResults(Collection<E> target, boolean keepResultSetOpen);

    void addNextResults(Collection<E> target, int count);

    void addNextResults(Collection<E> target, int count, boolean keepResultSetOpen);

    void addResults(Collection<E> target, int start, int count) throws SQLException;

    void addResults(Collection<E> target, int start, int count, boolean keepResultSetOpen) throws SQLException;

    void processResults(DataStoreQuery.ResultProcessor<E> processor);

    void processResults(DataStoreQuery.ResultProcessor<E> processor, boolean keepResultSetOpen);

    void processRemainingResults(DataStoreQuery.ResultProcessor<E> processor);

    void processRemainingResults(DataStoreQuery.ResultProcessor<E> processor, boolean keepResultSetOpen);

    void processNextResults(DataStoreQuery.ResultProcessor<E> processor, int count);

    void processNextResults(DataStoreQuery.ResultProcessor<E> processor, int count, boolean keepResultSetOpen);

    void processResults(DataStoreQuery.ResultProcessor<E> processor, int start, int count) throws SQLException;

    void processResults(DataStoreQuery.ResultProcessor<E> processor, int start, int count, boolean keepResultSetOpen) throws SQLException;

    void close();

    void reset();

    E nextResult();

    E getResult(int index);
}
