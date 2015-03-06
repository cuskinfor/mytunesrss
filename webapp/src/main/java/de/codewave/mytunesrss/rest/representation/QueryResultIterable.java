package de.codewave.mytunesrss.rest.representation;

import de.codewave.utils.sql.QueryResult;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class QueryResultIterable<S, T> implements Iterable<T> {

    public interface ResultTransformer<S, T> {
        T transform(S input);
    }

    private QueryResult<S> myQueryResult;
    private ResultTransformer<S, T> myResultTransformer;
    private AtomicReference<S> myNextResult = new AtomicReference<>();
    private int myRemainingCount;

    public QueryResultIterable(QueryResult<S> queryResult, ResultTransformer<S, T> resultTransformer) {
        this(queryResult, resultTransformer, Integer.MAX_VALUE);
    }

    public QueryResultIterable(QueryResult<S> queryResult, ResultTransformer<S, T> resultTransformer, int count) {
        myQueryResult = queryResult;
        myResultTransformer = resultTransformer;
        myRemainingCount = count;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public synchronized boolean hasNext() {
                if (myRemainingCount > 0) {
                    if (myNextResult.get() == null) {
                        myNextResult.set(myQueryResult.nextResult());
                    }
                    return myNextResult.get() != null;
                } else {
                    return false;
                }
            }

            @Override
            public synchronized T next() {
                if (myRemainingCount > 0) {
                    if (myNextResult.get() != null) {
                        myRemainingCount--;
                        return myResultTransformer.transform(myNextResult.getAndSet(null));
                    } else {
                        T result = myResultTransformer.transform(myQueryResult.nextResult());
                        if (result == null) {
                            throw new NoSuchElementException("No more results in query result.");
                        }
                        myRemainingCount--;
                        return result;
                    }
                } else {
                    throw new NoSuchElementException("No more results in query result.");
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove elements from a query result.");
            }
        };
    }

}
