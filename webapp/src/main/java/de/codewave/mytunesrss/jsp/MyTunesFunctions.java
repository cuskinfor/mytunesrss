/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.MiscUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.io.JsonStringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * de.codewave.mytunesrss.jsp.MyTunesFunctions
 */
public class MyTunesFunctions {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesFunctions.class);

    private static final String DEFAULT_NAME = "MyTunesRSS";

    private static final SimpleDateFormat PUBLISH_DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);

    public static String webSafeFileName(String name) {
        name = getLegalFileName(name);
        return MiscUtils.encodeUrl(name);
    }

    public static String getLegalFileName(String name) {
        name = name.replace('/', '_');
        name = name.replace('\\', '_');
        name = name.replace('?', '_');
        name = name.replace('*', '_');
        name = name.replace(':', '_');
        name = name.replace('|', '_');
        name = name.replace('\"', '_');
        name = name.replace('<', '_');
        name = name.replace('>', '_');
        name = name.replace('`', '_');
        //        name = name.replace('´', '_');
        name = name.replace('\'', '_');
        return name;
    }

    public static boolean unknown(String trackAlbumOrArtist) {
        return InsertTrackStatement.UNKNOWN.equals(trackAlbumOrArtist);
    }

    public static String virtualTrackName(Track track) {
        if (unknown(track.getArtist())) {
            return webSafeFileName(track.getName());
        }
        return webSafeFileName(track.getArtist() + " - " + track.getName());
    }

    public static String virtualAlbumName(Album album) {
        if (unknown(album.getArtist()) && unknown(album.getName())) {
            return DEFAULT_NAME;
        } else if (unknown(album.getArtist()) || album.getArtistCount() > 1) {
            return webSafeFileName(album.getName());
        }
        return webSafeFileName(album.getArtist() + " - " + album.getName());
    }

    public static String virtualArtistName(Artist artist) {
        if (unknown(artist.getName())) {
            return DEFAULT_NAME;
        }
        return webSafeFileName(artist.getName());
    }

    public static String virtualGenreName(Genre genre) {
        return webSafeFileName(genre.getName());
    }

    public static String lowerSuffix(WebConfig config, User user, Track track) {
        String suffix = suffix(config, user, track);
        return suffix != null ? suffix.toLowerCase() : suffix;
    }

    public static String suffix(WebConfig config, User user, Track track) {
        if (config != null && user != null) {
            TranscoderConfig transcoderConfig = user.getForceTranscoder(track);
            if (transcoderConfig != null) {
                return transcoderConfig.getTargetSuffix();
            }
            if (user.isTranscoder()) {
                transcoderConfig = MyTunesRssWebUtils.getTranscoder(config.getActiveTranscoders(), track);
                if (transcoderConfig != null) {
                    return transcoderConfig.getTargetSuffix();
                }
            }
        }
        return FilenameUtils.getExtension(track.getFile().getName());
    }

    public static String contentType(WebConfig config, User user, Track track) {
        return FileSupportUtils.getContentType("dummy." + suffix(config, user, track));
    }

    public static boolean transcoding(PageContext pageContext, User user, Track track) {
        if (user != null && user.isTranscoder()) {
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            WebConfig config = MyTunesRssWebUtils.getWebConfig(request);
            return MyTunesRssWebUtils.getTranscoder(config.getActiveTranscoders(), track) != null;
        }
        return false;
    }

    public static String tcParamValue(PageContext pageContext, User user) {
        return tcParamValue((HttpServletRequest) pageContext.getRequest(), user);
    }

    public static String tcParamValue(HttpServletRequest request, User user) {
        if (user != null && user.isTranscoder()) {
            WebConfig config = MyTunesRssWebUtils.getWebConfig(request);
            return MyTunesRssWebUtils.createTranscodingPathInfo(config);
        }

        return "";
    }

    public static String replace(String string, String target, String replacement) {
        return string.replace(target, replacement);
    }

    public static String getDuration(Track track) {
        int time = track.getTime();
        int hours = time / 3600;
        int minutes = (time - (hours * 3600)) / 60;
        int seconds = time % 60;
        if (hours > 0) {
            return getTwoDigitString(hours) + ":" + getTwoDigitString(minutes) + ":" + getTwoDigitString(seconds);
        }
        return getTwoDigitString(minutes) + ":" + getTwoDigitString(seconds);
    }

    private static String getTwoDigitString(int value) {
        if (value < 0 || value > 99) {
            throw new IllegalArgumentException("Cannot make a two digit string from value \"" + value + "\".");
        }
        if (value < 10) {
            return "0" + value;
        }
        return Integer.toString(value);
    }

    public static void initializeFlipFlop(HttpServletRequest request, String value1, String value2) {
        request.setAttribute("flipFlop_value1", value1);
        request.setAttribute("flipFlop_value2", value2);
        request.setAttribute("flipFlop_currentValue", value1);
    }

    public static String flipFlop(HttpServletRequest request) {
        String value1 = (String) request.getAttribute("flipFlop_value1");
        String value2 = (String) request.getAttribute("flipFlop_value2");
        String currentValue = (String) request.getAttribute("flipFlop_currentValue");
        if (value1.equals(currentValue)) {
            request.setAttribute("flipFlop_currentValue", value2);
        } else {
            request.setAttribute("flipFlop_currentValue", value1);
        }
        return currentValue;
    }

    public static String getMemorySizeForDisplay(long bytes) {
        return MyTunesRssUtils.getMemorySizeForDisplay(bytes);
    }

    public static int getSectionTrackCount(String sectionIds) {
        return StringUtils.split(sectionIds, ",").length;
    }

    public static String formatDateAsDateAndTime(HttpServletRequest request, long milliseconds) {
        LocalizationContext context = (LocalizationContext) request.getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
        ResourceBundle bundle = context != null ? context.getResourceBundle() : ResourceBundle.getBundle("de/codewave/mytunesrss/MyTunesRssWeb",
                request.getLocale());
        SimpleDateFormat format = new SimpleDateFormat(bundle.getString("dateAndTimeFormat"));
        return format.format(new Date(milliseconds));
    }

    public static String formatDates(HttpServletRequest request, String pre, Long milliseconds1, String mid, Long milliseconds2, String post) {
        LocalizationContext context = (LocalizationContext) request.getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
        ResourceBundle bundle = context != null ? context.getResourceBundle() : ResourceBundle.getBundle("de/codewave/mytunesrss/MyTunesRssWeb",
                request.getLocale());
        SimpleDateFormat format = new SimpleDateFormat(bundle.getString("dateFormat"));
        if (milliseconds1 < 0 && milliseconds2 >= 0) {
            return pre + format.format(new Date(milliseconds2)) + post;
        } else if (milliseconds1 >= 0 && milliseconds2 < 0) {
            return pre + format.format(new Date(milliseconds1)) + post;
        } else if (milliseconds1 >= 0 && milliseconds2 >= 0) {
            String date1 = format.format(new Date(milliseconds1));
            String date2 = format.format(new Date(milliseconds2));
            return pre + (date1.equals(date2) ? date1 : date1 + mid + date2) + post;
        } else {
            return null;
        }
    }

    public static String[] splitComments(String comments) {
        return StringUtils.split(comments, '\n');
    }

    public static List<String[]> availableLanguages(Locale displayLocale) {
        Set<String> codes = new HashSet<String>();
        for (AddonsUtils.LanguageDefinition definition : AddonsUtils.getLanguages(true)) {
            codes.add(definition.getCode());
        }
        List<String[]> langs = new ArrayList<String[]>(codes.size());
        for (String code : codes) {
            langs.add(new String[]{code, new Locale(code).getDisplayName(displayLocale)});
        }
        Collections.sort(langs, new Comparator<String[]>() {
            public int compare(String[] o1, String[] o2) {
                return o1[1].compareTo(o2[1]);
            }
        });
        return langs;
    }

    public static Locale preferredLocale(PageContext pageContext, boolean requestFallback) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        return StringUtils.isBlank(MyTunesRssWebUtils.getCookieLanguage(request)) ? (requestFallback ? pageContext.getRequest().getLocale() : null) : new Locale(MyTunesRssWebUtils.getCookieLanguage(request));
    }

    public static String httpLiveStreamUrl(PageContext pageContext, Track track, String extraPathInfo) {
        return httpLiveStreamUrl((HttpServletRequest) pageContext.getRequest(), track, extraPathInfo);
    }

    public static String httpLiveStreamUrl(HttpServletRequest request, Track track, String extraPathInfo) {
        MyTunesRssCommand command = MyTunesRssCommand.HttpLiveStream;
        HttpSession session = request.getSession();
        StringBuilder builder = new StringBuilder((String) request.getAttribute("downloadPlaybackServletUrl"));
        String auth = (String) request.getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) session.getAttribute("auth");
        }
        builder.append("/").append(command.getName()).append("/").append(auth);
        StringBuilder pathInfo = new StringBuilder("track=");
        pathInfo.append(MyTunesRssUtils.getUtf8UrlEncoded(track.getId()));
        User user = MyTunesRssWebUtils.getAuthUser(request);
        String tcParam = tcParamValue(request, user);
        if (StringUtils.isNotBlank(tcParam)) {
            pathInfo.append("/tc=").append(tcParam);
        }
        if (StringUtils.isNotBlank(extraPathInfo)) {
            pathInfo.append("/").append(extraPathInfo);
        }
        builder.append("/").append(MyTunesRssWebUtils.encryptPathInfo(request, pathInfo.toString()));
        return builder.toString();
    }

    public static String photoUrl(PageContext pageContext, Photo photo, String extraPathInfo) {
        return photoUrl((HttpServletRequest) pageContext.getRequest(), photo, extraPathInfo);
    }

    public static String photoUrl(HttpServletRequest request, Photo photo, String extraPathInfo) {
        MyTunesRssCommand command = MyTunesRssCommand.ShowPhoto;
        HttpSession session = request.getSession();
        StringBuilder builder = new StringBuilder((String) request.getAttribute("downloadPlaybackServletUrl"));
        String auth = (String) request.getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) session.getAttribute("auth");
        }
        builder.append("/").append(command.getName()).append("/").append(auth);
        StringBuilder pathInfo = new StringBuilder("photo=");
        pathInfo.append(MyTunesRssUtils.getUtf8UrlEncoded(photo.getId()));
        if (StringUtils.isNotBlank(extraPathInfo)) {
            pathInfo.append("/").append(extraPathInfo);
        }
        builder.append("/").append(MyTunesRssWebUtils.encryptPathInfo(request, pathInfo.toString()));
        builder.append("/").append(webSafeFileName(FilenameUtils.getName(StringUtils.trimToEmpty(photo.getFile()))));
        return builder.toString();
    }

    public static String playbackUrl(PageContext pageContext, Track track, String extraPathInfo) {
        return playbackUrl((HttpServletRequest) pageContext.getRequest(), track, extraPathInfo);
    }

    public static String playbackUrl(HttpServletRequest request, Track track, String extraPathInfo) {
        if (MyTunesRssWebUtils.getUserAgent(request) == UserAgent.Iphone && MyTunesRssWebUtils.isHttpLiveStreaming(request, track, false)) {
            return httpLiveStreamUrl(request, track, extraPathInfo);
        }
        MyTunesRssCommand command = MyTunesRssCommand.PlayTrack;
        HttpSession session = request.getSession();
        StringBuilder builder = new StringBuilder((String) request.getAttribute("downloadPlaybackServletUrl"));
        String auth = (String) request.getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) session.getAttribute("auth");
        }
        builder.append("/").append(command.getName()).append("/").append(auth);
        StringBuilder pathInfo = new StringBuilder("track=");
        pathInfo.append(MyTunesRssUtils.getUtf8UrlEncoded(track.getId()));
        User user = MyTunesRssWebUtils.getAuthUser(request);
        String tcParam = tcParamValue(request, user);
        if (StringUtils.isNotBlank(tcParam)) {
            pathInfo.append("/tc=").append(tcParam);
        }
        if (StringUtils.isNotBlank(extraPathInfo)) {
            pathInfo.append("/").append(extraPathInfo);
        }
        builder.append("/").append(MyTunesRssWebUtils.encryptPathInfo(request, pathInfo.toString()));
        builder.append("/").append(virtualTrackName(track)).append(".").append(suffix(MyTunesRssWebUtils.getWebConfig(request), user, track));
        return builder.toString();
    }

    public static String downloadUrl(PageContext pageContext, Track track, String extraPathInfo) {
        return downloadUrl((HttpServletRequest) pageContext.getRequest(), track, extraPathInfo);
    }

    public static String downloadUrl(HttpServletRequest request, Track track, String extraPathInfo) {
        MyTunesRssCommand command = MyTunesRssCommand.DownloadTrack;
        HttpSession session = request.getSession();
        StringBuilder builder = new StringBuilder((String) request.getAttribute("downloadPlaybackServletUrl"));
        String auth = (String) request.getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) session.getAttribute("auth");
        }
        builder.append("/").append(command.getName()).append("/").append(auth);
        StringBuilder pathInfo = new StringBuilder("track=");
        pathInfo.append(MyTunesRssUtils.getUtf8UrlEncoded(track.getId()));
        if (StringUtils.isNotBlank(extraPathInfo)) {
            pathInfo.append("/").append(extraPathInfo);
        }
        builder.append("/").append(MyTunesRssWebUtils.encryptPathInfo(request, pathInfo.toString()));
        builder.append("/").append(virtualTrackName(track)).append(".").append(FilenameUtils.getExtension(track.getFilename()));
        return builder.toString();
    }

    public static boolean isTranscoder(WebConfig webConfig, TranscoderConfig transcoderConfig) {
        return MyTunesRssWebUtils.isActiveTranscoder(webConfig.getActiveTranscoders(), transcoderConfig.getName());
    }

    public static String rssDate(long timestamp) {
        return PUBLISH_DATE_FORMAT.format(new Date(timestamp));
    }

    public static boolean isExternalSites(String type) {
        return !MyTunesRss.CONFIG.getExternalSites(type).isEmpty();
    }

    public static Map<String, String> getExternalSiteDefinitions(String type) {
        Map<String, String> result = new TreeMap<String, String>();
        for (ExternalSiteDefinition def : MyTunesRss.CONFIG.getExternalSites(type)) {
            result.put(def.getName(), def.getUrl());
        }
        return result;
    }

    public static String jsArray(Object items) {
        StringBuilder builder = new StringBuilder();
        if (items instanceof Iterable) {
            for (Object item : (Iterable) items) {
                if (item instanceof String) {
                    builder.append("'").append(StringEscapeUtils.escapeJavaScript((String) item)).append("'");
                } else {
                    builder.append(item.toString());
                }
                builder.append(",");
            }
        } else if (items.getClass().isArray()) {
            for (Object item : (Object[]) items) {
                if (item instanceof String) {
                    builder.append("'").append(StringEscapeUtils.escapeJavaScript((String) item)).append("'");
                } else {
                    builder.append(item.toString());
                }
                builder.append(",");
            }
        } else {
            throw new IllegalArgumentException("Not an iterable or an array: " + items.getClass());
        }
        return builder.substring(0, builder.length() - 1);
    }

    public static String hostFromUrl(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not create URL from \"" + url + "\". Returning complete URL as host.");
            }
        }
        return url;
    }

    public static List<FlashPlayerConfig> flashPlayerConfigs() {
        List<FlashPlayerConfig> flashPlayerConfigs = new ArrayList<FlashPlayerConfig>(FlashPlayerConfig.getDefaults());
        flashPlayerConfigs.addAll(MyTunesRss.CONFIG.getFlashPlayers());
        Collections.sort(flashPlayerConfigs);
        return flashPlayerConfigs;
    }

    public static FlashPlayerConfig flashPlayerConfig(String id) {
        FlashPlayerConfig flashPlayerConfig = MyTunesRss.CONFIG.getFlashPlayer(id);
        return flashPlayerConfig != null ? flashPlayerConfig : FlashPlayerConfig.getDefault(id);
    }

    public static String escapeJson(String json) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(json));
    }
}