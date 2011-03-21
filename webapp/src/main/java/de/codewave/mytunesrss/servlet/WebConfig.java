/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.servlet.WebConfig
 */
public class WebConfig {
    private static final Logger LOG = LoggerFactory.getLogger(WebConfig.class);

    private static final String CONFIG_COOKIE_NAME = MyTunesRss.APPLICATION_IDENTIFIER + "Cookie";
    private static final String CFG_FEED_TYPE_RSS = "feedTypeRss";
    private static final String CFG_FEED_TYPE_PLAYLIST = "feedTypePlaylist";
    private static final String CFG_RSS_LIMIT = "rssLimit";
    private static final String CFG_PAGE_SIZE = "pageSize";
    private static final String CFG_SHOW_DOWNLOAD = "showDownload";
    private static final String CFG_SHOW_PLAYER = "showPlayer";
    private static final String CFG_RANDOM_PLAYLIST_SIZE = "randomPlaylistSize";
    private static final String CFG_LAST_UPDATED_PLAYLIST_SIZE = "lastUpdatedPlaylistSize";
    private static final String CFG_MOST_PLAYED_PLAYLIST_SIZE = "mostPlayedPlaylistSize";
    private static final String CFG_RECENTLY_PLAYED_PLAYLIST_SIZE = "recentlyPlayedPlaylistSize";
    private static final String CFG_PLAYLIST_TYPE = "playlistType";
    private static final String CFG_THEME = "theme";
    private static final String CFG_RANDOM_SOURCE = "rndSrc";
    private static final String CFG_FLASH_PLAYER = "flashplayer";
    private static final String CFG_YAHOO_MEDIAPLAYER = "yahooMediaPlayer";
    private static final String CFG_BROWSER_START_INDEX = "browserStartIndex";
    private static final String CFG_MYTUNESRSSCOM_ADDRESS = "myTunesRssComAddress";
    private static final String CFG_RANDOM_MEDIATYPE = "rndMedia";
    private static final String CFG_RANDOM_PROTECTED = "rndProt";
    private static final String CFG_ALBUM_IMAGE_SIZE = "albImgSize";
    private static final String CFG_SHOW_REMOTE_CONTROL = "rmCtrl";
    private static final String CFG_ACTIVE_TRANSCODERS = "actTra";
    private static final String CFG_KEEP_ALIVE = "keepAlive";
    private static final String CFG_SEARCH_FUZZINESS = "searchFuzziness";
    private static final String CFG_SHOW_THUMBNAILS_FOR_ALBUMS = "showAlbumThumbs";
    private static final String CFG_SHOW_THUMBNAILS_FOR_TRACKS = "showTrackThumbs";
    private static final String CFG_SHOW_EXTERNAL_SITES = "showExtSites";
    private static final String CFG_SHOW_EDIT_TAGS = "showEditTags";
    private static final String CFG_SHOW_ADD_TO_PLAYLIST = "showAddToPlaylist";
    private static Map<String, String> FEED_FILE_SUFFIXES = new HashMap<String, String>();

    private static final String[] VALID_NAMES = {CFG_FEED_TYPE_RSS, CFG_FEED_TYPE_PLAYLIST, CFG_RSS_LIMIT, CFG_PAGE_SIZE,
            CFG_SHOW_DOWNLOAD, CFG_SHOW_PLAYER, CFG_RANDOM_PLAYLIST_SIZE, CFG_LAST_UPDATED_PLAYLIST_SIZE, CFG_MOST_PLAYED_PLAYLIST_SIZE,
            CFG_RECENTLY_PLAYED_PLAYLIST_SIZE, CFG_PLAYLIST_TYPE, CFG_THEME, CFG_RANDOM_SOURCE,
            CFG_FLASH_PLAYER, CFG_YAHOO_MEDIAPLAYER, CFG_BROWSER_START_INDEX, CFG_MYTUNESRSSCOM_ADDRESS, CFG_RANDOM_MEDIATYPE, CFG_RANDOM_PROTECTED,
            CFG_ALBUM_IMAGE_SIZE, CFG_SHOW_REMOTE_CONTROL, CFG_ACTIVE_TRANSCODERS, CFG_SEARCH_FUZZINESS, CFG_SHOW_THUMBNAILS_FOR_ALBUMS, CFG_SHOW_THUMBNAILS_FOR_TRACKS, CFG_SHOW_EXTERNAL_SITES, CFG_KEEP_ALIVE, CFG_SHOW_EDIT_TAGS, CFG_SHOW_ADD_TO_PLAYLIST};

