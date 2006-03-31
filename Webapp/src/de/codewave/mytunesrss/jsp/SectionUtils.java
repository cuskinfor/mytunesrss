/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.musicfile.*;
import de.codewave.mytunesrss.servlet.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.jsp.SectionUtils
 */
public class SectionUtils {
    public static Collection<Section> buildSections(Collection<MusicFile> musicFiles, SortOrder sortOrder) {
        switch (sortOrder) {
            case Album:
                return buildAlbumSections(musicFiles);

            case Artist:
                return buildArtistSections(musicFiles);
        }
        return null;
    }

    public static Collection<Section> buildAlbumSections(Collection<MusicFile> musicFiles) {
        List<MusicFile> sortedFiles = new ArrayList<MusicFile>(musicFiles);
        Collections.sort(sortedFiles, new AlbumComparator());
        Collection<Section> sections = new ArrayList<Section>();
        Section section = null;
        String album = null;
        for (MusicFile musicFile : sortedFiles) {
            if (!musicFile.getAlbum().equals(album)) {
                section = new Section();
                sections.add(section);
                album = musicFile.getAlbum();
            }
            section.addItem(new SectionItem(musicFile, false));
        }
        return sections;
    }

    public static Collection<Section> buildArtistSections(Collection<MusicFile> musicFiles) {
        List<MusicFile> sortedFiles = new ArrayList<MusicFile>(musicFiles);
        Collections.sort(sortedFiles, new ArtistComparator());
        Collection<Section> sections = new ArrayList<Section>();
        Section section = null;
        String artist = null;
        for (MusicFile musicFile : sortedFiles) {
            if (!musicFile.getArtist().equals(artist)) {
                section = new Section();
                sections.add(section);
                artist = musicFile.getArtist();
            }
            section.addItem(new SectionItem(musicFile, false));
        }
        return sections;
    }

    public static void setSelection(Collection<Section> sections, Collection<String> selectedIds) {
        for (Section section : sections) {
            for (SectionItem item : section.getItems()) {
                if (selectedIds.contains(item.getFile().getId())) {
                    item.setSelected(true);
                }
            }
        }
    }
}