/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.transport.impl.HttpExchangeUpnpStream;
import org.seamless.util.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class MyTunesRssDIDLContent extends DIDLContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssDIDLContent.class);

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

    protected Res createTrackResource(Track track, User user) {
        StringBuilder builder = new StringBuilder("http://");
        builder.append(HttpExchangeUpnpStream.REQUEST_LOCAL_ADDRESS.get()).append(":").append(MyTunesRss.CONFIG.getPort());
        String context = StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebappContext());
        if (!context.startsWith("/")) {
            builder.append("/");
        }
        builder.append(context);
        if (context.length() > 0 && !context.endsWith("/")) {
            builder.append("/");
        }
        builder.append("mytunesrss/playTrack/").append(MyTunesRssUtils.createAuthToken(user));
        StringBuilder pathInfo = new StringBuilder("track=");
        pathInfo.append(MiscUtils.getUtf8UrlEncoded(track.getId()));
        TranscoderConfig transcoder = null;
        if (track.getMediaType() == MediaType.Audio) {
            transcoder = MyTunesRssUtils.getTranscoder(TranscoderConfig.MEDIA_SERVER_MP3_128.getName(), track);
            if (transcoder != null) {
                pathInfo.append("/tc=").append(transcoder.getName());
            }
        }
        builder.append("/").append(MyTunesRssUtils.encryptPathInfo(pathInfo.toString()));
        Res res = new Res();
        MimeType mimeType = MimeType.valueOf(transcoder != null ? transcoder.getTargetContentType() : track.getContentType());
        res.setProtocolInfo(new ProtocolInfo(mimeType));
        if (transcoder == null) {
            res.setSize(track.getContentLength());
        }
        res.setDuration(toHumanReadableTime(track.getTime()));
        res.setValue(builder.toString());
        return res;
    }

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
        int effectiveCount = count > 0 ? count : Integer.MAX_VALUE;
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
        if (effectiveCount > 0) {
            queryResult.processNextResults(new DataStoreQuery.ResultProcessor<T>() {
                @Override
                public void process(T result) {
                    total.incrementAndGet();
                    processor.process(result);
                }
            }, effectiveCount, true);
        }
        queryResult.processRemainingResults(new DataStoreQuery.ResultProcessor<T>() {
            @Override
            public void process(T result) {
                total.incrementAndGet();
            }
        });
        LOGGER.debug("Processed {} items from query result.", total.get());
        return total.get();
    }

    protected String toHumanReadableTime(int time) {
        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        int hours = time / 3600;
        StringBuilder builder = new StringBuilder();
        builder.append(hours).append(":");
        builder.append(StringUtils.leftPad(Integer.toString(minutes), 2, '0')).append(":");
        builder.append(StringUtils.leftPad(Integer.toString(seconds), 2, '0')).append(".000");
        LOGGER.debug("Human readable of \"" + time + "\" is \"" + builder.toString() + "\".");
        return builder.toString();
    }
}