    public static final String MYTUNESRSS_COM_USER = "mytunesrss_com_user";
    public static final String MYTUNESRSS_COM_COOKIE = "mytunesrss_com_cookie";

    public static enum PlaylistType {
        M3u(), Xspf(), Json();

        public String getFileSuffix() {
            switch (this) {
                case M3u:
                    return "m3u";
                case Xspf:
                    return "xspf";
                case Json:
                    return "json";
                default:
                    throw new IllegalArgumentException("illegal playlist type: " + this.name());
            }
        }

        public MyTunesRssResource getTemplateResource() {
            switch (this) {
                case M3u:
                    return MyTunesRssResource.TemplateM3u;
                case Xspf:
                    return MyTunesRssResource.TemplateXspf;
                case Json:
                    return MyTunesRssResource.TemplateJson;
                default:
                    throw new IllegalArgumentException("illegal playlist type: " + this.name());
            }
        }
    }

    private Map<String, String> myConfigValues = new HashMap<String, String>();

    public void clear() {
        myConfigValues.clear();
    }

    public void initWithDefaults(HttpServletRequest request) {
        initWithDefaults();
        if (MyTunesRssWebUtils.getUserAgent(request) == UserAgent.Psp) {
            initWithPspDefaults();
        } else if (MyTunesRssWebUtils.getUserAgent(request) == UserAgent.Iphone) {
            initWithIphoneDefaults();
        } else if (MyTunesRssWebUtils.getUserAgent(request) == UserAgent.NintendoWii) {
            initWithNintendoWiiDefaults();
        }
    }

    private void initWithDefaults() {
        myConfigValues.put(CFG_FEED_TYPE_RSS, "true");
        myConfigValues.put(CFG_FEED_TYPE_PLAYLIST, "true");
        myConfigValues.put(CFG_RSS_LIMIT, "0");
        myConfigValues.put(CFG_PAGE_SIZE, "0");
        myConfigValues.put(CFG_SHOW_DOWNLOAD, "true");
        myConfigValues.put(CFG_SHOW_PLAYER, "true");
        myConfigValues.put(CFG_RANDOM_PLAYLIST_SIZE, "25");
        myConfigValues.put(CFG_LAST_UPDATED_PLAYLIST_SIZE, "25");
        myConfigValues.put(CFG_MOST_PLAYED_PLAYLIST_SIZE, "25");
        myConfigValues.put(CFG_RECENTLY_PLAYED_PLAYLIST_SIZE, "25");
        myConfigValues.put(CFG_PLAYLIST_TYPE, PlaylistType.M3u.name());
        myConfigValues.put(CFG_RANDOM_SOURCE, "");
        myConfigValues.put(CFG_YAHOO_MEDIAPLAYER, "false");
        myConfigValues.put(CFG_BROWSER_START_INDEX, "1");
        myConfigValues.put(CFG_MYTUNESRSSCOM_ADDRESS, "true");
        myConfigValues.put(CFG_RANDOM_MEDIATYPE, "");
        myConfigValues.put(CFG_RANDOM_PROTECTED, "true");
        myConfigValues.put(CFG_ALBUM_IMAGE_SIZE, "128");
        myConfigValues.put(CFG_SHOW_REMOTE_CONTROL, "true");
        myConfigValues.put(CFG_KEEP_ALIVE, "false");
        myConfigValues.put(CFG_SEARCH_FUZZINESS, "50");
        myConfigValues.put(CFG_SHOW_THUMBNAILS_FOR_ALBUMS, "true");
        myConfigValues.put(CFG_SHOW_THUMBNAILS_FOR_TRACKS, "false");
        myConfigValues.put(CFG_SHOW_EXTERNAL_SITES, "false");
        myConfigValues.put(CFG_SHOW_EDIT_TAGS, "false");
        myConfigValues.put(CFG_SHOW_ADD_TO_PLAYLIST, "false");
    }

