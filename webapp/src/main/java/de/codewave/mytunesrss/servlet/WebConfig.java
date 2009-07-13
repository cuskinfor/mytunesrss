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
import java.util.Iterator;
import java.util.Map;

/**
 * de.codewave.mytunesrss.servlet.WebConfig
 */
public class WebConfig {
    private static final Logger LOG = LoggerFactory.getLogger(WebConfig.class);

    private static final String CONFIG_COOKIE_NAME = MyTunesRss.APPLICATION_IDENTIFIER + "Cookie";
    private static final String CFG_USER_NAME = "userName";
    private static final String CFG_PASSWORD_HASH = "passwordHash";
    private static final String CFG_LOGIN_STORED = "rememberLogin";
    private static final String CFG_FEED_TYPE_RSS = "feedTypeRss";
    private static final String CFG_FEED_TYPE_PLAYLIST = "feedTypePlaylist";
    private static final String CFG_RSS_LIMIT = "rssLimit";
    private static final String CFG_PAGE_SIZE = "pageSize";
    private static final String CFG_SHOW_DOWNLOAD = "showDownload";
    private static final String CFG_SHOW_PLAYER = "showPlayer";
    private static final String CFG_RANDOM_PLAYLIST_SIZE = "randomPlaylistSize";
    private static final String CFG_LAST_UPDATED_PLAYLIST_SIZE = "lastUpdatedPlaylistSize";
    private static final String CFG_MOST_PLAYED_PLAYLIST_SIZE = "mostPlayedPlaylistSize";
    private static final String CFG_PLAYLIST_TYPE = "playlistType";
    private static final String CFG_LAME_TARGET_BITRATE = "lameBitrate";
    private static final String CFG_LAME_TARGET_SAMPLE_RATE = "lameSampleRate";
    private static final String CFG_THEME = "theme";
    private static final String CFG_TRANSCODE_OTF_IF_POSSIBLE = "transcodeOnTheFlyIfPossible";
    private static final String CFG_RANDOM_SOURCE = "rndSrc";
    private static final String CFG_FLASH_PLAYER_TYPE = "flashplayerType";
    private static final String CFG_YAHOO_MEDIAPLAYER = "yahooMediaPlayer";
    private static final String CFG_BROWSER_START_INDEX = "browserStartIndex";
    private static final String CFG_MYTUNESRSSCOM_ADDRESS = "myTunesRssComAddress";
    private static final String CFG_RANDOM_MEDIATYPE = "rndMedia";
    private static final String CFG_RANDOM_PROTECTED = "rndProt";
    private static final String CFG_ALBUM_IMAGE_SIZE = "albImgSize";
    private static final String CFG_LANGUAGE = "lc";
    private static final String CFG_SHOW_REMOTE_CONTROL = "rmCtrl";
    private static final String CFG_ACTIVE_TRANSCODERS = "actTra";
    private static final String CFG_KEEP_ALIVE = "keepAlive";
    private static Map<String, String> FEED_FILE_SUFFIXES = new HashMap<String, String>();

    private static final String[] VALID_NAMES = {CFG_USER_NAME, CFG_PASSWORD_HASH, CFG_LOGIN_STORED, CFG_FEED_TYPE_RSS, CFG_FEED_TYPE_PLAYLIST, CFG_RSS_LIMIT, CFG_PAGE_SIZE,
            CFG_SHOW_DOWNLOAD, CFG_SHOW_PLAYER, CFG_RANDOM_PLAYLIST_SIZE, CFG_LAST_UPDATED_PLAYLIST_SIZE, CFG_MOST_PLAYED_PLAYLIST_SIZE,
            CFG_PLAYLIST_TYPE, CFG_LAME_TARGET_BITRATE, CFG_LAME_TARGET_SAMPLE_RATE, CFG_THEME, CFG_TRANSCODE_OTF_IF_POSSIBLE, CFG_RANDOM_SOURCE,
            CFG_FLASH_PLAYER_TYPE, CFG_YAHOO_MEDIAPLAYER, CFG_BROWSER_START_INDEX, CFG_MYTUNESRSSCOM_ADDRESS, CFG_RANDOM_MEDIATYPE, CFG_RANDOM_PROTECTED,
            CFG_ALBUM_IMAGE_SIZE, CFG_LANGUAGE, CFG_SHOW_REMOTE_CONTROL, CFG_ACTIVE_TRANSCODERS, CFG_KEEP_ALIVE};

    public static final String MYTUNESRSS_COM_USER = "mytunesrss_com_user";
    public static final String MYTUNESRSS_COM_COOKIE = "mytunesrss_com_cookie";

    public static enum PlaylistType {
        M3u(), Xspf(), QtPlugin(), Qtplugin(), Iphone();

