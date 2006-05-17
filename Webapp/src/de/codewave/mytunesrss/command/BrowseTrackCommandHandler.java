/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;

import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseTrackCommandHandler extends MyTunesRssCommandHandler {
    public static enum SortOrder {
        Album(), Artist();
    }

    @Override
    public void executeAuthorized() throws Exception {
        String searchTerm = getRequestParameter("searchTerm", null);
        String[] albums = getNonEmptyParameterValues("album");
        String[] artists = getNonEmptyParameterValues("artist");
        String sortOrderName = getRequestParameter("sortOrder", SortOrder.Album.name());
        SortOrder sortOrderValue = SortOrder.valueOf(sortOrderName);

        FindTrackQuery query = null;
        if (StringUtils.isNotEmpty(searchTerm)) {
            query = FindTrackQuery.getForSearchTerm(searchTerm, sortOrderValue == SortOrder.Artist);
        } else if (albums != null && albums.length > 0) {
            query = FindTrackQuery.getForAlbum(albums, sortOrderValue == SortOrder.Artist);
        } else if (artists != null && artists.length > 0) {
            query = FindTrackQuery.getForArtist(artists, sortOrderValue == SortOrder.Artist);
        }
        getRequest().setAttribute("sortOrder", sortOrderName);
        if (query != null) {
            getRequest().setAttribute("tracks", getTracks(getDataStore().executeQuery(query), sortOrderValue));
        }
        forward(MyTunesRssResource.BrowseTrack);
    }

    private Collection<EnhancedTrack> getTracks(Collection<Track> tracks, SortOrder sortOrder) {
        List<EnhancedTrack> enhancedTracks = new ArrayList<EnhancedTrack>(tracks.size());
        String lastAlbum = getClass().getName();
        String lastArtist = getClass().getName();
        List<EnhancedTrack> sectionTracks = new ArrayList<EnhancedTrack>();
        boolean variousPerSection = false;
        for (Track track : tracks) {
            EnhancedTrack enhancedTrack = new EnhancedTrack(track);
            boolean newAlbum = !lastAlbum.equals(track.getAlbum());
            boolean newArtist = !lastArtist.equals(track.getArtist());
            if ((sortOrder == SortOrder.Album && newAlbum) || (sortOrder == SortOrder.Artist && newArtist)) { // new section begins
                enhancedTrack.setNewSection(true);
                if (!sectionTracks.isEmpty() && !variousPerSection) { // previous section was simple
                    for (EnhancedTrack rememberedTrack : sectionTracks) {
                        rememberedTrack.setSimple(true);
                    }
                }
                sectionTracks.clear();
                variousPerSection = false;
            } else {
                if ((sortOrder == SortOrder.Album && newArtist) || (sortOrder == SortOrder.Artist && newAlbum)) {
                    variousPerSection = true;
                }
            }
            enhancedTracks.add(enhancedTrack);
            sectionTracks.add(enhancedTrack);
            lastAlbum = track.getAlbum();
            lastArtist = track.getArtist();
        }
        if (!sectionTracks.isEmpty() && !variousPerSection) { // last section was simple
            for (EnhancedTrack rememberedTrack : sectionTracks) {
                rememberedTrack.setSimple(true);
            }
        }
        return enhancedTracks;
    }

    public static class EnhancedTrack extends Track {
        private boolean myNewSection;
        private boolean mySimple;

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

        public boolean isSimple() {
            return mySimple;
        }

        private void setSimple(boolean simple) {
            mySimple = simple;
        }
    }
}