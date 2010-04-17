/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public enum DatabaseType {
    h2(), h2custom(), postgres(), mysql();


    @Override
    public String toString() {
        return MyTunesRssUtils.getBundleString("settings.database.type." + name());
    }
}
