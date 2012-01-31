/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowseSinglePhotoCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseSinglePhotoCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (!getAuthUser().isPhotos()) {
            addError(new BundleError("error.illegalAccess"));
            forward(MyTunesRssCommand.ShowPortal);
        } else {
            String photoAlbumId = MyTunesRssBase64Utils.decodeToString(getRequestParameter("photoalbumid", null));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Getting photos for album with ID \"" + photoAlbumId + "\".");
            }
            DataStoreQuery.QueryResult<Photo> photoResult = getTransaction().executeQuery(FindPhotoQuery.getForAlbum(getAuthUser(), photoAlbumId));
            getRequest().setAttribute("photos", photoResult.getResults());
            getRequest().setAttribute("photoPage", getSafeIntegerRequestParameter("photoIndex", 0) / getWebConfig().getEffectivePhotoPageSize());
            forward(MyTunesRssResource.BrowseSinglePhoto);
        }
    }
}
