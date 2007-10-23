/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseTrackCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            String searchTerm = getRequestParameter("searchTerm", null);
            String sortOrderName = getRequestParameter("sortOrder", TrackRetrieveUtils.SortOrder.Album.name());
            TrackRetrieveUtils.SortOrder sortOrderValue = TrackRetrieveUtils.SortOrder.valueOf(sortOrderName);

            DataStoreQuery<Collection<Track>> query = null;
            if (StringUtils.isNotEmpty(searchTerm)) {
                query = FindTrackQuery.getForSearchTerm(searchTerm, sortOrderValue == TrackRetrieveUtils.SortOrder.Artist);
            } else {
                query = TrackRetrieveUtils.getQuery(getRequest(), false);
            }
            getRequest().setAttribute("sortOrder", sortOrderName);
            List<EnhancedTrack> tracks = null;
            if (query != null) {
                List<Track> simpleTracks = (List<Track>)getDataStore().executeQuery(query);
                EnhancedTracks enhancedTracks = getTracks(simpleTracks, sortOrderValue);
                getRequest().setAttribute("sortOrderLink", Boolean.valueOf(!enhancedTracks.isSimpleResult()));
                tracks = (List<EnhancedTrack>)enhancedTracks.getTracks();
                int pageSize = getWebConfig().getEffectivePageSize();
                if (pageSize > 0 && simpleTracks.size() > pageSize) {
                    int current = getSafeIntegerRequestParameter("index", 0);
                    Pager pager = createPager(simpleTracks.size(), current);
                    getRequest().setAttribute("pager", pager);
                    tracks = tracks.subList(current * pageSize, Math.min((current * pageSize) + pageSize, simpleTracks.size()));
                }
                if (pageSize > 0 && tracks.size() > pageSize) {
                    tracks.get(0).setContinuation(!tracks.get(0).isNewSection());
                    tracks.get(0).setNewSection(true);
                }
                getRequest().setAttribute("tracks", tracks);
            }
            if (tracks == null || tracks.isEmpty()) {
                addError(new BundleError("error.browseTrackNoResult"));
                redirect(MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null)));
            } else {
                forward(MyTunesRssResource.BrowseTrack);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

    private EnhancedTracks getTracks(Collection<Track> tracks, TrackRetrieveUtils.SortOrder sortOrder) {
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
            if ((sortOrder == TrackRetrieveUtils.SortOrder.Album && newAlbum) || (sortOrder == TrackRetrieveUtils.SortOrder.Artist && newArtist)) {// new section begins
                sectionCount++;
                enhancedTrack.setNewSection(true);
                finishSection(sectionTracks, variousPerSection);
                sectionTracks.clear();
                variousPerSection = false;
            } else {
                if ((sortOrder == TrackRetrieveUtils.SortOrder.Album && newArtist) || (sortOrder == TrackRetrieveUtils.SortOrder.Artist && newAlbum)) {
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
            for (EnhancedTrack rememberedTrack : sectionTracks) {
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

        private EnhancedTrack(Track track) {
            setId(track.getId());
            setName(track.getName());
            setAlbum(track.getAlbum());
            setArtist(track.getArtist());
            setTime(track.getTime());
            setTrackNumber(track.getTrackNumber());
            setFile(track.getFile());
            setProtected(track.isProtected());
            setVideo(track.isVideo());
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
    }
}