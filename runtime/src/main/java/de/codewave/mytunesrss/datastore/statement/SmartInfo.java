package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SmartInfo
 */
public class SmartInfo {
    private String myAlbumPattern;
    private String myArtistPattern;
    private String myGenrePattern;
    private String myTitlePattern;
    private String myFilePattern;
    private Integer myTimeMin;
    private Integer myTimeMax;
    private Boolean myProtected;
    private Boolean myVideo;

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

    public Boolean getVideo() {
        return myVideo;
    }

    public void setVideo(Boolean video) {
        myVideo = video;
    }
}