/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import org.apache.commons.lang.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseTrackCommandHandler extends MyTunesRssCommandHandler {
    public static enum SortOrder {
        Album(),Artist();
    }

    @Override
    public void executeAuthorized() throws Exception {
        String searchTerm = getRequestParameter("searchTerm", null);
        String album = getRequestParameter("album", null);
        String artist = getRequestParameter("artist", null);
        String playlistId = getRequestParameter("playlist", null);
        String sortOrderName = getRequestParameter("sortOrder", SortOrder.Album.name());
        SortOrder sortOrderValue = SortOrder.valueOf(sortOrderName);

        DataStoreQuery<Track> query = null;
        if (StringUtils.isNotEmpty(searchTerm)) {
            query = FindTrackQuery.getForSearchTerm(searchTerm, sortOrderValue == SortOrder.Artist);
        } else if (StringUtils.isNotEmpty(album)) {
            query = FindTrackQuery.getForAlbum(new String[] {album}, sortOrderValue == SortOrder.Artist);
        } else if (StringUtils.isNotEmpty(artist)) {
            if (Boolean.valueOf(getRequestParameter("fullAlbums", "false"))) {
                Collection<Album> albumsWithArtist = getDataStore().executeQuery(new FindAlbumQuery(artist));
                List<String> albumNames = new ArrayList<String>();
                for (Album albumWithArtist : albumsWithArtist) {
                    albumNames.add(albumWithArtist.getName());
                }
                query = FindTrackQuery.getForAlbum(albumNames.toArray(new String[albumsWithArtist.size()]), sortOrderValue == SortOrder.Artist);
            } else {
                query = FindTrackQuery.getForArtist(new String[] {artist}, sortOrderValue == SortOrder.Artist);
            }
        } else if (StringUtils.isNotEmpty(playlistId)) {
            query = new FindPlaylistTracksQuery(playlistId);
        }
        getRequest().setAttribute("sortOrder", sortOrderName);
        if (query != null) {
            List<Track> simpleTracks = (List<Track>)getDataStore().executeQuery(query);
            int pageSize = getWebConfig().getPageSize();
            if (pageSize > 0 && simpleTracks.size() > pageSize) {
                int current = Integer.parseInt(getRequestParameter("index", "0"));
                Pager pager = createPager(simpleTracks.size(), current);
                getRequest().setAttribute("pager", pager);
                simpleTracks = simpleTracks.subList(current * pageSize, Math.min((current * pageSize) + pageSize, simpleTracks.size()));
            }
            EnhancedTracks enhancedTracks = getTracks(simpleTracks, sortOrderValue);
            getRequest().setAttribute("sortOrderLink", Boolean.valueOf(!enhancedTracks.isSimpleResult()));
            List<EnhancedTrack> tracks = (List<EnhancedTrack>)enhancedTracks.getTracks();
            if (pageSize > 0 && tracks.size() > pageSize) {
                tracks.get(0).setContinuation(!tracks.get(0).isNewSection());
                tracks.get(0).setNewSection(true);
            }
            getRequest().setAttribute("tracks", tracks);
        }
        forward(MyTunesRssResource.BrowseTrack);
    }

    private EnhancedTracks getTracks(Collection<Track> tracks, SortOrder sortOrder) {
        EnhancedTracks enhancedTracks = new EnhancedTracks();
        enhancedTracks.setTracks(new ArrayList<EnhancedTrack>(tracks.size()));
        String lastAlbum = getClass().getName();
        String lastArtist = getClass().getName();
        List<EnhancedTrack> sectionTracks = new ArrayList<EnhancedTrack>();
        boolean variousPerSection = false;
        int sectionCount = 0;
        for (Track track : tracks) {
            EnhancedTrack enhancedTrack = new EnhancedTrack(track);
            boolean newAlbum = !lastAlbum.equals(track.getAlbum());
            boolean newArtist = !lastArtist.equals(track.getArtist());
            if ((sortOrder == SortOrder.Album && newAlbum) || (sortOrder == SortOrder.Artist && newArtist)) {// new section begins
                sectionCount++;
                enhancedTrack.setNewSection(true);
                finishSection(sectionTracks, variousPerSection);
                sectionTracks.clear();
                variousPerSection = false;
            } else {
                if ((sortOrder == SortOrder.Album && newArtist) || (sortOrder == SortOrder.Artist && newAlbum)) {
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