/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

public enum SmartFieldType {
    // the ordinal order here defines the order on the edit page
    album(), artist(), genre(), tvshow(), title(), file(), comment(), mintime(), maxtime(), mediatype(),
    videotype(), protection(), composer(), datasource(), recentlyUpdated(), recentlyPlayed(), order(), sizeLimit();

    public boolean isLucene() {
        switch (this) {
            case album:
            case artist:
            case genre:
            case tvshow:
            case title:
            case file:
            case comment:
            case composer:
                return true;
            default:
                return false;
        }
    }
}