    private void initWithIphoneDefaults() {
        myConfigValues.put(CFG_FEED_TYPE_RSS, "false");
        myConfigValues.put(CFG_FEED_TYPE_PLAYLIST, "false");
        myConfigValues.put(CFG_PAGE_SIZE, "30");
        myConfigValues.put(CFG_SHOW_DOWNLOAD, "false");
        myConfigValues.put(CFG_FLASH_PLAYER, FlashPlayerConfig.HTML5.getId());
        myConfigValues.put(CFG_ALBUM_IMAGE_SIZE, "256");
        myConfigValues.put(CFG_SHOW_REMOTE_CONTROL, "false");
    }

    private void initWithPspDefaults() {
        myConfigValues.put(CFG_FEED_TYPE_PLAYLIST, "false");
        myConfigValues.put(CFG_RSS_LIMIT, "100");
        myConfigValues.put(CFG_PAGE_SIZE, "30");
        myConfigValues.put(CFG_SHOW_PLAYER, "false");
        myConfigValues.put(CFG_SHOW_REMOTE_CONTROL, "false");
    }

    private void initWithNintendoWiiDefaults() {
        myConfigValues.put(CFG_FEED_TYPE_RSS, "false");
        myConfigValues.put(CFG_FEED_TYPE_PLAYLIST, "false");
        myConfigValues.put(CFG_SHOW_DOWNLOAD, "false");
        myConfigValues.put(CFG_PAGE_SIZE, "30");
        myConfigValues.put(CFG_SHOW_PLAYER, "true");
        myConfigValues.put(CFG_FLASH_PLAYER, FlashPlayerConfig.SIMPLE.getId());
    }

