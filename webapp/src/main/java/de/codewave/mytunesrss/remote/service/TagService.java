package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.TrackUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;

import java.sql.SQLException;
import java.util.List;

/**
 * de.codewave.mytunesrss.remote.service.TagService
 */
public class TagService {
    /**
     * Get all tags available in the database.
     *
     * @return List of all tags.
     * @throws IllegalAccessException
     * @throws SQLException
     */
    public Object getAllTags() throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(new FindAllTagsQuery()), 0, 0));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object getTagsForTrack(String trackId) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(new FindAllTagsForTrackQuery(trackId)), 0, 0));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object getTagsForPlaylist(String playlistId) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(new FindAllTagsForPlaylistQuery(playlistId)), 0, 0));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object getTagsForAlbum(String album) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(new FindAllTagsForAlbumQuery(album)), 0, 0));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object getTagsForArtist(String artist) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(new FindAllTagsForArtistQuery(artist)), 0, 0));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public void setTagsToTracks(String[] trackIds, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            for (String tag : tags) {
                TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(trackIds, tag));
            }
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public void removeTagsFromTracks(String[] trackIds, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            for (String tag : tags) {
                TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(trackIds, tag));
            }
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public void setTagsToTrack(String trackId, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            for (String tag : tags) {
                TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(new String[] {trackId}, tag));
            }
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public void removeTagsFromTrack(String trackId, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            for (String tag : tags) {
                TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(new String[]{trackId}, tag));
            }
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }


    public void setTagsToPlaylist(String playlistId, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            List<Track> tracks = TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(playlistId, SortOrder.KeepOrder)).getResults();
            setTagsToTracks(TrackUtils.getTrackIds(tracks), tags);
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public void removeTagsFromPlaylist(String playlistId, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            List<Track> tracks = TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(playlistId, SortOrder.KeepOrder)).getResults();
            removeTagsFromTracks(TrackUtils.getTrackIds(tracks), tags);
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public void setTagsToAlbum(String album, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            List<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForAlbum(user, new String[]{album}, SortOrder.KeepOrder)).getResults();
            setTagsToTracks(TrackUtils.getTrackIds(tracks), tags);
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public void removeTagsFromAlbum(String album, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            List<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForAlbum(user, new String[]{album}, SortOrder.KeepOrder)).getResults();
            removeTagsFromTracks(TrackUtils.getTrackIds(tracks), tags);
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public void setTagsToArtist(String artist, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            List<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForArtist(user, new String[]{artist}, SortOrder.KeepOrder)).getResults();
            setTagsToTracks(TrackUtils.getTrackIds(tracks), tags);
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public void removeTagsFromArtist(String artist, String[] tags) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            List<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForArtist(user, new String[]{artist}, SortOrder.KeepOrder)).getResults();
            removeTagsFromTracks(TrackUtils.getTrackIds(tracks), tags);
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }
}