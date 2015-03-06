/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowsePhotoCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowsePhotoCommandHandler.class);

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
            QueryResult<Photo> photoResult = getTransaction().executeQuery(FindPhotoQuery.getForAlbum(getAuthUser(), photoAlbumId));
            int current = getSafeIntegerRequestParameter("index", 0);
            int pageSize = getWebConfig().getEffectivePhotoPageSize();
            if (StringUtils.isBlank(getRequestParameter("photoIndex", null)) && pageSize > 0 && photoResult.getResultSize() > pageSize) {
                Pager pager = createPager(photoResult.getResultSize(), pageSize, current);
                getRequest().setAttribute("pager", pager);
                getRequest().setAttribute("photos", photoResult.getResults(current * pageSize, pageSize));
                getRequest().setAttribute("firstPhotoIndex", current * pageSize);
            } else {
                getRequest().setAttribute("photos", photoResult.getResults());
                getRequest().setAttribute("firstPhotoIndex", 0);
            }
            getRequest().setAttribute("sessionAuthorized", isSessionAuthorized());
            forward(MyTunesRssResource.BrowsePhoto);
        }
    }
}
