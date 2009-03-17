package de.codewave.mytunesrss.datastore.external;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.ServiceException;
import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.InsertOrUpdateTrackStatement;
import de.codewave.mytunesrss.datastore.statement.InsertTrackStatement;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import de.codewave.mytunesrss.datastore.statement.UpdateTrackStatement;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * de.codewave.mytunesrss.datastore.url.UrlLoader
 */
public class YouTubeLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeLoader.class);
    private static final String YOUTUBE_VIDEO_PREFIX = "http://www.youtube.com/watch?v=";
    private static final String YOUTUBE_MYTUNESRSS_FILENAME_PREFIX = "http://youtube.com/get_video?video_id=";
    private static final String YOUTUBE_VIDEO_FEED_PREFIX = "http://gdata.youtube.com/feeds/api/videos/";
    private static final String YOUTUBE_API_CLIENT_ID = "ytapi-MichaelDescher-MyTuneRSS-l70f4r3p-0";
    private static final Pattern YOUTUBE_ADDITIONAL_PARAM_PATTERN = Pattern.compile("swfArgs.*\\{.*\"t\".*?\"([^\"]+)\"");

    public static boolean handles(String external) {
        if (StringUtils.startsWithIgnoreCase(external, "http://")) {
            if (StringUtils.contains(StringUtils.substringBetween(external, "http://", "/"), "youtube.com")) {
                return true;
            }
        }
        return false;
    }

    private Collection<String> myTrackIds;
    private Set<String> myExistingIds = new HashSet<String>();
    private DataStoreSession myStoreSession;
    private int myUpdatedCount;
    private YouTubeService myService;
    private HttpClient myHttpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
    private long myLastUpdate;
    private Set<ExternalLoader.Flag> myFlags;

    public YouTubeLoader(DataStoreSession storeSession, long lastUpdate, Set<ExternalLoader.Flag> flags, Collection<String> trackIds) {
        myService = new YouTubeService(YOUTUBE_API_CLIENT_ID);
        myStoreSession = storeSession;
        myTrackIds = trackIds;
        myLastUpdate = lastUpdate;
        myFlags = flags;
    }

    public void process(String url) {
        if (StringUtils.startsWithIgnoreCase(url, YOUTUBE_VIDEO_PREFIX)) {
            try {
                processVideoEntry(myService.getEntry(new URL(YOUTUBE_VIDEO_FEED_PREFIX + url.substring(YOUTUBE_VIDEO_PREFIX.length())), VideoEntry.class));
            } catch (IOException e) {
                LOGGER.debug("Could not process youtube video from url \"" + url + "\".", e);
            } catch (ServiceException e) {
                LOGGER.debug("Could not process youtube video from url \"" + url + "\".", e);
            }
        } else {
            try {
                VideoFeed videoFeed = myService.getFeed(new URL(url), VideoFeed.class);
                String album = videoFeed.getTitle().getPlainText();
                for (VideoEntry videoEntry : videoFeed.getEntries()) {
                    processVideoEntry(videoEntry, album);
                }
            } catch (Exception e) {
                LOGGER.error("Could not process youtube video from url \"" + url + "\".", e);
            }
        }
    }

    private void processVideoEntry(VideoEntry videoEntry) {
        processVideoEntry(videoEntry, null);
    }

    private void processVideoEntry(VideoEntry videoEntry, String album) {
        if (videoEntry.getId().lastIndexOf(":") > -1 && videoEntry.getId().lastIndexOf(":") + 1 < videoEntry.getId().length()) {
            String videoId = videoEntry.getId().substring(videoEntry.getId().lastIndexOf(":") + 1);
            String trackId = "youtube_" + videoId;
            boolean existing = myTrackIds.contains(trackId);
            if (existing) {
                myExistingIds.add(trackId);
            }
            if (!existing || myLastUpdate <= videoEntry.getUpdated().getValue()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Processing youtube video \"" + videoId + "\".");
                }
                InsertOrUpdateTrackStatement statement;
                if (!MyTunesRss.CONFIG.isIgnoreArtwork()) {
                    // TODO
                    // statement = existing ? new UpdateTrackAndImageStatement() : new InsertTrackAndImageStatement(TrackSource.YouTube);
                    statement = existing ? new UpdateTrackStatement(TrackSource.YouTube) : new InsertTrackStatement(TrackSource.YouTube);
                } else {
                    statement = existing ? new UpdateTrackStatement(TrackSource.YouTube) : new InsertTrackStatement(TrackSource.YouTube);
                }
                statement.setId(trackId);
                statement.setSticky(myFlags.contains(ExternalLoader.Flag.Sticky));
                statement.setFileName(YOUTUBE_MYTUNESRSS_FILENAME_PREFIX + videoId + "&t=" + retrieveAdditionalParam(videoId) + "&fmt=18");
                statement.setName(videoEntry.getTitle().getPlainText());
                statement.setArtist(videoEntry.getAuthors().get(0).getName());
                statement.setAlbum(album);
                YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
                if (mediaGroup != null) {
                    statement.setGenre(mediaGroup.getYouTubeCategory() != null ? mediaGroup.getYouTubeCategory().getLabel() : null);
                    statement.setTime(mediaGroup.getDuration() != null ? (int) mediaGroup.getDuration().longValue() : 0);
                }
                statement.setMediaType(MediaType.Video);
                try {
                    myStoreSession.executeStatement(statement);
                    myUpdatedCount++;
                    DatabaseBuilderTask.updateHelpTables(myStoreSession, myUpdatedCount);
                    myExistingIds.add(trackId);
                } catch (SQLException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could not insert youtube video \"" + videoId + "\" into database", e);
                    }
                }
            }
        } else {
            LOGGER.warn("Illegal video id \"" + videoEntry.getId() + "\".");
        }
    }

    private String retrieveAdditionalParam(String videoId) {
        GetMethod method = new GetMethod(YOUTUBE_VIDEO_PREFIX + videoId);
        try {
            if (myHttpClient.executeMethod(method) == 200) {
                Matcher matcher = YOUTUBE_ADDITIONAL_PARAM_PATTERN.matcher(method.getResponseBodyAsString());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not retrieve additional parameter for youtube video using url \"" + (YOUTUBE_VIDEO_PREFIX + videoId) + "\".", e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    public Set<String> getExistingIds() {
        return myExistingIds;
    }
}