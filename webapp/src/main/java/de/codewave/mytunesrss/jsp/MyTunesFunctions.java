/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.camel.mp3.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.utils.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.text.*;

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

    public static String suffix(WebConfig config, User user, Track track) {
        if (config != null && user != null && FileSupportUtils.isMp4(track.getFile()) && MyTunesRss.REGISTRATION.isRegistered() && user.isTranscoder()) {
            if ("alac".equals(track.getMp4Codec()) && config.isAlac() && MyTunesRss.CONFIG.isValidAlacBinary()) {
                return "mp3";
            } else if ("mp4a".equals(track.getMp4Codec()) && config.isFaad2() && MyTunesRss.CONFIG.isValidFaad2Binary()) {
                return "mp3";
            }
        }
        return FilenameUtils.getExtension(track.getFile().getName());
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

    public static String getDisplaySampleRate(Track track) {
        try {
            Mp3Info info = Mp3Utils.getMp3Info(new FileInputStream(track.getFile()));
            return info.getAvgSampleRate() + " Hz";
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(null, e);
            }
        }
        return null;
    }

    public static String getDisplayBitrate(Track track) {
        try {
            Mp3Info info = Mp3Utils.getMp3Info(new FileInputStream(track.getFile()));
            return info.getAvgBitrate() + " kbit";
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(null, e);
            }
        }
        return null;
    }

    public static String formatDateAsDateAndTime(HttpServletRequest request, long milliseconds) {
        ResourceBundle bundle = ResourceBundle.getBundle("de/codewave/mytunesrss/MyTunesRssWeb", request.getLocale());
        SimpleDateFormat format = new SimpleDateFormat(bundle.getString("dateAndTimeFormat"));
        return format.format(new Date(milliseconds));
    }
}