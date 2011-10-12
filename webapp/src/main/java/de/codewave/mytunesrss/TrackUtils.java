package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.SaveTempPlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.SmartStatement;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.TrackUtils
 */
public class TrackUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackUtils.class);

    public static EnhancedTracks getEnhancedTracks(DataStoreSession transaction, Collection<Track> tracks,
            SortOrder sortOrder) {
        EnhancedTracks enhancedTracks = new EnhancedTracks();
        enhancedTracks.setTracks(new ArrayList<EnhancedTrack>(tracks.size()));
        String lastAlbum = TrackUtils.class.getName();// we need some dummy name
        String lastArtist = TrackUtils.class.getName();// we need some dummy name
        List<EnhancedTrack> sectionTracks = new ArrayList<EnhancedTrack>();
        boolean variousPerSection = false;
        int sectionCount = 0;
        for (Track track : tracks) {
            EnhancedTrack enhancedTrack = new EnhancedTrack(track);
            boolean newAlbum = !lastAlbum.equalsIgnoreCase(track.getAlbum());
            boolean newArtist = !lastArtist.equalsIgnoreCase(track.getArtist());
            if ((sortOrder == SortOrder.Album && newAlbum) || (sortOrder == SortOrder.Artist && newArtist)) {// new section begins
                sectionCount++;
                enhancedTrack.setNewSection(true);
                finishSection(transaction, sectionTracks, variousPerSection);
                sectionTracks.clear();
                variousPerSection = sortOrder == SortOrder.Album && !track.getArtist().equals(track.getAlbumArtist());
            } else {
                if ((sortOrder == SortOrder.Album && !track.getArtist().equals(track.getAlbumArtist())) || (sortOrder == SortOrder.Artist && newAlbum)) {
                    variousPerSection = true;
                }
            }
            enhancedTracks.getTracks().add(enhancedTrack);
            sectionTracks.add(enhancedTrack);
            lastAlbum = track.getAlbum();
            lastArtist = track.getArtist();
        }
        finishSection(transaction, sectionTracks, variousPerSection);
        enhancedTracks.setSimpleResult(sectionCount == 1 && !variousPerSection);
        return enhancedTracks;
    }

    public static List<TvShowEpisode> getTvShowEpisodes(DataStoreSession transaction, Collection<Track> tracks) {
        List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>(tracks.size());
        String lastSeries = TrackUtils.class.getName(); // we need some dummy name
        Integer lastSeason = null;
        List<TvShowEpisode> seriesEpisodes = new ArrayList<TvShowEpisode>();
        List<TvShowEpisode> seasonEpisodes = new ArrayList<TvShowEpisode>();
        for (Track track : tracks) {
            TvShowEpisode episode = new TvShowEpisode(track);
            boolean newSeries = !lastSeries.equalsIgnoreCase(track.getSeries());
            boolean newSeason = newSeries || (lastSeason == null || !lastSeason.equals(track.getSeason()));
            if (newSeries) { // new series begins
                episode.setNewSeries(true);
                finishSeries(transaction, seriesEpisodes);
                seriesEpisodes.clear();
            }
            if (newSeason) {
                episode.setNewSeason(true);
                finishSeason(transaction, seasonEpisodes);
                seasonEpisodes.clear();
            }
            episodes.add(episode);
            seriesEpisodes.add(episode);
            seasonEpisodes.add(episode);
            lastSeries = track.getSeries();
            lastSeason = track.getSeason();
        }
        finishSeries(transaction, seriesEpisodes);
        finishSeason(transaction, seasonEpisodes);
        return episodes;
    }

    private static void finishSection(DataStoreSession transaction, List<EnhancedTrack> sectionTracks, boolean variousInSection) {
        String sectionIds = sectionIdsToString(sectionTracks);
        if (!sectionTracks.isEmpty()) {
            String sectionHash = sectionTracks.size() > 1 ? createTemporarySectionPlaylist(transaction, sectionIds) : null;
            for (EnhancedTrack rememberedTrack : sectionTracks) {
                rememberedTrack.setSectionPlaylistId(sectionHash);
                rememberedTrack.setSectionIds(sectionIds);
                if (!variousInSection) {
                    rememberedTrack.setSimple(true);
                }
            }
        }
    }

    private static void finishSeries(DataStoreSession transaction, List<TvShowEpisode> episodes) {
        String sectionIds = sectionIdsToString(episodes);
        if (!episodes.isEmpty()) {
            String sectionHash = episodes.size() > 1 ? createTemporarySectionPlaylist(transaction, sectionIds) : null;
            for (TvShowEpisode episode : episodes) {
                episode.setSeriesSectionPlaylistId(sectionHash);
                episode.setSeriesSectionIds(sectionIds);
            }
        }
    }

    private static void finishSeason(DataStoreSession transaction, List<TvShowEpisode> episodes) {
        String sectionIds = sectionIdsToString(episodes);
        if (!episodes.isEmpty()) {
            String sectionHash = episodes.size() > 1 ? createTemporarySectionPlaylist(transaction, sectionIds) : null;
            for (TvShowEpisode episode : episodes) {
                episode.setSeasonSectionPlaylistId(sectionHash);
                episode.setSeasonSectionIds(sectionIds);
            }
        }
    }

    private static String createTemporarySectionPlaylist(DataStoreSession transaction, String sectionIds) {
        try {
            final String sectionHash = MyTunesRssBase64Utils.encode(MyTunesRss.SHA1_DIGEST.digest(MyTunesRssUtils.getUtf8Bytes(sectionIds)));
            LOGGER.debug("Trying to create temporary playlist with id \"" + sectionHash + "\".");
            transaction.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeTempPlaylistWithId");
                    statement.setString("id", sectionHash);
                    statement.execute();
                }
            });
            SaveTempPlaylistStatement statement = new SaveTempPlaylistStatement();
            statement.setId(sectionHash);
            statement.setName(sectionHash);
            statement.setTrackIds(Arrays.<String>asList(StringUtils.split(sectionIds, ',')));
            transaction.executeStatement(statement);
            return sectionHash;
        } catch (SQLException e) {
            LOGGER.error("Could not check for existing temporary playlist or could not insert missing temporary playlist.", e);
            return null;// do not use calculated section hash in case of an sql exception
        }
    }

    private static String sectionIdsToString(List<? extends Track> sectionTracks) {
        StringBuffer sectionIds = new StringBuffer();
        for (Iterator<? extends Track> iterator = sectionTracks.iterator(); iterator.hasNext();) {
            Track enhancedTrack = iterator.next();
            sectionIds.append(enhancedTrack.getId());
            if (iterator.hasNext()) {
                sectionIds.append(",");
            }
        }
        return sectionIds.toString();
    }

    /**
     * Get a list of track ids from a list of tracks.
     *
     * @param tracks A list of tracks.
     *
     * @return The list of track ids.
     */
    public static String[] getTrackIds(List<Track> tracks) {
        String[] trackIds = new String[tracks.size()];
        int i = 0;
        for (Track track : tracks) {
            trackIds[i++] = track.getId();
        }
        return trackIds;
    }

    public static class EnhancedTracks {
        private Collection<EnhancedTrack> myTracks;
        private boolean mySimpleResult;

        public boolean isSimpleResult() {
            return mySimpleResult;
        }

        public void setSimpleResult(boolean simpleResult) {
            mySimpleResult = simpleResult;
        }

        public Collection<EnhancedTrack> getTracks() {
            return myTracks;
        }

        public void setTracks(Collection<EnhancedTrack> tracks) {
            myTracks = tracks;
        }
    }

    public static class EnhancedTrack extends Track {
        private boolean myNewSection;
        private boolean myContinuation;
        private boolean mySimple;
        private String mySectionIds;
        private String mySectionPlaylistId;

        private EnhancedTrack(Track track) {
            super(track);
        }

        public boolean isNewSection() {
            return myNewSection;
        }

        public void setNewSection(boolean newSection) {
            myNewSection = newSection;
        }

        public boolean isContinuation() {
            return myContinuation;
        }

        public void setContinuation(boolean continuation) {
            myContinuation = continuation;
        }

        public boolean isSimple() {
            return mySimple;
        }

        public void setSimple(boolean simple) {
            mySimple = simple;
        }

        public String getSectionIds() {
            return mySectionIds;
        }

        public void setSectionIds(String sectionIds) {
            mySectionIds = sectionIds;
        }

        public String getSectionPlaylistId() {
            return mySectionPlaylistId;
        }

        public void setSectionPlaylistId(String sectionPlaylistId) {
            mySectionPlaylistId = sectionPlaylistId;
        }
    }

    public static class TvShowEpisode extends Track {
        private boolean myNewSeries;
        private boolean myNewSeason;
        private boolean myContinuation;
        private String mySeriesSectionIds;
        private String mySeriesSectionPlaylistId;
        private String mySeasonSectionIds;
        private String mySeasonSectionPlaylistId;

        private TvShowEpisode(Track track) {
            super(track);
        }

        public boolean isNewSeries() {
            return myNewSeries;
        }

        public void setNewSeries(boolean newSeries) {
            myNewSeries = newSeries;
        }

        public boolean isNewSeason() {
            return myNewSeason;
        }

        public void setNewSeason(boolean newSeason) {
            myNewSeason = newSeason;
        }

        public boolean isContinuation() {
            return myContinuation;
        }

        public void setContinuation(boolean continuation) {
            myContinuation = continuation;
        }

        public String getSeriesSectionIds() {
            return mySeriesSectionIds;
        }

        public void setSeriesSectionIds(String seriesSectionIds) {
            mySeriesSectionIds = seriesSectionIds;
        }

        public String getSeriesSectionPlaylistId() {
            return mySeriesSectionPlaylistId;
        }

        public void setSeriesSectionPlaylistId(String seriesSectionPlaylistId) {
            mySeriesSectionPlaylistId = seriesSectionPlaylistId;
        }

        public String getSeasonSectionIds() {
            return mySeasonSectionIds;
        }

        public void setSeasonSectionIds(String seasonSectionIds) {
            mySeasonSectionIds = seasonSectionIds;
        }

        public String getSeasonSectionPlaylistId() {
            return mySeasonSectionPlaylistId;
        }

        public void setSeasonSectionPlaylistId(String seasonSectionPlaylistId) {
            mySeasonSectionPlaylistId = seasonSectionPlaylistId;
        }
    }
}