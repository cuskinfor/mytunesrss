package de.codewave.mytunesrss.xmlrpc.service;

import de.codewave.utils.sql.*;

/**
 * de.codewave.mytunesrss.xmlrpc.service.QueryResultWrapper
 */
public class QueryResultWrapper {
    private int mySize;
    private int myItemsLeft;
    private DataStoreQuery.QueryResult myQueryResult;

    public QueryResultWrapper(DataStoreQuery.QueryResult queryResult, int startItem, int maxItems) {
        myQueryResult = queryResult;
        if (startItem > 0) {
            myQueryResult.getResult(startItem - 1);
        }
        if (maxItems > 0) {
            myItemsLeft = Math.min(maxItems, myQueryResult.getResultSize() - startItem);
        } else {
            myItemsLeft = myQueryResult.getResultSize() - startItem;
        }
        mySize = myItemsLeft;
    }

    public int getResultSize() {
        return mySize;
    }

    public Object nextResult() {
        if (myItemsLeft > 0) {
            myItemsLeft--;
            return myQueryResult.nextResult();
        }
        return null;
    }
}