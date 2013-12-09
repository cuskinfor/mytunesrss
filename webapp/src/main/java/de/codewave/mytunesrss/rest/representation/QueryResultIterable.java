package de.codewave.mytunesrss.rest.representation;

import de.codewave.utils.sql.DataStoreQuery;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class QueryResultIterable<S, T> implements Iterable<T> {

    public static interface ResultTransformer<S, T> {
        T transform(S input);
    } 
    
    private DataStoreQuery.QueryResult<S> myQueryResult;
    private ResultTransformer<S, T> myResultTransformer;
    private AtomicReference<S> myNextResult = new AtomicReference<S>();
    private int myRemainingCount;    

    public QueryResultIterable(DataStoreQuery.QueryResult<S> queryResult, ResultTransformer<S, T> resultTransformer) {
        this(queryResult, resultTransformer, Integer.MAX_VALUE);
    }

    public QueryResultIterable(DataStoreQuery.QueryResult<S> queryResult, ResultTransformer<S, T> resultTransformer, int count) {
        myQueryResult = queryResult;
        myResultTransformer = resultTransformer;
        myRemainingCount = count;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
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

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove elements from a query result.");
            }
        };
    }

}
