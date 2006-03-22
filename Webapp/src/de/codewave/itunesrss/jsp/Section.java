/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.itunesrss.jsp;

import java.util.*;
import java.io.*;

/**
 * de.codewave.itunesrss.jsp.Section
 */
public class Section implements Serializable {
    private List<SectionItem> myItems = new ArrayList<SectionItem>();

    public List<SectionItem> getItems() {
        return myItems;
    }

    public void addItem(SectionItem item) {
        myItems.add(item);
    }

    public boolean isCommonAlbum() {
        String album = getFirstAlbum();
        for (SectionItem item : myItems) {
            if (!item.getFile().getAlbum().equals(album)) {
                return false;
            }
        }
        return true;
    }

    public String getFirstAlbum() {
        return myItems != null && !myItems.isEmpty() ? myItems.get(0).getFile().getAlbum() : null;
    }

    public boolean isCommonArtist() {
        String artist = getFirstArtist();
        for (SectionItem item : myItems) {
            if (!item.getFile().getArtist().equals(artist)) {
                return false;
            }
        }
        return true;
    }

    public String getFirstArtist() {
        return myItems != null && !myItems.isEmpty() ? myItems.get(0).getFile().getArtist() : null;
    }
}