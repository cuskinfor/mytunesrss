package de.codewave.mytunesrss.remote.service;

import de.codewave.camel.mp3.Mp3Info;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.TrackUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Service for track retrieval and management.
 */
public class TrackService {
    /**
     * Get an URL for downloading the track with the specified ID.
     *
     * @param trackId ID of the track.
     *
     * @return The URL for playback of the track.
     */
    public String getDownloadUrl(String trackId) {
        return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.DownloadTrack, "track=" + trackId);
    }

    /**
     * Get an URL for playing the track with the specified ID.
     *
     * @param trackId ID of the track.
     *
     * @return The URL for playback of the track.
     */
    public String getPlaybackUrl(String trackId) {
        return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.PlayTrack, "track=" + trackId);
    }

    public Object getTrackInfo(String trackId) throws IllegalAccessException, SQLException, IOException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            Collection<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForId(new String[] {trackId})).getResults();
            if (!tracks.isEmpty()) {
                Track track = tracks.iterator().next();
                Map<String, Object> result = (Map<String, Object>)RenderMachine.getInstance().render(track);
                if (FileSupportUtils.isMp3(track.getFile())) {
                    result.put("mp3info", Boolean.TRUE);
                    Mp3Info info = Mp3Utils.getMp3Info(new FileInputStream(track.getFile()));
                    result.put("avgBitRate", info.getAvgBitrate());
                    result.put("avgSampleRate", info.getAvgSampleRate());
                }
                return result;
            } else {
                throw new IllegalArgumentException("Track with ID \"" + trackId + "\" not found.");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object search(String searchTerm, boolean sortByArtistFirst, int firstItem, int maxItems) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            if (StringUtils.isNotBlank(searchTerm)) {
                int maxTermSize = 0;
                for (String term : searchTerm.split(" ")) {
                    if (term.length() > maxTermSize) {
                        maxTermSize = term.length();
                    }
                }
                if (maxTermSize >= 3) {
                    FindTrackQuery query = FindTrackQuery.getForSearchTerm(user, searchTerm, sortByArtistFirst);
                    DataStoreSession transaction = TransactionFilter.getTransaction();
                    DataStoreQuery.QueryResult<Track> result = transaction.executeQuery(query);
                    List<Track> tracks = maxItems > 0 ? result.getResults(firstItem, maxItems) : result.getResults();
                    return RenderMachine.getInstance().render(TrackUtils.getEnhancedTracks(transaction,
                                                                                           tracks,
                                                                                           sortByArtistFirst ?
                                                                                                   FindPlaylistTracksQuery.SortOrder.Artist :
                                                                                                   FindPlaylistTracksQuery.SortOrder.Album));
                } else {
                    throw new IllegalArgumentException("At least one of the search terms must be longer than 3 characters!");
                }
            } else {
                throw new IllegalArgumentException("Search term must not be NULL ot empty!");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object getTracks(String[] ids) throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter
                    .getTransaction().executeQuery(FindTrackQuery.getForId(ids)), 0, -1));
        }
        throw new IllegalAccessException("Unauthorized");
    }
}