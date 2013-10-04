/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

public enum SmartFieldType {
    album(), artist(), genre(), tvshow(), title(), file(), tag(), comment(), mintime(), maxtime(), mediatype(), videotype(), protection(), composer(), datasource(), order(), sizeLimit();

    public boolean isLucene() {
        switch (this) {
            case album:
            case artist:
            case genre:
            case tvshow:
            case title:
            case file:
            case tag:
            case comment:
            case composer:
                return true;
            default:
                return false;
        }
    }
}
