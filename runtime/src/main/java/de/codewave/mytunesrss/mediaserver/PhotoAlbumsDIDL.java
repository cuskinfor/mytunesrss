package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindPhotoAlbumIdsQuery;
import de.codewave.mytunesrss.datastore.statement.GetPhotoAlbumsQuery;
import de.codewave.mytunesrss.datastore.statement.PhotoAlbum;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.support.model.SortCriterion;
import org.omg.CORBA._PolicyStub;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoAlbumsDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        if (StringUtils.isBlank(oidParams)) {
            FindPhotoAlbumIdsQuery findPhotoAlbumIdsQuery = new FindPhotoAlbumIdsQuery();
            int photoAlbumCount = tx.executeQuery(findPhotoAlbumIdsQuery).size();
            for (Integer photoSize : getClientProfile().getPhotoSizes()) {
                addContainer(
                        createSimpleContainer(
                                ObjectID.PhotoAlbums.getValue() + ";" + encode(Integer.toString(photoSize)),
                                ObjectID.PhotoAlbums.getValue(),
                                photoSize > 0 ? "max. " + photoSize + " px" : "original size",
                                photoAlbumCount
                        )
                );
            }
            myTotalMatches = getClientProfile().getPhotoSizes().size();
        } else {
            GetPhotoAlbumsQuery getPhotoAlbumsQuery = new GetPhotoAlbumsQuery(user);
            final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
            executeAndProcess(
                    tx,
                    getPhotoAlbumsQuery,
                    new DataStoreQuery.ResultProcessor<PhotoAlbum>() {
                        @Override
                        public void process(PhotoAlbum photoAlbum) {
                            org.fourthline.cling.support.model.container.PhotoAlbum albumItem = new org.fourthline.cling.support.model.container.PhotoAlbum();
                            albumItem.setId(ObjectID.PhotoAlbum.getValue() + ";" + encode(decode(oidParams).get(0), photoAlbum.getId()));
                            albumItem.setParentID(ObjectID.PhotoAlbums.getValue() + ";" + oidParams);
                            albumItem.setTitle(photoAlbum.getName());
                            albumItem.setDate(dateFormat.format(new Date(photoAlbum.getFirstDate())));
                            albumItem.setChildCount(photoAlbum.getPhotoCount());
                            addContainer(albumItem);
                        }
                    },
                    firstResult,
                    (int) maxResults
            );
        }
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        if (StringUtils.isBlank(oidParams)) {
            addContainer(createSimpleContainer(ObjectID.PhotoAlbums.getValue(), ObjectID.Root.getValue(), getClientProfile().getPhotoSizes().size()));
        } else {
            FindPhotoAlbumIdsQuery findPhotoAlbumIdsQuery = new FindPhotoAlbumIdsQuery();
            int photoAlbumCount = tx.executeQuery(findPhotoAlbumIdsQuery).size();
            addContainer(createSimpleContainer(ObjectID.PhotoAlbums.getValue() + ";" + oidParams, ObjectID.PhotoAlbums.getValue(), photoAlbumCount));
        }
        myTotalMatches = 1;
    }

}