        public String getFileSuffix() {
            switch (this) {
                case M3u:
                    return "m3u";
                case Xspf:
                    return "xspf";
                case QtPlugin:
                case Qtplugin:
                case Iphone:
                    return "html";
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
                case QtPlugin:
                case Qtplugin:
                case Iphone:
                    return MyTunesRssResource.TemplateQtPlugin;
                default:
                    throw new IllegalArgumentException("illegal playlist type: " + this.name());
            }
        }
    }

    private Map<String, String> myConfigValues = new HashMap<String, String>();

    public void clear() {
        myConfigValues.clear();
    }

    public void clearFileSuffixes() {
        for (Iterator<String> iterator = myConfigValues.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next();
            if (key.startsWith("CFG_SUFFIX")) {
                iterator.remove();
            }
        }
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
        myConfigValues.put(CFG_LOGIN_STORED, "false");
        myConfigValues.put(CFG_PASSWORD_HASH, "");
        myConfigValues.put(CFG_PAGE_SIZE, "0");
        myConfigValues.put(CFG_SHOW_DOWNLOAD, "true");
        myConfigValues.put(CFG_SHOW_PLAYER, "true");
        myConfigValues.put(CFG_RANDOM_PLAYLIST_SIZE, "25");
        myConfigValues.put(CFG_LAST_UPDATED_PLAYLIST_SIZE, "25");
        myConfigValues.put(CFG_MOST_PLAYED_PLAYLIST_SIZE, "25");
        myConfigValues.put(CFG_PLAYLIST_TYPE, PlaylistType.M3u.name());
        myConfigValues.put(CFG_LAME_TARGET_BITRATE, "96");
        myConfigValues.put(CFG_LAME_TARGET_SAMPLE_RATE, "22050");
        myConfigValues.put(CFG_TRANSCODE_OTF_IF_POSSIBLE, "false");
        myConfigValues.put(CFG_RANDOM_SOURCE, "");
        myConfigValues.put(CFG_FLASH_PLAYER_TYPE, "jw");
        myConfigValues.put(CFG_YAHOO_MEDIAPLAYER, "false");
        myConfigValues.put(CFG_BROWSER_START_INDEX, "1");
        myConfigValues.put(CFG_MYTUNESRSSCOM_ADDRESS, "true");
        myConfigValues.put(CFG_RANDOM_MEDIATYPE, "");
        myConfigValues.put(CFG_RANDOM_PROTECTED, "true");
        myConfigValues.put(CFG_ALBUM_IMAGE_SIZE, "128");
        myConfigValues.put(CFG_SHOW_REMOTE_CONTROL, "true");
        myConfigValues.put(CFG_KEEP_ALIVE, "false");
    }

    private void initWithIphoneDefaults() {
        myConfigValues.put(CFG_FEED_TYPE_RSS, "false");
        myConfigValues.put(CFG_PAGE_SIZE, "30");
        myConfigValues.put(CFG_SHOW_DOWNLOAD, "false");
        myConfigValues.put(CFG_SHOW_PLAYER, "false");
        myConfigValues.put(CFG_PLAYLIST_TYPE, PlaylistType.QtPlugin.name());
        myConfigValues.put(CFG_ALBUM_IMAGE_SIZE, "256");
    }

    private void initWithPspDefaults() {
        myConfigValues.put(CFG_FEED_TYPE_PLAYLIST, "false");
        myConfigValues.put(CFG_RSS_LIMIT, "100");
        myConfigValues.put(CFG_PAGE_SIZE, "30");
        myConfigValues.put(CFG_SHOW_PLAYER, "false");
    }

    private void initWithNintendoWiiDefaults() {
        myConfigValues.put(CFG_FEED_TYPE_RSS, "false");
        myConfigValues.put(CFG_FEED_TYPE_PLAYLIST, "false");
        myConfigValues.put(CFG_SHOW_DOWNLOAD, "false");
        myConfigValues.put(CFG_PAGE_SIZE, "30");
        myConfigValues.put(CFG_SHOW_PLAYER, "true");
        myConfigValues.put(CFG_FLASH_PLAYER_TYPE, "jw3");
    }

    public void load(User user) {
        if (user != null && StringUtils.isNotEmpty(user.getWebSettings())) {
            initFromString(MyTunesRssBase64Utils.decodeToString(user.getWebSettings()));
        }
    }

    public void clearWithDefaults(HttpServletRequest request) {
        clear();
        initWithDefaults(request);
    }