    /**
     * Load web config from server-side user profile.
     *
     * @param request Servlet request.
     * @param user User.
     */
    public void load(HttpServletRequest request, User user) {
        if (user != null && !user.isSharedUser() && StringUtils.isNotEmpty(user.getWebConfig(MyTunesRssWebUtils.getUserAgent(request)))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initializing web configuration from user settings.");
            }
            initFromString(MyTunesRssBase64Utils.decodeToString(user.getWebConfig(MyTunesRssWebUtils.getUserAgent(request))));
        } else {
            if (StringUtils.isNotEmpty(request.getParameter(WebConfig.MYTUNESRSS_COM_COOKIE))) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Initializing web configuration from request parameter.");
                }
                initFromString(MyTunesRssBase64Utils.decodeToString(request.getParameter(WebConfig.MYTUNESRSS_COM_COOKIE)));
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Initializing web configuration from cookie.");
                }
                try {
                    initFromString(MyTunesRssBase64Utils.decodeToString(getCookieValue(request)));
                } catch (Exception e) {
                    // intentionally left blank
                }
            }
            if (StringUtils.isNotEmpty(request.getParameter(WebConfig.MYTUNESRSS_COM_USER))) {
                request.getSession().setAttribute(WebConfig.MYTUNESRSS_COM_USER, request.getParameter(WebConfig.MYTUNESRSS_COM_USER));
            }
        }
    }

    public void clearWithDefaults(HttpServletRequest request) {
        clear();
        initWithDefaults(request);
    }

    private String getCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (CONFIG_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    private void initFromString(String cookieValue) {
        for (String keyValueToken : StringUtils.split(cookieValue, ';')) {
            int k = keyValueToken.indexOf('=');
            if (k > 0) {
                String keyName = keyValueToken.substring(0, k);
                if (ArrayUtils.contains(VALID_NAMES, keyName)) {
                    myConfigValues.put(keyName, k < keyValueToken.length() - 1 ? keyValueToken.substring(k + 1) : "");
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Illegal configuration token found in cookie: \"" + keyValueToken + "\".");
                }
            }
        }
    }

    public void save(HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty((String) request.getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER))) {
            Cookie cookie = new Cookie(CONFIG_COOKIE_NAME, createCookieValue());
            cookie.setVersion(1);
            cookie.setComment("MyTunesRSS settings cookie");
            cookie.setMaxAge(3600 * 24 * 365);// one year
            String servletUrl = MyTunesRssWebUtils.getServletUrl(request);
            cookie.setPath(servletUrl.substring(servletUrl.lastIndexOf("/")));
            response.addCookie(cookie);
        }
    }

    public void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty((String) request.getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER))) {
            Cookie cookie = new Cookie(CONFIG_COOKIE_NAME, createCookieValue());
            cookie.setVersion(1);
            cookie.setComment("MyTunesRSS settings cookie");
            cookie.setMaxAge(0); // delete cookie
            String servletUrl = MyTunesRssWebUtils.getServletUrl(request);
            cookie.setPath(servletUrl.substring(servletUrl.lastIndexOf("/")));
            response.addCookie(cookie);
        }
    }

    public String createCookieValue() {
        StringBuffer value = new StringBuffer();
        for (Map.Entry<String, String> entry : myConfigValues.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                value.append(";").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return MyTunesRssBase64Utils.encode(value.substring(1));
    }

    public Map<String, String> getMap() {
        return Collections.unmodifiableMap(myConfigValues);
    }

    public void setShowDownload(boolean showDownload) {
        myConfigValues.put(CFG_SHOW_DOWNLOAD, Boolean.toString(showDownload));
    }

    public boolean isShowDownload() {
        return Boolean.valueOf(myConfigValues.get(CFG_SHOW_DOWNLOAD));
    }

    public void setShowPlayer(boolean showPlayer) {
        myConfigValues.put(CFG_SHOW_PLAYER, Boolean.toString(showPlayer));
    }

    public boolean isShowPlayer() {
        return Boolean.valueOf(myConfigValues.get(CFG_SHOW_PLAYER));
    }

    public String getTheme() {
        return myConfigValues.get(CFG_THEME);
    }

    public void setTheme(String theme) {
        if (StringUtils.isNotEmpty(theme)) {
            myConfigValues.put(CFG_THEME, theme);
        } else {
            myConfigValues.remove(CFG_THEME);
        }
    }

    public boolean isShowRss() {
        return Boolean.valueOf(myConfigValues.get(CFG_FEED_TYPE_RSS));
    }

    public void setShowRss(boolean showRss) {
        myConfigValues.put(CFG_FEED_TYPE_RSS, Boolean.toString(showRss));
    }

    public boolean isShowPlaylist() {
        return Boolean.valueOf(myConfigValues.get(CFG_FEED_TYPE_PLAYLIST));
    }

    public void setShowPlaylist(boolean showPlaylist) {
        myConfigValues.put(CFG_FEED_TYPE_PLAYLIST, Boolean.toString(showPlaylist));
    }

    public String getPlaylistType() {
        String type = myConfigValues.get(CFG_PLAYLIST_TYPE);
        if (StringUtils.isNotEmpty(type)) {
            try {
                PlaylistType.valueOf(type);
                return type;
            } catch (IllegalArgumentException e) {
                // set default value and return it
            }
            setPlaylistType(PlaylistType.M3u.name());
        }
        return PlaylistType.M3u.name();
    }

    public void setPlaylistType(String playlistType) {
        myConfigValues.put(CFG_PLAYLIST_TYPE, playlistType);
    }

    public int getFeedTypeCount() {
        int count = isShowRss() ? 1 : 0;
        count += (isShowPlaylist() ? 1 : 0);
        return count;
    }

    public int getRssFeedLimit() {
        String rssLimit = myConfigValues.get(CFG_RSS_LIMIT);
        if (StringUtils.isNotEmpty(rssLimit)) {
            return Integer.parseInt(rssLimit);
        }
        return 0;
    }

    public void setRssFeedLimit(int rssFeedLimit) {
        myConfigValues.put(CFG_RSS_LIMIT, Integer.toString(rssFeedLimit));
    }

    public Map<String, String> getFeedFileSuffix() {
        return FEED_FILE_SUFFIXES;
    }

    public int getPageSize() {
        return Integer.parseInt(myConfigValues.get(CFG_PAGE_SIZE));
    }

    public int getEffectivePageSize() {
        int pageSize = getPageSize();
        return pageSize > 0 ? pageSize : 1000;
    }

    public void setPageSize(int pageSize) {
        myConfigValues.put(CFG_PAGE_SIZE, Integer.toString(pageSize));
    }

    public int getRandomPlaylistSize() {
        return Integer.parseInt(myConfigValues.get(CFG_RANDOM_PLAYLIST_SIZE));
    }

    public void setRandomPlaylistSize(int count) {
        myConfigValues.put(CFG_RANDOM_PLAYLIST_SIZE, Integer.toString(count));
    }

    public int getLastUpdatedPlaylistSize() {
        return Integer.parseInt(myConfigValues.get(CFG_LAST_UPDATED_PLAYLIST_SIZE));
    }

    public void setLastUpdatedPlaylistSize(int count) {
        myConfigValues.put(CFG_LAST_UPDATED_PLAYLIST_SIZE, Integer.toString(count));
    }

    public int getMostPlayedPlaylistSize() {
        return Integer.parseInt(myConfigValues.get(CFG_MOST_PLAYED_PLAYLIST_SIZE));
    }

    public void setMostPlayedPlaylistSize(int count) {
        myConfigValues.put(CFG_MOST_PLAYED_PLAYLIST_SIZE, Integer.toString(count));
    }

    public int getRecentlyPlayedPlaylistSize() {
        return Integer.parseInt(myConfigValues.get(CFG_RECENTLY_PLAYED_PLAYLIST_SIZE));
    }

    public void setRecentlyPlayedPlaylistSize(int count) {
        myConfigValues.put(CFG_RECENTLY_PLAYED_PLAYLIST_SIZE, Integer.toString(count));
    }

    public String getPlaylistFileSuffix() {
        return PlaylistType.valueOf(getPlaylistType()).getFileSuffix();
    }

    public MyTunesRssResource getPlaylistTemplateResource() {
        return PlaylistType.valueOf(getPlaylistType()).getTemplateResource();
    }

    public String getRandomSource() {
        return myConfigValues.get(CFG_RANDOM_SOURCE);
    }

    public void setRandomSource(String source) {
        myConfigValues.put(CFG_RANDOM_SOURCE, source);
    }

    public String getFlashplayer() {
        return myConfigValues.get(CFG_FLASH_PLAYER);
    }

    public void setFlashplayer(String type) {
        myConfigValues.put(CFG_FLASH_PLAYER, type);
    }

    public boolean isYahooMediaPlayer() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_YAHOO_MEDIAPLAYER));
    }

    public void setYahooMediaPlayer(boolean yahooMediaPlayer) {
        myConfigValues.put(CFG_YAHOO_MEDIAPLAYER, Boolean.toString(yahooMediaPlayer));
    }

    public String getBrowserStartIndex() {
        return myConfigValues.get(CFG_BROWSER_START_INDEX);
    }

    public void setBrowserStartIndex(String browserStartIndex) {
        myConfigValues.put(CFG_BROWSER_START_INDEX, browserStartIndex);
    }

    public boolean isMyTunesRssComAddress() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_MYTUNESRSSCOM_ADDRESS));
    }

    public void setMyTunesRssComAddress(boolean myTunesRssComAddress) {
        myConfigValues.put(CFG_MYTUNESRSSCOM_ADDRESS, Boolean.toString(myTunesRssComAddress));
    }

    public String getRandomMediaType() {
        return myConfigValues.get(CFG_RANDOM_MEDIATYPE);
    }

    public void setRandomMediaType(String rndMedia) {
        myConfigValues.put(CFG_RANDOM_MEDIATYPE, rndMedia);
    }

    public boolean isRandomProtected() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_RANDOM_PROTECTED));
    }

    public void setRandomProtected(boolean rndProtected) {
        myConfigValues.put(CFG_RANDOM_PROTECTED, Boolean.toString(rndProtected));
    }

    public int getAlbumImageSize() {
        return Integer.parseInt(myConfigValues.get(CFG_ALBUM_IMAGE_SIZE));
    }

    public void setAlbumImageSize(int imageSize) {
        myConfigValues.put(CFG_ALBUM_IMAGE_SIZE, Integer.toString(imageSize));
    }

    public boolean isRemoteControl() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_SHOW_REMOTE_CONTROL));
    }

    public void setRemoteControl(boolean remoteControl) {
        myConfigValues.put(CFG_SHOW_REMOTE_CONTROL, Boolean.toString(remoteControl));
    }

    public String getActiveTranscoders() {
        return myConfigValues.get(CFG_ACTIVE_TRANSCODERS);
    }

    public void setActiveTranscoders(String activeTranscoders) {
        myConfigValues.put(CFG_ACTIVE_TRANSCODERS, activeTranscoders);
    }

    public boolean isKeepAlive() {
        return Boolean.valueOf(myConfigValues.get(CFG_KEEP_ALIVE));
    }

    public void setKeepAlive(boolean keepAlive) {
        myConfigValues.put(CFG_KEEP_ALIVE, Boolean.toString(keepAlive));
    }

    public int getSearchFuzziness() {
        return Integer.parseInt(myConfigValues.get(CFG_SEARCH_FUZZINESS));
    }

    public void setSearchFuzziness(int searchFuzziness) {
        myConfigValues.put(CFG_SEARCH_FUZZINESS, Integer.toString(searchFuzziness));
    }

    public boolean isShowThumbnailsForAlbums() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_SHOW_THUMBNAILS_FOR_ALBUMS));
    }

    public void setShowThumbnailsForAlbums(boolean showThumbnails) {
        myConfigValues.put(CFG_SHOW_THUMBNAILS_FOR_ALBUMS, Boolean.toString(showThumbnails));
    }

    public boolean isShowThumbnailsForTracks() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_SHOW_THUMBNAILS_FOR_TRACKS));
    }

    public void setShowThumbnailsForTracks(boolean showThumbnails) {
        myConfigValues.put(CFG_SHOW_THUMBNAILS_FOR_TRACKS, Boolean.toString(showThumbnails));
    }

    public boolean isShowExternalSites() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_SHOW_EXTERNAL_SITES));
    }

    public void setShowExternalSites(boolean showExternalSites) {
        myConfigValues.put(CFG_SHOW_EXTERNAL_SITES, Boolean.toString(showExternalSites));
    }

    public boolean isShowEditTags() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_SHOW_EDIT_TAGS));
    }

    public void setShowEditTags(boolean showEditTags) {
        myConfigValues.put(CFG_SHOW_EDIT_TAGS, Boolean.toString(showEditTags));
    }

    public boolean isShowAddToPlaylist() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_SHOW_ADD_TO_PLAYLIST));
    }

    public void setShowAddToPlaylist(boolean showAddToPlaylist) {
        myConfigValues.put(CFG_SHOW_ADD_TO_PLAYLIST, Boolean.toString(showAddToPlaylist));
    }
}