/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.regex.Pattern;

public class WatchfolderDatasourceConfig extends DatasourceConfig {

    public static final String DEFAULT_TITLE_FALLBACK = "[[[file:(.*)\\..*]]]";
    public static final String DEFAULT_ALBUM_FALLBACK = "[[[dir:0]]]";
    public static final String DEFAULT_ARTIST_FALLBACK = "[[[dir:1]]]";
    public static final String DEFAULT_SERIES_FALLBACK = "[[[dir:1]]]";
    public static final String DEFAULT_SEASON_FALLBACK = "[[[dir:0:Season ([0-9]+)]]]";
    public static final String DEFAULT_EPISODE_FALLBACK = "[[[file:[0-9]+[eEx]([0-9]+)]]]";
    public static final String DEFAULT_PHOTO_ALBUM_PATTERN = "[[[dir:1]]] - [[[dir:0]]]";

    private long myMinFileSize;
    private long myMaxFileSize;
    private Pattern myIncludePattern;
    private Pattern myExcludePattern;
    private String myTitleFallback = DEFAULT_TITLE_FALLBACK;
    private String myAlbumFallback = DEFAULT_ALBUM_FALLBACK;
    private String myArtistFallback = DEFAULT_ARTIST_FALLBACK;
    private String mySeriesFallback = DEFAULT_SERIES_FALLBACK;
    private String mySeasonFallback = DEFAULT_SEASON_FALLBACK;
    private String myEpisodeFallback = DEFAULT_EPISODE_FALLBACK;
    private VideoType myVideoType = VideoType.Movie;
    private String myPhotoAlbumPattern = DEFAULT_PHOTO_ALBUM_PATTERN;
    private boolean myIgnoreFileMeta = false;
    private String myArtistDropWords = "";

    public WatchfolderDatasourceConfig(WatchfolderDatasourceConfig source) {
        super(source);
        myMinFileSize = source.getMinFileSize();
        myMaxFileSize = source.getMaxFileSize();
        myIncludePattern = source.myIncludePattern;
        myExcludePattern = source.myExcludePattern;
        myTitleFallback = source.getTitleFallback();
        myAlbumFallback = source.getAlbumFallback();
        myArtistFallback = source.getArtistFallback();
        mySeriesFallback = source.getSeriesFallback();
        mySeasonFallback = source.getSeasonFallback();
        myEpisodeFallback = source.getEpisodeFallback();
        myVideoType = source.getVideoType();
        myPhotoAlbumPattern = source.getPhotoAlbumPattern();
        myIgnoreFileMeta = source.isIgnoreFileMeta();
        myArtistDropWords = source.getArtistDropWords();
    }

    public WatchfolderDatasourceConfig(String id, String definition) {
        super(id, definition);
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

    public String getTitleFallback() {
        return myTitleFallback;
    }

    public void setTitleFallback(String titleFallback) {
        myTitleFallback = titleFallback;
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

    public String getSeriesFallback() {
        return mySeriesFallback;
    }

    public void setSeriesFallback(String seriesFallback) {
        mySeriesFallback = seriesFallback;
    }

    public String getSeasonFallback() {
        return mySeasonFallback;
    }

    public void setSeasonFallback(String seasonFallback) {
        mySeasonFallback = seasonFallback;
    }

    public String getEpisodeFallback() {
        return myEpisodeFallback;
    }

    public void setEpisodeFallback(String episodeFallback) {
        myEpisodeFallback = episodeFallback;
    }

    public VideoType getVideoType() {
        return myVideoType;
    }

    public void setVideoType(VideoType videoType) {
        myVideoType = videoType;
    }

    public String getPhotoAlbumPattern() {
        return myPhotoAlbumPattern;
    }

    public void setPhotoAlbumPattern(String photoAlbumPattern) {
        myPhotoAlbumPattern = photoAlbumPattern;
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

    public boolean isIgnoreFileMeta() {
        return myIgnoreFileMeta;
    }

    public void setIgnoreFileMeta(boolean ignoreFileMeta) {
        myIgnoreFileMeta = ignoreFileMeta;
    }

    public String getArtistDropWords() {
        return myArtistDropWords;
    }

    public void setArtistDropWords(String artistDropWords) {
        myArtistDropWords = artistDropWords;
    }
}