    public void load(HttpServletRequest request) {
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
            cookie.setComment("MyTunesRSS settings cookie");
            cookie.setMaxAge(3600 * 24 * 365);// one year
            String servletUrl = MyTunesRssWebUtils.getServletUrl(request);
            cookie.setPath(servletUrl.substring(servletUrl.lastIndexOf("/")));
            response.addCookie(cookie);
        }
    }

    public String createCookieValue() {
        StringBuffer value = new StringBuffer();
        for (Map.Entry<String, String> entry : myConfigValues.entrySet()) {
            value.append(";").append(entry.getKey()).append("=").append(entry.getValue());
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

    public boolean isLoginStored() {
        String passwordHashStored = myConfigValues.get(CFG_LOGIN_STORED);
        return Boolean.valueOf(passwordHashStored);
    }

    public void setLoginStored(boolean passwordHashStored) {
        myConfigValues.put(CFG_LOGIN_STORED, Boolean.toString(passwordHashStored));
    }

    public byte[] getPasswordHash() {
        String passwordHash = myConfigValues.get(CFG_PASSWORD_HASH);
        if (StringUtils.isNotEmpty(passwordHash)) {
            try {
                return MyTunesRssBase64Utils.decode(passwordHash);
            } catch (IllegalArgumentException e) {
                return null;// ignore exception
            }
        }
        return null;
    }

    public void setPasswordHash(byte[] passwordHash) {
        myConfigValues.put(CFG_PASSWORD_HASH, MyTunesRssBase64Utils.encode(passwordHash));
    }

    public String getUserName() {
        return myConfigValues.get(CFG_USER_NAME);
    }

    public User getUser() {
        return MyTunesRss.CONFIG.getUser(getUserName());
    }

    public void setUserName(String userName) {
        myConfigValues.put(CFG_USER_NAME, userName);
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

    public String getPlaylistFileSuffix() {
        return PlaylistType.valueOf(getPlaylistType()).getFileSuffix();
    }

    public MyTunesRssResource getPlaylistTemplateResource() {
        return PlaylistType.valueOf(getPlaylistType()).getTemplateResource();
    }

    public int getLameTargetBitrate() {
        return Integer.parseInt(myConfigValues.get(CFG_LAME_TARGET_BITRATE));
    }

    public void setLameTargetBitrate(int lameTargetBitrate) {
        myConfigValues.put(CFG_LAME_TARGET_BITRATE, Integer.toString(lameTargetBitrate));
    }

    public int getLameTargetSampleRate() {
        return Integer.parseInt(myConfigValues.get(CFG_LAME_TARGET_SAMPLE_RATE));
    }

    public void setLameTargetSampleRate(int lameTargetFrequency) {
        myConfigValues.put(CFG_LAME_TARGET_SAMPLE_RATE, Integer.toString(lameTargetFrequency));
    }

    public boolean isTranscodeOnTheFlyIfPossible() {
        return Boolean.parseBoolean(myConfigValues.get(CFG_TRANSCODE_OTF_IF_POSSIBLE));
    }

    public void setTranscodeOnTheFlyIfPossible(boolean transcodeOnTheFlyIfPossible) {
        myConfigValues.put(CFG_TRANSCODE_OTF_IF_POSSIBLE, Boolean.toString(transcodeOnTheFlyIfPossible));
    }

    public String getRandomSource() {
        return myConfigValues.get(CFG_RANDOM_SOURCE);
    }

    public void setRandomSource(String source) {
        myConfigValues.put(CFG_RANDOM_SOURCE, source);
    }

    public String getFlashplayerType() {
        return myConfigValues.get(CFG_FLASH_PLAYER_TYPE);
    }

    public void setFlashplayerType(String type) {
        myConfigValues.put(CFG_FLASH_PLAYER_TYPE, type);
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

    public String getLanguage() {
        return myConfigValues.get(CFG_LANGUAGE);
    }

    public void setLanguage(String lc) {
        if (StringUtils.isNotBlank(lc)) {
            myConfigValues.put(CFG_LANGUAGE, lc);
        } else {
            myConfigValues.remove(CFG_LANGUAGE);
        }
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

    public boolean isActiveTranscoder(String name) {
        return ArrayUtils.contains(StringUtils.split(getActiveTranscoders(), ','), name);
    }

    public TranscoderConfig getTranscoder(Track track) {
        for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            if (isActiveTranscoder(config.getName()) && config.isValidFor(FileSupportUtils.getFileSuffix(track.getFilename()), track.getMp4Codec(), track.getMediaType())) {
                return config;
            }
        }
        return null;
    }

    public boolean isKeepAlive() {
        return Boolean.valueOf(myConfigValues.get(CFG_KEEP_ALIVE));
    }

    public void setKeepAlive(boolean keepAlive) {
        myConfigValues.put(CFG_KEEP_ALIVE, Boolean.toString(keepAlive));
    }
}