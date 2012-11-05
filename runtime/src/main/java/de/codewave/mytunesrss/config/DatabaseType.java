/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.MyTunesRssUtils;

import java.util.Locale;

public enum DatabaseType {
    h2(), mysqlinternal(), h2custom(), postgres(), mysql();

    @Override
    public String toString() {
        return MyTunesRssUtils.getBundleString(Locale.getDefault(), "settings.database.type." + name());
    }

    public String getDialect() {
        switch (this) {
            case mysql:
            case mysqlinternal:
                return "mysql";
            case postgres:
                return "postgres";
            default:
                return "h2";
        }
    }
}
