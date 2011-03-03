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
            if ((sortOrder == SortOrder.Album && newAlbum) ||
                    (sortOrder == SortOrder.Artist && newArtist)) {// new section begins
                sectionCount++;
                enhancedTrack.setNewSection(true);
                finishSection(transaction, sectionTracks, variousPerSection);
                sectionTracks.clear();
                variousPerSection = false;
            } else {
                if ((sortOrder == SortOrder.Album && newArtist) ||
                        (sortOrder == SortOrder.Artist && newAlbum)) {
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

    private static void finishSection(DataStoreSession transaction, List<EnhancedTrack> sectionTracks, boolean variousInSection) {
        StringBuffer sectionIds = new StringBuffer();
        for (Iterator<EnhancedTrack> iterator = sectionTracks.iterator(); iterator.hasNext();) {
            EnhancedTrack enhancedTrack = iterator.next();
            sectionIds.append(enhancedTrack.getId());
            if (iterator.hasNext()) {
                sectionIds.append(",");
            }
        }
        if (!sectionTracks.isEmpty()) {
            String sectionHash = null;
            if (sectionTracks.size() > 1) {// for more than 1 track in a section create a temporary section playlist
                try {
                    sectionHash = MyTunesRssBase64Utils.encode(MyTunesRss.SHA1_DIGEST.digest(MyTunesRssUtils.getUtf8Bytes(sectionIds.toString())));
                    final String finalSectionHash = sectionHash;
                    LOGGER.debug("Trying to create temporary playlist with id \"" + sectionHash + "\".");
                    transaction.executeStatement(new DataStoreStatement() {
                        public void execute(Connection connection) throws SQLException {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeTempPlaylistWithId");
                            statement.setString("id", finalSectionHash);
                            statement.execute();
                        }
                    });
                    SaveTempPlaylistStatement statement = new SaveTempPlaylistStatement();
                    statement.setId(sectionHash);
                    statement.setName(sectionHash);
                    statement.setTrackIds(Arrays.<String>asList(StringUtils.split(sectionIds.toString(), ',')));
                    transaction.executeStatement(statement);
                } catch (SQLException e) {
                    LOGGER.error("Could not check for existing temporary playlist or could not insert missing temporary playlist.", e);
                    sectionHash = null;// do not use calculated section hash in case of an sql exception
                }
            }
            for (EnhancedTrack rememberedTrack : sectionTracks) {
                rememberedTrack.setSectionPlaylistId(sectionHash);
                rememberedTrack.setSectionIds(sectionIds.toString());
                if (!variousInSection) {
                    rememberedTrack.setSimple(true);
                }
            }
        }
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
            setSource(track.getSource());
            setId(track.getId());
            setName(track.getName());
            setAlbum(track.getAlbum());
            setArtist(track.getArtist());
            setOriginalArtist(track.getOriginalArtist());
            setTime(track.getTime());
            setTrackNumber(track.getTrackNumber());
            setFilename(track.getFilename());
            setFile(track.getFile());
            setProtected(track.isProtected());
            setMediaType(track.getMediaType());
            setGenre(track.getGenre());
            setMp4Codec(track.getMp4Codec());
            setTsPlayed(track.getTsPlayed());
            setTsUpdated(track.getTsUpdated());
            setLastImageUpdate(track.getLastImageUpdate());
            setPlayCount(track.getPlayCount());
            setImageHash(track.getImageHash());
            setComment(track.getComment());
            setPosNumber(track.getPosNumber());
            setPosSize(track.getPosSize());
            setYear(track.getYear());
            setVideoType(track.getVideoType());
            setEpisode(track.getEpisode());
            setSeason(track.getSeason());
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
}