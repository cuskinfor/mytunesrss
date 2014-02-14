package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

public class PhotoAlbumsDIDL extends MyTunesRssDIDLContent {
    @Override
    void createDirectChildren(User user, DataStoreSession tx, String objectID, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String objectID) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    long getTotalMatches() {
        return 0;
    }
}
