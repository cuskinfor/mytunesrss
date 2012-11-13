/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.itunes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MissingItunesFiles {
    public static final int MAX_MISSING_FILE_PATHS = 100;

    private long myCount;
    private Collection<String> myPaths;

    public MissingItunesFiles() {
        myPaths = new HashSet<String>();
    }

    public MissingItunesFiles(long count, Collection<String> paths) {
        myCount = count;
        myPaths = paths;
    }

    public long getCount() {
        return myCount;
    }

    public void setCount(long count) {
        myCount = count;
    }

    public Collection<String> getPaths() {
        return myPaths;
    }

    public void setPaths(Collection<String> paths) {
        myPaths = paths;
    }
}
