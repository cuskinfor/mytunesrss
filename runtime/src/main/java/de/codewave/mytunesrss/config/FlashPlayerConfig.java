package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for a flash player.
 */
public class FlashPlayerConfig implements Comparable<FlashPlayerConfig>, Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlashPlayerConfig.class);

    public static final FlashPlayerConfig JW316 = new FlashPlayerConfig("mytunesrss_jwmediaplayer316", "JW Media Player 3.16", PlaylistFileType.Xspf, 600, 450, TimeUnit.SECONDS, 256);
    public static final FlashPlayerConfig JW46 = new FlashPlayerConfig("mytunesrss_jwmediaplayer", "JW Media Player 4.6", PlaylistFileType.Xspf, 600, 276, TimeUnit.SECONDS, 256);
    public static final FlashPlayerConfig JW46_SHUFFLE = new FlashPlayerConfig("mytunesrss_jwmediaplayer_shuffle", "JW Media Player 4.6 (Shuffle)", PlaylistFileType.Xspf, 600, 276, TimeUnit.SECONDS, 256);
    public static final FlashPlayerConfig SIMPLE = new FlashPlayerConfig("mytunesrss_simple", "XSPF Player", PlaylistFileType.Xspf, 600, 450, TimeUnit.MILLISECONDS, 256);

    public static final FlashPlayerConfig ABSOLUTE_DEFAULT = JW46;

    private static final Set<FlashPlayerConfig> DEFAULTS = new HashSet<>();

    static {
        DEFAULTS.add(JW316);
        DEFAULTS.add(JW46);
        DEFAULTS.add(JW46_SHUFFLE);
        DEFAULTS.add(SIMPLE);
    }

    private static String getPlayerCode(String file) {
        InputStream stream = FlashPlayerConfig.class.getResourceAsStream(file);
        try {
            return StringUtils.join(IOUtils.readLines(stream), System.getProperty("line.separator"));
        } catch (IOException ignored) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read stream from \"" + file + "\".");
            }
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not close stream from \"" + file + "\".");
                }
            }
        }
        return "<html><body><h1>Jukebox could not be loaded!</h1></body></html>";
    }

    public static Set<FlashPlayerConfig> getDefaults() {
        Set<FlashPlayerConfig> defaults = new HashSet<>();
        for (FlashPlayerConfig config : DEFAULTS) {
            defaults.add((FlashPlayerConfig) config.clone());
        }
        return defaults;
    }

    public static FlashPlayerConfig getDefault() {
        return (FlashPlayerConfig) JW46.clone();
    }

    private String myId;
    private String myName;
    private PlaylistFileType myPlaylistFileType;
    private int myWidth;
    private int myHeight;
    private TimeUnit myTimeUnit;
    private int myImageSize;

    public FlashPlayerConfig(String id, String name, PlaylistFileType playlistFileType, int width, int height, TimeUnit timeUnit, int imageSize) {
        myId = id;
        myName = name;
        myPlaylistFileType = playlistFileType;
        myWidth = width;
        myHeight = height;
        myTimeUnit = timeUnit;
        myImageSize = imageSize;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public PlaylistFileType getPlaylistFileType() {
        return myPlaylistFileType;
    }

    public void setPlaylistFileType(PlaylistFileType playlistFileType) {
        myPlaylistFileType = playlistFileType;
    }

    public int getWidth() {
        return myWidth;
    }

    public void setWidth(int width) {
        myWidth = width;
    }

    public int getHeight() {
        return myHeight;
    }

    public void setHeight(int height) {
        myHeight = height;
    }

    public TimeUnit getTimeUnit() {
        return myTimeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        myTimeUnit = timeUnit;
    }

    public int getImageSize() {
        return myImageSize;
    }

    public void setImageSize(int imageSize) {
        myImageSize = imageSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlashPlayerConfig that = (FlashPlayerConfig) o;

        return !(myId != null ? !myId.equals(that.myId) : that.myId != null);

    }

    @Override
    public int hashCode() {
        return myId != null ? myId.hashCode() : 0;
    }

    @Override
    public int compareTo(FlashPlayerConfig o) {
        if (o == null || o.myName == null) {
            return 1;
        } else if (myName == null) {
            return -1;
        } else {
            return myName.compareTo(o.myName);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            FlashPlayerConfig clone = (FlashPlayerConfig) super.clone();
            clone.myId = myId;
            clone.myName = myName;
            clone.myPlaylistFileType = myPlaylistFileType;
            clone.myWidth = myWidth;
            clone.myHeight = myHeight;
            clone.myTimeUnit = myTimeUnit;
            clone.myImageSize = myImageSize;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone flash player config.", e);
        }
    }

    public File getBaseDir() {
        return new File(MyTunesRss.PREFERENCES_DATA_PATH + "/flashplayer", myId);
    }

}
