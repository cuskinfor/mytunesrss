/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;

public class BrowsePhotoCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            String photoAlbum = MyTunesRssBase64Utils.decodeToString(getRequestParameter("photoalbum", null));
            DataStoreQuery.QueryResult<Track> photoResult = getTransaction().executeQuery(FindTrackQuery.getPhotos(getAuthUser(), photoAlbum));
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && photoResult.getResultSize() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(photoResult.getResultSize(), current);
                getRequest().setAttribute("pager", pager);
                getRequest().setAttribute("photos", photoResult.getResults(current * pageSize, pageSize));
            } else {
                getRequest().setAttribute("photos", photoResult.getResults());
            }
            forward(MyTunesRssResource.BrowsePhoto);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
