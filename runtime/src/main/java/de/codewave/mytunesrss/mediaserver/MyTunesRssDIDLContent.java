/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class MyTunesRssDIDLContent extends DIDLContent {

    final void initDirectChildren(String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            createDirectChildren(MyTunesRss.CONFIG.getUser("cling"), tx, oidParams, filter, firstResult, maxResults, orderby);
        } finally {
            tx.rollback();
        }
    }

    final void initMetaData(String oidParams) throws Exception {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            createMetaData(MyTunesRss.CONFIG.getUser("cling"), tx, oidParams);
        } finally {
            tx.rollback();
        }
    }

    abstract void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception;

    abstract void createMetaData(User user, DataStoreSession tx, String oidParams) throws Exception;

    abstract long getTotalMatches();

    String encode(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : strings) {
            stringBuilder.append(MyTunesRssBase64Utils.encode(s)).append(";");
        }
        return stringBuilder.length() > 0 ? stringBuilder.substring(0, stringBuilder.length() - 1) : stringBuilder.toString();
    }

    List<String> decode(String s) {
        List<String> decoded = new ArrayList<>();
        for (String encoded : StringUtils.split(s, ";")) {
            decoded.add(MyTunesRssBase64Utils.decodeToString(encoded));
        }
        return decoded;
    }

    protected <T> long executeAndProcess(DataStoreSession tx, DataStoreQuery<DataStoreQuery.QueryResult<T>> query, final DataStoreQuery.ResultProcessor<T> processor, long first, int count) throws java.sql.SQLException {
        query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        DataStoreQuery.QueryResult<T> queryResult = tx.executeQuery(query);
        final AtomicLong total = new AtomicLong();
        if (first > 0) {
            queryResult.processNextResults(new DataStoreQuery.ResultProcessor<T>() {
                @Override
                public void process(T result) {
                    total.incrementAndGet();
                }
            }, (int) first, true);
        }
        if (count > 0) {
            queryResult.processNextResults(new DataStoreQuery.ResultProcessor<T>() {
                @Override
                public void process(T result) {
                    total.incrementAndGet();
                    processor.process(result);
                }
            }, count, true);
        }
        queryResult.processRemainingResults(new DataStoreQuery.ResultProcessor<T>() {
            @Override
            public void process(T result) {
                total.incrementAndGet();
            }
        });
        return total.get();
    }

}
