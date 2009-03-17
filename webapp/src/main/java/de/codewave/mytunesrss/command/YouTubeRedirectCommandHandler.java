package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.external.YouTubeLoader;
import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.command.YouTubeRedirectCommandHandler
 */
public class YouTubeRedirectCommandHandler extends MyTunesRssCommandHandler {
    private static final String YOUTUBE_MYTUNESRSS_FILENAME_PREFIX = "http://youtube.com/get_video?video_id=";

    @Override
    public void executeAuthorized() throws Exception {
        String videoId = StringUtils.substringAfter(getRequestParameter("track", null), "youtube_");
        redirect(YOUTUBE_MYTUNESRSS_FILENAME_PREFIX + videoId + "&t=" + YouTubeLoader.retrieveAdditionalParam(videoId) + "&fmt=18");
    }
}
