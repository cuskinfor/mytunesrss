/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;

import java.sql.SQLException;

abstract class MyTunesRssDataStoreQuery<T> extends DataStoreQuery<T> {

    private boolean myForceEmptyResult;

    protected void setForceEmptyResult(boolean forceEmptyResult) {
        myForceEmptyResult = forceEmptyResult;
    }

    @Override
    protected <E> QueryResult<E> execute(SmartStatement statement, ResultBuilder<E> builder) throws SQLException {
        if (myForceEmptyResult) {
            return new EmptyQueryResult<>();
        } else {
            return super.execute(statement, builder);
        }
    }
}
