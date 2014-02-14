package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

public class TvShowsDIDL extends MyTunesRssDIDLContent {
    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    long getTotalMatches() {
        return 0;
    }
}
