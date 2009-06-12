/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.datastore.statement.FindTrackImageQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.MyTunesRssMp4Utils;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.command.ShowTrackImageCommandHandler
 */
public class ShowTrackImageCommandHandler extends ShowImageCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ShowTrackImageCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        Image image = null;
        if (!isRequestAuthorized()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Not authorized to request track, sending default MyTunesRSS image.");
            }
        } else {
            String trackId = getRequest().getParameter("track");
            int size = getIntegerRequestParameter("size", 256);
            if (StringUtils.isNotEmpty(trackId)) {
                if (size == 0) {
                    DataStoreQuery.QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[] {trackId}));
                    if (tracks.getResultSize() > 0) {
                        Track track = tracks.nextResult();
                        if (FileSupportUtils.isMp3(track.getFile())) {
                            image = MyTunesRssMp3Utils.getImage(track);
                        } else if (FileSupportUtils.isMp4(track.getFile())) {
                            image = MyTunesRssMp4Utils.getImage(track);
                        }
                    }
                } else {
                    byte[] data = getTransaction().executeQuery(new FindTrackImageQuery(trackId, size));
                    if (data != null && data.length > 0) {
                        image = new Image("image/jpeg", data);
                    }
                }
            }
        }
        if (image == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("No tracks recognized in request or no images found in recognized tracks, sending default MyTunesRSS image.");
            }
            sendDefaultImage(256);
        } else {
            sendImage(image);
        }
    }
}