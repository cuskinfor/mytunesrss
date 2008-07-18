package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindAlbumImageQuery;
import de.codewave.mytunesrss.meta.Image;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.command.ShowTrackImageCommandHandler
 */
public class ShowAlbumImageCommandHandler extends ShowImageCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ShowAlbumImageCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        Image image = null;
        String album = getRequest().getParameter("album");
        int size = getIntegerRequestParameter("size", 32);
        if (!isRequestAuthorized()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Not authorized to request track, sending default MyTunesRSS image.");
            }
        } else {
            if (StringUtils.isNotEmpty(album)) {
                image = getTransaction().executeQuery(new FindAlbumImageQuery(album, size));
            }
        }
        if (image == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("No tracks recognized in request or no images found in recognized tracks, sending default MyTunesRSS image.");
            }
            sendDefaultImage(size);
        } else {
            sendImage(image);
        }
    }
}