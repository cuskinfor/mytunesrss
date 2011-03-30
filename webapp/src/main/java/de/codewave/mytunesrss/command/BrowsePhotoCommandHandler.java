/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowsePhotoCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowsePhotoCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            if (!getAuthUser().isPhotos()) {
                addError(new BundleError("error.illegalAccess"));
                forward(MyTunesRssCommand.ShowPortal);
            } else {
                String photoAlbumId = MyTunesRssBase64Utils.decodeToString(getRequestParameter("photoalbumid", null));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Getting photos for album with ID \"" + photoAlbumId + "\".");
                }
                DataStoreQuery.QueryResult<Photo> photoResult = getTransaction().executeQuery(FindPhotoQuery.getForAlbum(photoAlbumId));
                int pageSize = getWebConfig().getEffectivePhotoPageSize();
                if (pageSize > 0 && photoResult.getResultSize() > pageSize) {
                    int current = getSafeIntegerRequestParameter("index", 0);
                    Pager pager = createPager(photoResult.getResultSize(), pageSize, current);
                    getRequest().setAttribute("pager", pager);
                    getRequest().setAttribute("photos", photoResult.getResults(current * pageSize, pageSize));
                } else {
                    getRequest().setAttribute("photos", photoResult.getResults());
                }
                forward(MyTunesRssResource.BrowsePhoto);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
