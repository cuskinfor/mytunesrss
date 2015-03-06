package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.GetPhotoAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.datastore.statement.PhotoAlbum;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PhotoAlbumDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, String oidParams, long firstResult, long maxResults) throws SQLException {
        final int photoSize = getInt(decode(oidParams).get(0), PhotoDIDL.DEFAULT_PHOTO_SIZE);
        String photoAlbumId = decode(oidParams).get(1);
        FindPhotoQuery findPhotoQuery = FindPhotoQuery.getForAlbum(user, photoAlbumId);
        final PhotoAlbum photoAlbum = tx.executeQuery(new GetPhotoAlbumQuery(photoAlbumId)).nextResult();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        executeAndProcess(
                tx,
                findPhotoQuery,
                new DataStoreQuery.ResultProcessor<Photo>() {
                    @Override
                    public void process(Photo photo) {
                        addItem(createPhotoItem(photo, photoAlbum, dateFormat, user, photoSize));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        PhotoAlbum photoAlbum = tx.executeQuery(new GetPhotoAlbumQuery(decode(oidParams).get(1))).nextResult();
        addContainer(
                createSimpleContainer(
                        ObjectID.PhotoAlbum.getValue() + ";" + oidParams,
                        ObjectID.PhotoAlbums.getValue() + ";" + encode(decode(oidParams).get(0)),
                        photoAlbum.getPhotoCount()
                )
        );
        myTotalMatches = 1;
    }

}
