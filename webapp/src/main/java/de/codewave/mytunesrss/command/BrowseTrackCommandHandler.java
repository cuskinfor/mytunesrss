/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseTrackCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BrowseTrackCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (getRequest().getPathInfo() != null && getRequest().getPathInfo().toLowerCase().endsWith(".gif")) {
            return;// fix for those stupid yahoo media player img requests
        }
        if (isSessionAuthorized()) {
            String searchTerm = getRequestParameter("searchTerm", null);
            String sortOrderName = getRequestParameter("sortOrder", FindPlaylistTracksQuery.SortOrder.Album.name());
            FindPlaylistTracksQuery.SortOrder sortOrderValue = FindPlaylistTracksQuery.SortOrder.valueOf(sortOrderName);

            DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = null;
            if (StringUtils.isNotEmpty(searchTerm)) {
                int maxTermSize = 0;
                for (String term : searchTerm.split(" ")) {
                    if (term.length() > maxTermSize) {
                        maxTermSize = term.length();
                    }
                }
                if (maxTermSize >= 3) {
                    query = FindTrackQuery.getForSearchTerm(getAuthUser(), searchTerm, sortOrderValue == FindPlaylistTracksQuery.SortOrder.Artist);
                } else {
                    addError(new BundleError("error.searchTermMinSize", 3));
                    forward(MyTunesRssCommand.ShowPortal);
                    return;// early return
                }
            } else {
                query = TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), false);
            }
            getRequest().setAttribute("sortOrder", sortOrderName);
            List<EnhancedTrack> tracks = null;
            if (query != null) {
                DataStoreQuery.QueryResult<Track> result = getTransaction().executeQuery(query);
                int pageSize = getWebConfig().getEffectivePageSize();
                EnhancedTracks enhancedTracks;
                if (pageSize > 0 && result.getResultSize() > pageSize) {
                    int current = getSafeIntegerRequestParameter("index", 0);
                    Pager pager = createPager(result.getResultSize(), current);
                    getRequest().setAttribute("pager", pager);
                    enhancedTracks = getTracks(result.getResults(current * pageSize, pageSize), sortOrderValue);
                } else {
                    enhancedTracks = getTracks(result.getResults(), sortOrderValue);
                }
                getRequest().setAttribute("sortOrderLink", Boolean.valueOf(!enhancedTracks.isSimpleResult()));
                tracks = (List<EnhancedTrack>)enhancedTracks.getTracks();
                if (pageSize > 0 && tracks.size() > pageSize) {
                    tracks.get(0).setContinuation(!tracks.get(0).isNewSection());
                    tracks.get(0).setNewSection(true);
                }
                getRequest().setAttribute("tracks", tracks);
            }
            if (tracks == null || tracks.isEmpty()) {
                addError(new BundleError("error.browseTrackNoResult"));
                if (StringUtils.isNotEmpty(getRequestParameter("backUrl", null))) {
                    redirect(MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null)));
                } else {
                    getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            } else {
                DataStoreQuery.QueryResult<Playlist> playlistsQueryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                                                PlaylistType.MyTunes,
                                                                                                                                null, null,
                                                                                                                                false,
                                                                                                                                true));
                getRequest().setAttribute("editablePlaylists", playlistsQueryResult.getResults());
                forward(MyTunesRssResource.BrowseTrack);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

    private EnhancedTracks getTracks(Collection<Track> tracks, FindPlaylistTracksQuery.SortOrder sortOrder) {
        EnhancedTracks enhancedTracks = new EnhancedTracks();
        enhancedTracks.setTracks(new ArrayList<EnhancedTrack>(tracks.size()));
        String lastAlbum = getClass().getName();
        String lastArtist = getClass().getName();
        List<EnhancedTrack> sectionTracks = new ArrayList<EnhancedTrack>();
        boolean variousPerSection = false;
        int sectionCount = 0;
        for (Track track : tracks) {
            EnhancedTrack enhancedTrack = new EnhancedTrack(track);
            boolean newAlbum = !lastAlbum.equalsIgnoreCase(track.getAlbum());
            boolean newArtist = !lastArtist.equalsIgnoreCase(track.getArtist());
            if ((sortOrder == FindPlaylistTracksQuery.SortOrder.Album && newAlbum) ||
                    (sortOrder == FindPlaylistTracksQuery.SortOrder.Artist && newArtist)) {// new section begins
                sectionCount++;
                enhancedTrack.setNewSection(true);
                finishSection(sectionTracks, variousPerSection);
                sectionTracks.clear();
                variousPerSection = false;
            } else {
                if ((sortOrder == FindPlaylistTracksQuery.SortOrder.Album && newArtist) ||
                        (sortOrder == FindPlaylistTracksQuery.SortOrder.Artist && newAlbum)) {
                    variousPerSection = true;
                }
            }
            enhancedTracks.getTracks().add(enhancedTrack);
            sectionTracks.add(enhancedTrack);
            lastAlbum = track.getAlbum();
            lastArtist = track.getArtist();
        }
        finishSection(sectionTracks, variousPerSection);
        enhancedTracks.setSimpleResult(sectionCount == 1 && !variousPerSection);
        return enhancedTracks;
    }

    private void finishSection(List<EnhancedTrack> sectionTracks, boolean variousInSection) {
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
            if (sectionTracks.size() > 5) { // for more than 5 tracks in a section create or use a temporary section playlist
                try {
                    sectionHash = MyTunesRssBase64Utils.encode(MyTunesRss.SHA1_DIGEST.digest(sectionIds.toString().getBytes("UTF-8")));
                    final String hash = sectionHash;
                    if (!getTransaction().executeQuery(new DataStoreQuery<Boolean>() {
                        public Boolean execute(Connection connection) throws SQLException {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "checkTempPlaylistWithId");
                            statement.setString("id", hash);
                            ResultSet rs = statement.executeQuery();
                            return Boolean.valueOf(rs.next());
                        }
                    })) {
                        SaveTempPlaylistStatement statement = new SaveTempPlaylistStatement();
                        statement.setId(sectionHash);
                        statement.setName(sectionHash);
                        statement.setTrackIds(Arrays.<String>asList(StringUtils.split(sectionIds.toString(), ',')));
                        getTransaction().executeStatement(statement);
                    }
                } catch (SQLException e) {
                    LOG.error("Could not check for existing temporary playlist or could not insert missing temporary playlist.", e);
                    sectionHash = null; // do not use calculated section hash in case of an sql exception
                } catch (UnsupportedEncodingException e) {
                    LOG.error("Could not create section playlist hash.", e);
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
            setId(track.getId());
            setName(track.getName());
            setAlbum(track.getAlbum());
            setArtist(track.getArtist());
            setOriginalArtist(track.getOriginalArtist());
            setTime(track.getTime());
            setTrackNumber(track.getTrackNumber());
            setFile(track.getFile());
            setProtected(track.isProtected());
            setVideo(track.isVideo());
            setGenre(track.getGenre());
            setMp4Codec(track.getMp4Codec());
            setTsPlayed(track.getTsPlayed());
            setTsUpdated(track.getTsUpdated());
            setLastImageUpdate(track.getLastImageUpdate());
            setPlayCount(track.getPlayCount());
            setImageCount(track.getImageCount());
            setComment(track.getComment());
            setPosNumber(track.getPosNumber());
            setPosSize(track.getPosSize());
        }

        public boolean isNewSection() {
            return myNewSection;
        }

        private void setNewSection(boolean newSection) {
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

        private void setSimple(boolean simple) {
            mySimple = simple;
        }

        public String getSectionIds() {
            return mySectionIds;
        }

        private void setSectionIds(String sectionIds) {
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