package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.VideoType;
import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.datastore.statement.SmartInfo
 */
public class SmartInfo {
    private String myAlbumPattern;
    private String myArtistPattern;
    private String mySeriesPattern;
    private String myGenrePattern;
    private String myTitlePattern;
    private String myFilePattern;
    private String myTagPattern;
    private String myCommentPattern;
    private Integer myTimeMin;
    private Integer myTimeMax;
    private Boolean myProtected;
    private MediaType myMediaType;
    private VideoType myVideoType;

    public String getAlbumPattern() {
        return myAlbumPattern;
    }

    public void setAlbumPattern(String albumPattern) {
        myAlbumPattern = albumPattern;
    }

    public String getArtistPattern() {
        return myArtistPattern;
    }

    public void setArtistPattern(String artistPattern) {
        myArtistPattern = artistPattern;
    }

    public String getSeriesPattern() {
        return mySeriesPattern;
    }

    public void setSeriesPattern(String seriesPattern) {
        mySeriesPattern = seriesPattern;
    }

    public String getGenrePattern() {
        return myGenrePattern;
    }

    public void setGenrePattern(String genrePattern) {
        myGenrePattern = genrePattern;
    }

    public String getTitlePattern() {
        return myTitlePattern;
    }

    public void setTitlePattern(String titlePattern) {
        myTitlePattern = titlePattern;
    }

    public String getFilePattern() {
        return myFilePattern;
    }

    public void setFilePattern(String filePattern) {
        myFilePattern = filePattern;
    }

    public String getTagPattern() {
        return myTagPattern;
    }

    public void setTagPattern(String tagPattern) {
        myTagPattern = tagPattern;
    }

    public String getCommentPattern() {
        return myCommentPattern;
    }

    public void setCommentPattern(String commentPattern) {
        myCommentPattern = commentPattern;
    }

    public Integer getTimeMin() {
        return myTimeMin;
    }

    public void setTimeMin(Integer timeMin) {
        myTimeMin = timeMin;
    }

    public Integer getTimeMax() {
        return myTimeMax;
    }

    public void setTimeMax(Integer timeMax) {
        myTimeMax = timeMax;
    }

    public Boolean getProtected() {
        return myProtected;
    }

    public void setProtected(Boolean aProtected) {
        myProtected = aProtected;
    }

    public MediaType getMediaType() {
        return myMediaType;
    }

    public void setMediaType(MediaType mediaType) {
        myMediaType = mediaType;
    }

    public VideoType getVideoType() {
        return myVideoType;
    }

    public void setVideoType(VideoType videoType) {
        myVideoType = videoType;
    }

    public boolean isLuceneCriteria() {
        return StringUtils.isNotEmpty(myAlbumPattern) || StringUtils.isNotEmpty(myArtistPattern) || StringUtils.isNotEmpty(myCommentPattern) || StringUtils.isNotEmpty(myFilePattern) || StringUtils.isNotEmpty(myGenrePattern) || StringUtils.isNotEmpty(myTagPattern) || StringUtils.isNotEmpty(myTitlePattern) || StringUtils.isNotEmpty(mySeriesPattern);
    }
}