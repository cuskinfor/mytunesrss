package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.mp3.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.servlet.http.*;

/**
 * de.codewave.mytunesrss.command.ShowTrackImageCommandHandler
 */
public class ShowAlbumImageCommandHandler extends ShowImageCommandHandler {
    private static final Log LOG = LogFactory.getLog(ShowAlbumImageCommandHandler.class);

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
                image = getDataStore().executeQuery(new FindAlbumImageQuery(album, size));
            }
        }
        if (image == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("No tracks recognized in request or no images found in recognized tracks, sending default MyTunesRSS image.");
            }
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            sendImage(image);
        }
    }
}