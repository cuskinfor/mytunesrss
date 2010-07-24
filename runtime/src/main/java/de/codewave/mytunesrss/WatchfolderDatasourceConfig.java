/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.regex.Pattern;

public class WatchfolderDatasourceConfig extends DatasourceConfig {

    private long myMinFileSize;
    private long myMaxFileSize;
    private Pattern myIncludePattern;
    private Pattern myExcludePattern;
    private String myAlbumFallback = "[dir:0]";
    private String myArtistFallback = "[dir:1]";

    public WatchfolderDatasourceConfig(WatchfolderDatasourceConfig source) {
        super(source);
        myMinFileSize = source.getMinFileSize();
        myMaxFileSize = source.getMaxFileSize();
        myIncludePattern = source.myIncludePattern;
        myExcludePattern = source.myExcludePattern;
        myAlbumFallback = source.getAlbumFallback();
        myArtistFallback = source.getArtistFallback();
    }

    public WatchfolderDatasourceConfig(String definition) {
        super(definition);
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Watchfolder;
    }

    public long getMinFileSize() {
        return myMinFileSize;
    }

    public void setMinFileSize(long minFileSize) {
        myMinFileSize = minFileSize;
    }

    public long getMaxFileSize() {
        return myMaxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        myMaxFileSize = maxFileSize;
    }

    public String getIncludePattern() {
        return myIncludePattern != null ? myIncludePattern.pattern() : null;
    }

    public void setIncludePattern(String includePattern) {
        myIncludePattern = StringUtils.isNotBlank(includePattern) ? Pattern.compile(includePattern, Pattern.CASE_INSENSITIVE) : null;
    }

    public String getExcludePattern() {
        return myExcludePattern != null ? myExcludePattern.pattern() : null;
    }

    public void setExcludePattern(String excludePattern) {
        myExcludePattern = StringUtils.isNotBlank(excludePattern) ? Pattern.compile(excludePattern, Pattern.CASE_INSENSITIVE) : null;
    }

    public String getAlbumFallback() {
        return myAlbumFallback;
    }

    public void setAlbumFallback(String albumFallback) {
        myAlbumFallback = albumFallback;
    }

    public String getArtistFallback() {
        return myArtistFallback;
    }

    public void setArtistFallback(String artistFallback) {
        myArtistFallback = artistFallback;
    }

    public boolean isIncluded(File file) {
        if (file.length() < myMinFileSize) {
            return false;
        }
        if (myMaxFileSize > 0 && file.length() > myMaxFileSize) {
            return false;
        }
        String name = file.getName();
        if (myExcludePattern != null) {
            if (myExcludePattern.matcher(name).matches()) {
                return false;
            }
        }
        return myIncludePattern == null || myIncludePattern.matcher(name).matches();
    }
}
