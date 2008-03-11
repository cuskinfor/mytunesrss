package de.codewave.mytunesrss;

import de.codewave.utils.PrefsUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class FileSuffixInfo {
    private static final Log LOG = LogFactory.getLog(FileSuffixInfo.class);
    private static final Properties INTERNAL_PROPERTIES = new Properties();
    private static final Properties USER_PROPERTIES = new Properties();
    private static final Set<String> SUFFIXES = new HashSet<String>();

    static {
        try {
            INTERNAL_PROPERTIES.load(FileSuffixInfo.class.getResourceAsStream("file-suffixes.properties"));
            for (Enumeration e = INTERNAL_PROPERTIES.keys(); e.hasMoreElements();) {
                SUFFIXES.add(e.nextElement().toString().split("\\.")[0].toLowerCase());
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load internal file suffix properties.", e);
            }
        }
        try {
            File file = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/file-suffixes.properties");
            if (file.isFile()) {
                USER_PROPERTIES.load(new FileInputStream(file));
                for (Enumeration e = USER_PROPERTIES.keys(); e.hasMoreElements();) {
                    SUFFIXES.add(e.nextElement().toString().split("\\.")[0].toLowerCase());
                }
            }
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not load user file suffix properties: " + e.getMessage());
            }
        }
    }

    private String myMimeType;
    private boolean myProtected;
    private boolean myVideo;

    FileSuffixInfo(String mimeType, boolean video, boolean protectedType) {
        myMimeType = mimeType;
        myVideo = video;
        myProtected = protectedType;
    }

    public String getMimeType(boolean video) {
        if (myMimeType.contains("/")) {
            return myMimeType;
        }
        return video ? "video/" + myMimeType : "audio/" + myMimeType;
    }

    public boolean isProtected() {
        return myProtected;
    }

    public boolean isVideo() {
        return myVideo;
    }

    public static Set<String> getSuffixes() {
        return SUFFIXES;
    }

    public static FileSuffixInfo getForSuffix(String suffix) {
        if (SUFFIXES.contains(suffix.toLowerCase())) {
            return new FileSuffixInfo(getString(suffix + ".mime"), getBoolean(suffix + ".video"), getBoolean(suffix + ".protected"));
        }
        return null;
    }

    private static boolean getBoolean(String key) {
        return Boolean.valueOf(getString(key));
    }

    private static String getString(String key) {
        return USER_PROPERTIES.getProperty(key, INTERNAL_PROPERTIES.getProperty(key, ""));
    }
}
