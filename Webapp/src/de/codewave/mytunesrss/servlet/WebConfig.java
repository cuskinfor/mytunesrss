/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.utils.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.servlet.http.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.servlet.WebConfig
 */
public class WebConfig {
    private static final Log LOG = LogFactory.getLog(WebConfig.class);

    private static final String CONFIG_COOKIE_NAME = "MyTunesRSSConfig";
    private static final String CFG_USER_NAME = "userName";
    private static final String CFG_PASSWORD_HASH = "passwordHash";
    private static final String CFG_LOGIN_STORED = "rememberLogin";
    private static final String CFG_FEED_TYPE_RSS = "feedTypeRss";
    private static final String CFG_FEED_TYPE_M3U = "feedTypeM3u";
    private static final String CFG_RSS_LIMIT = "rssLimit";
    private static final String CFG_PAGE_SIZE = "pageSize";
    private static final String CFG_SHOW_DOWNLOAD = "showDownload";
    private static final String CFG_RSS_ARTWORK = "rssArtwork";
    private static final String CFG_RANDOM_PLAYLIST_SIZE = "randomPlaylistSize";
    private static Map<String, String> FEED_FILE_SUFFIXES = new HashMap<String, String>();

    Map<String, String> myConfigValues = new HashMap<String, String>();

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

    private void initWithDefaults() {
        myConfigValues.put(CFG_FEED_TYPE_RSS, "true");
        myConfigValues.put(CFG_FEED_TYPE_M3U, "true");
        myConfigValues.put(CFG_RSS_LIMIT, "0");
        myConfigValues.put(CFG_LOGIN_STORED, "false");
        myConfigValues.put(CFG_PASSWORD_HASH, "");
        myConfigValues.put(CFG_PAGE_SIZE, "0");
        myConfigValues.put(CFG_SHOW_DOWNLOAD, "true");
        myConfigValues.put(CFG_RSS_ARTWORK, "true");
        myConfigValues.put(CFG_RANDOM_PLAYLIST_SIZE, "25");
    }

    public void load(HttpServletRequest request) {
        clear();
        initWithDefaults();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (CONFIG_COOKIE_NAME.equals(cookie.getName())) {
                    String cookieValue = "";
                    try {
                        cookieValue = Base64Utils.decodeToString(cookie.getValue());
                    } catch (Exception e) {
                        // intentionally left blank
                    }
                    for (String keyValueToken : StringUtils.split(cookieValue, ';')) {
                        int k = keyValueToken.indexOf('=');
                        if (k > 0) {
                            myConfigValues.put(keyValueToken.substring(0, k), k < keyValueToken.length() - 1 ? keyValueToken.substring(k + 1) : "");
                        } else {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("Illegal configuration token found in cookie: \"" + keyValueToken + "\".");
                            }
                        }
                    }
                }
            }
        }
    }

    public void save(HttpServletResponse response) {
        StringBuffer value = new StringBuffer();
        for (Map.Entry<String, String> entry : myConfigValues.entrySet()) {
            value.append(";").append(entry.getKey()).append("=").append(entry.getValue());
        }
        Cookie cookie = new Cookie(CONFIG_COOKIE_NAME, Base64Utils.encode(value.substring(1)));
        cookie.setComment("MyTunesRSS settings cookie");
        cookie.setMaxAge(3600 * 24 * 365);// one year
        response.addCookie(cookie);
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
                return Base64Utils.decode(passwordHash);
            } catch (IllegalArgumentException e) {
                return null;// ignore exception
            }
        }
        return null;
    }

    public void setPasswordHash(byte[] passwordHash) {
        myConfigValues.put(CFG_PASSWORD_HASH, Base64Utils.encode(passwordHash));
    }

    public String getUserName() {
        return myConfigValues.get(CFG_USER_NAME);
    }

    public void setUserName(String userName) {
        myConfigValues.put(CFG_USER_NAME, userName);
    }

    public boolean isShowRss() {
        return Boolean.valueOf(myConfigValues.get(CFG_FEED_TYPE_RSS));
    }

    public void setShowRss(boolean showRss) {
        myConfigValues.put(CFG_FEED_TYPE_RSS, Boolean.toString(showRss));
    }

    public boolean isShowM3u() {
        return Boolean.valueOf(myConfigValues.get(CFG_FEED_TYPE_M3U));
    }

    public void setShowM3u(boolean showM3u) {
        myConfigValues.put(CFG_FEED_TYPE_M3U, Boolean.toString(showM3u));
    }

    public int getFeedTypeCount() {
        int count = isShowRss() ? 1 : 0;
        count += (isShowM3u() ? 1 : 0);
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

    public boolean isRssArtwork() {
        return Boolean.valueOf(myConfigValues.get(CFG_RSS_ARTWORK));
    }

    public void setRssArtwork(boolean rssArtwork) {
        myConfigValues.put(CFG_RSS_ARTWORK, Boolean.toString(rssArtwork));
    }

    public int getRandomPlaylistSize() {
        return Integer.parseInt(myConfigValues.get(CFG_RANDOM_PLAYLIST_SIZE));
    }

    public void setRandomPlaylistSize(int count) {
        myConfigValues.put(CFG_RANDOM_PLAYLIST_SIZE, Integer.toString(count));
    }
}