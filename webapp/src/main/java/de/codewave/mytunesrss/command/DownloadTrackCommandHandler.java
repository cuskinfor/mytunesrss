package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.servlet.RedirectSender;
import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.utils.servlet.StreamListener;
import de.codewave.utils.servlet.StreamSender;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.DownloadTrackCommandHandler
 */
public class DownloadTrackCommandHandler extends PlayTrackCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (!getAuthUser().isDownload()) {
            getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            super.executeAuthorized();
        }
    }

    @Override
    protected StreamSender handleInitialRequest(final Track track, File file) throws IOException {
        addResponseHeader(track, file);
        StreamSender streamSender = new RedirectSender(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.DownloadTrack) + "/" + MyTunesRssUtils.encryptPathInfo(getRequest().getPathInfo().replace("/" + MyTunesRssCommand.DownloadTrack.getName(), "initial=false")));
        streamSender.setStreamListener(new StreamListener() {
            @Override
            public void afterSend() {
                StatisticsEventManager.getInstance().fireEvent(new DownloadEvent(getAuthUser().getName(), track.getId(), 0));
            }
        });
        return streamSender;
    }

    private void addResponseHeader(Track track, File file) {
        getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + MyTunesRssUtils.getLegalFileName(FilenameUtils.getBaseName(file.getName()) + "." + MyTunesFunctions.suffix(getWebConfig(), getAuthUser(), track, getBooleanRequestParameter("notranscode", false))) + "\"");
    }

    @Override
    protected StreamSender handleFollowUpRequest(Track track, File file) throws IOException {
        addResponseHeader(track, file);
        return super.handleFollowUpRequest(track, file);
    }

}
