/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.MiscUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * de.codewave.mytunesrss.jsp.MyTunesFunctions
 */
public class MyTunesFunctions {
    private static final Log LOG = LogFactory.getLog(MyTunesFunctions.class);

    private static final String DEFAULT_NAME = "MyTunesRSS";

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
        if (config != null && user != null && FileSupportUtils.isMp4(track.getFile()) &&
                user.isTranscoder()) {
            if ("alac".equals(track.getMp4Codec()) && config.isAlac() && MyTunesRss.CONFIG.isValidAlacBinary()) {
                return "mp3";
            } else if ("mp4a".equals(track.getMp4Codec()) && config.isFaad2() && MyTunesRss.CONFIG.isValidFaad2Binary()) {
                return "mp3";
            }
        }
        return FilenameUtils.getExtension(track.getFile().getName());
    }

    public static String contentType(WebConfig config, User user, Track track) {
        return FileSupportUtils.getContentType("dummy." + suffix(config, user, track), track.isVideo());
    }

    public static boolean transcoding(WebConfig config, User user, Track track) {
        if (config != null && user != null && user.isTranscoder()) {
            if (FileSupportUtils.isMp4(track.getFile()) && "alac".equals(track.getMp4Codec()) && config.isAlac() &&
                    MyTunesRss.CONFIG.isValidAlacBinary()) {
                return true;
            } else if (FileSupportUtils.isMp4(track.getFile()) && "mp4a".equals(track.getMp4Codec()) && config.isFaad2() &&
                    MyTunesRss.CONFIG.isValidFaad2Binary()) {
                return true;
            } else if (FileSupportUtils.isMp3(track.getFile()) && config.isLame() && MyTunesRss.CONFIG.isValidLameBinary()) {
                return true;
            }
        }
        return false;
    }

    public static String tcParamValue(WebConfig config, User user, Track track) {
        if (config != null && user != null && user.isTranscoder() &&
                MyTunesRss.CONFIG.isValidLameBinary()) {
            if (FileSupportUtils.isMp4(track.getFile())) {
                if ("alac".equals(track.getMp4Codec()) && config.isAlac() && MyTunesRss.CONFIG.isValidAlacBinary()) {
                    return config.getLameTargetBitrate() + "," + config.getLameTargetSampleRate() + "," + config.isTranscodeOnTheFlyIfPossible();
                } else if ("mp4a".equals(track.getMp4Codec()) && config.isFaad2() && MyTunesRss.CONFIG.isValidFaad2Binary()) {
                    return config.getLameTargetBitrate() + "," + config.getLameTargetSampleRate() + "," + config.isTranscodeOnTheFlyIfPossible();
                }
            } else if (FileSupportUtils.isMp3(track.getFile()) && config.isLame()) {
                return config.getLameTargetBitrate() + "," + config.getLameTargetSampleRate() + "," + config.isTranscodeOnTheFlyIfPossible();
            }
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
        String value1 = (String)request.getAttribute("flipFlop_value1");
        String value2 = (String)request.getAttribute("flipFlop_value2");
        String currentValue = (String)request.getAttribute("flipFlop_currentValue");
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

    public static int getButtonColumns(User user, WebConfig config) {
        int count = 0;
        if (user.isRss() && config.isShowRss()) {
            count++;
        }
        if (user.isPlaylist() && config.isShowPlaylist()) {
            count++;
        }
        if (user.isDownload() && config.isShowDownload()) {
            count++;
        }
        if (user.isPlayer() && config.isShowPlayer()) {
            count++;
        }
        return count;
    }

    public static int getSectionTrackCount(String sectionIds) {
        return StringUtils.split(sectionIds, ",").length;
    }

    public static String formatDateAsDateAndTime(HttpServletRequest request, long milliseconds) {
        LocalizationContext context = (LocalizationContext)request.getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
        ResourceBundle bundle = context != null ? context.getResourceBundle() : ResourceBundle.getBundle("de/codewave/mytunesrss/MyTunesRssWeb",
                                                                                                         request.getLocale());
        SimpleDateFormat format = new SimpleDateFormat(bundle.getString("dateAndTimeFormat"));
        return format.format(new Date(milliseconds));
    }

    public static String[] splitComments(String comments) {
        return StringUtils.split(comments, '\n');
    }

    public static String makeHttp(String url) {
        String schemePrefix = "https://";
        if (url != null && url.toLowerCase().startsWith(schemePrefix)) {
            int httpPort = MyTunesRss.CONFIG.getTomcatProxyPort() > 0 && MyTunesRss.CONFIG.getTomcatProxyPort() < 65536 ?
                    MyTunesRss.CONFIG.getTomcatProxyPort() : MyTunesRss.CONFIG.getPort();
            int serverSeparator = url.indexOf("/", schemePrefix.length());
            if (serverSeparator == -1) {
                serverSeparator = url.length();
            }
            int portSeparator = url.indexOf(':', schemePrefix.length());
            String oldHost = portSeparator != -1 ? url.substring(schemePrefix.length(), portSeparator) : url.substring(schemePrefix.length(),
                                                                                                                     serverSeparator);
            String httpHost = StringUtils.isNotEmpty(MyTunesRss.CONFIG.getTomcatProxyHost()) ? MyTunesRss.CONFIG.getTomcatProxyHost() : oldHost;
            return "http://" + httpHost + ":" + httpPort + (serverSeparator < url.length() ? url.substring(serverSeparator) : "");
        }
        return url;
    }
}