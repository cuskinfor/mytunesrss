package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlbumsDIDL extends MyTunesRssDIDLContent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumsDIDL.class);
    
    private long myTotalMatches;
    
    @Override
    void createDirectChildren(User user, DataStoreSession tx, final String objectID, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        LOGGER.debug("Getting total album matches.");
        myTotalMatches = tx.executeQuery(new GetSystemInformationQuery()).getAlbumCount();
        if (maxResults > 0 && firstResult < myTotalMatches) {
            LOGGER.debug("Finding {} albums starting at index {}.", maxResults, firstResult);
            FindAlbumQuery findAlbumQuery = new FindAlbumQuery(user, null, null, false, null, -1, 0, Integer.MAX_VALUE, false, false, FindAlbumQuery.AlbumType.ALL);
            findAlbumQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
            tx.executeQuery(findAlbumQuery).processResults(new DataStoreQuery.ResultProcessor<Album>() {
               public void process(Album album) {
                   LOGGER.debug("Adding album \"{}\" to DIDL.", album.getName());
                   addContainer(new MusicAlbum(RootMenuDIDL.ID_ALBUMS + ";" + encode(album.getName(), album.getArtist()), RootMenuDIDL.ID_ALBUMS, album.getName(), album.getArtist(), album.getTrackCount()));
               } 
            }, (int) firstResult, (int) maxResults);
        }
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String objectID) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    long getTotalMatches() {
        LOGGER.debug("Albums DIDL has " + myTotalMatches + " total matches.");
        return myTotalMatches;
    }

}
