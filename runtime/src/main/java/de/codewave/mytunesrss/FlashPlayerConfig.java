package de.codewave.mytunesrss;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for a flash player.
 */
public class FlashPlayerConfig implements Comparable<FlashPlayerConfig>, Cloneable {
    private static final FlashPlayerConfig JW46 = new FlashPlayerConfig("mytunesrss_jwmediaplayer", "JW Media Player 4.6", "<embed src=\"mediaplayer-4-6.swf\" width=\"100%\" height=\"100%\" allowscriptaccess=\"always\" allowfullscreen=\"true\" flashvars=\"file={PLAYLIST_URL}&amp;linktarget=_blank&amp;playlist=right&amp;autostart=true&amp;playlistsize=350&amp;repeat=list\"/>");
    private static final FlashPlayerConfig JW46_SHUFFLE = new FlashPlayerConfig("mytunesrss_jwmediaplayer_shuffle", "JW Media Player 4.6 (Shuffle)", "<embed src=\"mediaplayer-4-6.swf\" width=\"100%\" height=\"100%\" allowscriptaccess=\"always\" allowfullscreen=\"true\" flashvars=\"file={PLAYLIST_URL}&amp;linktarget=_blank&amp;playlist=right&amp;autostart=true&amp;playlistsize=350&amp;repeat=list&amp;shuffle=true\"/>");
    private static final FlashPlayerConfig SIMPLE = new FlashPlayerConfig("mytunesrss_simple", "XSPF Player", "<embed src=\"xspf_player.swf?autoplay=true&amp;autoload=true&amp;playlist_url={PLAYLIST_URL}\" width=\"100%\" height=\"100%\" allowscriptaccess=\"always\" allowfullscreen=\"true\" flashvars=\"displaywidth=256\"/>");

    public static final FlashPlayerConfig ABSOLUTE_DEFAULT = JW46;

    private static final Set<FlashPlayerConfig> DEFAULTS = new HashSet<FlashPlayerConfig>();

    static {
        DEFAULTS.add(JW46);
        DEFAULTS.add(JW46_SHUFFLE);
        DEFAULTS.add(SIMPLE);
    }

    public static Set<FlashPlayerConfig> getDefaults() {
        return new HashSet<FlashPlayerConfig>(DEFAULTS);
    }

    public static FlashPlayerConfig getDefault(String id) {
        for (FlashPlayerConfig flashPlayer : DEFAULTS) {
            if (flashPlayer.getId().equals(id)) {
                return flashPlayer;
            }
        }
        return JW46;
    }

    private String myId;
    private String myName;
    private String myHtml;

    public FlashPlayerConfig(String id, String name, String html) {
        myId = id;
        myName = name;
        myHtml = html;
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

    public String getHtml() {
        return myHtml;
    }

    public void setHtml(String html) {
        myHtml = html;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlashPlayerConfig that = (FlashPlayerConfig) o;

        if (myId != null ? !myId.equals(that.myId) : that.myId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return myId != null ? myId.hashCode() : 0;
    }

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
    public Object clone() {
        return new FlashPlayerConfig(myId, myName, myHtml);
    }

    public File getBaseDir() throws IOException {
        return new File(MyTunesRssUtils.getPreferencesDataPath() + "/flashplayer", myId);
    }

}
