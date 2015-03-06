/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.GetPhotoAlbumsQuery;
import de.codewave.mytunesrss.datastore.statement.PhotoAlbum;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.QueryResult;

public class BrowsePhotoAlbumCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            if (!getAuthUser().isPhotos()) {
                if (!getAuthUser().isPhotos()) {
                    addError(new BundleError("error.illegalAccess"));
                    forward(MyTunesRssCommand.ShowPortal);
                }
            } else {
                QueryResult<PhotoAlbum> photoAlbumsResult = getTransaction().executeQuery(new GetPhotoAlbumsQuery(getAuthUser()));
                int pageSize = getWebConfig().getEffectivePageSize();
                if (pageSize > 0 && photoAlbumsResult.getResultSize() > pageSize) {
                    int current = getSafeIntegerRequestParameter("index", 0);
                    Pager pager = createPager(photoAlbumsResult.getResultSize(), current);
                    getRequest().setAttribute("pager", pager);
                    getRequest().setAttribute("photoAlbums", photoAlbumsResult.getResults(current * pageSize, pageSize));
                } else {
                    getRequest().setAttribute("photoAlbums", photoAlbumsResult.getResults());
                }
                forward(MyTunesRssResource.BrowsePhotoAlbum);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
