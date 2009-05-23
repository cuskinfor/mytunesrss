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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * de.codewave.mytunesrss.jsp.MyTunesFunctions
 */
public class MyTunesFunctions {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesFunctions.class);

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
        if (config != null && user != null && user.isTranscoder() && config.getTranscoder(track) != null) {
            return "mp3";
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
            return config.getTranscoder(track) != null;
        }
        return false;
    }

    public static String tcParamValue(PageContext pageContext, User user, Track track) {
        return tcParamValue((HttpServletRequest) pageContext.getRequest(), user, track);
    }

    public static String tcParamValue(HttpServletRequest request, User user, Track track) {
        if (user != null && user.isTranscoder() && MyTunesRss.CONFIG.isValidLameBinary()) {
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

    public static String[] splitComments(String comments) {
        return StringUtils.split(comments, '\n');
    }

    public static List<String[]> availableLanguages(Locale displayLocale) {
        Set<String> codes = new HashSet<String>();
        codes.add("de");
        codes.add("en");
        for (AddonsUtils.LanguageDefinition definition : AddonsUtils.getLanguages()) {
            codes.add(definition.getCode());
        }
        List<String[]> langs = new ArrayList<String[]>(codes.size());
        for (String code : codes) {
            langs.add(new String[]{code, new Locale(code).getDisplayLanguage(displayLocale)});
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
        WebConfig config = MyTunesRssWebUtils.getWebConfig(request);
        return StringUtils.isBlank(config.getLanguage()) ? (requestFallback ? pageContext.getRequest().getLocale() : null) : new Locale(config.getLanguage());
    }

    public static String playbackUrl(PageContext pageContext, Track track, String extraPathInfo) {
        return playbackUrl((HttpServletRequest) pageContext.getRequest(), track, extraPathInfo);
    }

    public static String playbackUrl(HttpServletRequest request, Track track, String extraPathInfo) {
        MyTunesRssCommand command = MyTunesRssCommand.PlayTrack;
        if (track.getSource() == TrackSource.YouTube) {
            command = MyTunesRssCommand.YouTubeRedirect;
        }
        HttpSession session = request.getSession();
        StringBuilder builder = new StringBuilder((String) request.getAttribute("downloadPlaybackServletUrl"));
        String auth = (String) request.getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) session.getAttribute("auth");
        }
        builder.append("/").append(command.getName()).append("/").append(auth);
        StringBuilder pathInfo = new StringBuilder("track=");
        User user = MyTunesRssWebUtils.getAuthUser(request);
        try {
            pathInfo.append(URLEncoder.encode(track.getId(), "UTF-8")).append("/tc=").append(tcParamValue(request, user, track));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found!");
        }
        pathInfo.append("/playerRequest=").append(request.getParameter("playerRequest"));
        if (StringUtils.isNotBlank(extraPathInfo)) {
            pathInfo.append("/").append(extraPathInfo);
        }
        builder.append("/").append(MyTunesRssWebUtils.encryptPathInfo(request, pathInfo.toString()));
        builder.append("/").append(virtualTrackName(track)).append(".").append(suffix(MyTunesRssWebUtils.getWebConfig(request), user, track));
        return builder.toString();
    }

    public static boolean isTranscoder(WebConfig webConfig, TranscoderConfig transcoderConfig) {
        return webConfig.isActiveTranscoder(transcoderConfig.getName());
    }

    public static String rssDate(long timestamp) {
        return PUBLISH_DATE_FORMAT.format(new Date(timestamp));
    }
}