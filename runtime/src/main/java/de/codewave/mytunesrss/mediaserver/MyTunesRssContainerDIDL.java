package de.codewave.mytunesrss.mediaserver;

import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MyTunesRssContainerDIDL extends MyTunesRssDIDL {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssContainerDIDL.class);

    protected long myTotalMatches;

    @Override
    long getTotalMatches() {
        return myTotalMatches;
    }

    protected <T> void executeAndProcess(DataStoreSession tx, DataStoreQuery<DataStoreQuery.QueryResult<T>> query, final DataStoreQuery.ResultProcessor<T> processor, long first, int count) throws java.sql.SQLException {
        int effectiveCount = count > 0 ? count : Integer.MAX_VALUE;
        query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        DataStoreQuery.QueryResult<T> queryResult = tx.executeQuery(query);
        myTotalMatches = 0;
        if (first > 0) {
            queryResult.processNextResults(new DataStoreQuery.ResultProcessor<T>() {
                @Override
                public void process(T result) {
                    myTotalMatches++;
                }
            }, (int) first, true);
        }
        if (effectiveCount > 0) {
            queryResult.processNextResults(new DataStoreQuery.ResultProcessor<T>() {
                @Override
                public void process(T result) {
                    myTotalMatches++;
                    processor.process(result);
                }
            }, effectiveCount, true);
        }
        queryResult.processRemainingResults(new DataStoreQuery.ResultProcessor<T>() {
            @Override
            public void process(T result) {
                myTotalMatches++;
            }
        });
        LOGGER.debug("Processed {} items from query result.", myTotalMatches);
    }
}
