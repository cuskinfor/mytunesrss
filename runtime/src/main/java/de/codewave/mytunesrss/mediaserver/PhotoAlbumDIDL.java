package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.GetPhotoAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.datastore.statement.PhotoAlbum;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PhotoAlbumDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        String photoAlbumId = decode(oidParams).get(0);
        FindPhotoQuery findPhotoQuery = FindPhotoQuery.getForAlbum(user, photoAlbumId);
        final PhotoAlbum photoAlbum = tx.executeQuery(new GetPhotoAlbumQuery(photoAlbumId)).nextResult();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        executeAndProcess(
                tx,
                findPhotoQuery,
                new DataStoreQuery.ResultProcessor<Photo>() {
                    @Override
                    public void process(Photo photo) {
                        addItem(createPhotoItem(photo, photoAlbum, dateFormat, user, PrefsPhotoSizeDIDL.selectedPhotoSize.get()));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        PhotoAlbum photoAlbum = tx.executeQuery(new GetPhotoAlbumQuery(decode(oidParams).get(0))).nextResult();
        addContainer(createSimpleContainer(ObjectID.PhotoAlbum.getValue() + ";" + oidParams, ObjectID.PhotoAlbums.getValue(), photoAlbum.getPhotoCount()));
        myTotalMatches = 1;
    }

}
