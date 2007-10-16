package de.codewave.mytunesrss;

import de.codewave.utils.servlet.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.User
 */
public class User {
    private static final Log LOG = LogFactory.getLog(User.class);

    public enum QuotaType {
        None, Day, Week, Month;

        @Override
        public String toString() {
            return MyTunesRssUtils.getBundleString("editUser.quotaType." + name());
        }
    }

    private boolean myActive = true;
    private String myName;
    private byte[] myPasswordHash;
    private boolean myDownload;
    private boolean myRss;
    private boolean myPlaylist;
    private boolean myUpload;
    private boolean myPlayer;
    private boolean myChangePassword;
    private QuotaType myQuotaType;
    private long myDownBytes;
    private long myQuotaDownBytes;
    private long myBytesQuota;
    private long myResetTime;
    private long myQuotaResetTime;
    private int myMaximumZipEntries;
    private String myFileTypes;
    private int mySessionTimeout = 10;
    private boolean mySpecialPlaylists;
    private boolean myTranscoder;
    private int myBandwidthLimit;
    private String myPlaylistId;
    private boolean mySaveWebSettings;
    private String myWebSettings;

    public User(String name) {
        myName = name;
        myResetTime = System.currentTimeMillis();
        myQuotaResetTime = myResetTime;
        myQuotaType = QuotaType.None;
    }

    public boolean isActive() {
        return myActive;
    }

    public void setActive(boolean active) {
        myActive = active;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public byte[] getPasswordHash() {
        return myPasswordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        myPasswordHash = passwordHash;
    }

    public boolean isDownload() {
        return myDownload;
    }

    public void setDownload(boolean download) {
        myDownload = download;
    }

    public boolean isPlaylist() {
        return myPlaylist;
    }

    public void setPlaylist(boolean playlist) {
        myPlaylist = playlist;
    }

    public boolean isRss() {
        return myRss;
    }

    public void setRss(boolean rss) {
        myRss = rss;
    }

    public boolean isUpload() {
        return MyTunesRss.REGISTRATION.isRegistered() && myUpload;
    }

    public void setUpload(boolean upload) {
        myUpload = upload;
    }

    public boolean isPlayer() {
        return MyTunesRss.REGISTRATION.isRegistered() && myPlayer;
    }

    public void setPlayer(boolean player) {
        myPlayer = player;
    }

    public boolean isChangePassword() {
        return MyTunesRss.REGISTRATION.isRegistered() && myChangePassword;
    }

    public void setChangePassword(boolean changePassword) {
        myChangePassword = changePassword;
    }

    public long getBytesQuota() {
        return myBytesQuota;
    }

    public void setBytesQuota(long bytesQuota) {
        myBytesQuota = bytesQuota;
    }

    public long getDownBytes() {
        return myDownBytes;
    }

    public void setDownBytes(long downBytes) {
        myDownBytes = downBytes;
    }

    public long getQuotaDownBytes() {
        return myQuotaDownBytes;
    }

    public void setQuotaDownBytes(long quotaDownBytes) {
        myQuotaDownBytes = quotaDownBytes;
    }

    public long getQuotaResetTime() {
        return myQuotaResetTime;
    }

    public void setQuotaResetTime(long quotaResetTime) {
        myQuotaResetTime = quotaResetTime;
    }

    public QuotaType getQuotaType() {
        return MyTunesRss.REGISTRATION.isRegistered() ? myQuotaType : QuotaType.None;
    }

    public void setQuotaType(QuotaType quotaType) {
        myQuotaType = quotaType;
    }

    public boolean isQuota() {
        return myQuotaType != QuotaType.None;
    }

    public long getResetTime() {
        return myResetTime;
    }

    public void setResetTime(long resetTime) {
        myResetTime = resetTime;
    }

    public int getMaximumZipEntries() {
        return myMaximumZipEntries;
    }

    public void setMaximumZipEntries(int maximumZipEntries) {
        myMaximumZipEntries = maximumZipEntries;
    }

    public String getFileTypes() {
        return myFileTypes;
    }

    public void setFileTypes(String fileTypes) {
        myFileTypes = fileTypes;
    }

    public int getSessionTimeout() {
        return mySessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        mySessionTimeout = sessionTimeout;
    }

    public boolean isSpecialPlaylists() {
        return mySpecialPlaylists;
    }

    public void setSpecialPlaylists(boolean specialPlaylists) {
        mySpecialPlaylists = specialPlaylists;
    }

    public boolean isTranscoder() {
        return myTranscoder;
    }

    public void setTranscoder(boolean transcoder) {
        myTranscoder = transcoder;
    }

    public int getBandwidthLimit() {
        return myBandwidthLimit;
    }

    public void setBandwidthLimit(int bandwidthLimit) {
        myBandwidthLimit = bandwidthLimit;
    }

    public String getPlaylistId() {
        return myPlaylistId;
    }

    public void setPlaylistId(String playlistId) {
        myPlaylistId = playlistId;
    }

    public boolean isSaveWebSettings() {
        return mySaveWebSettings;
    }

    public void setSaveWebSettings(boolean saveWebSettings) {
        mySaveWebSettings = saveWebSettings;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof User && getName().equals(((User)object).getName());
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    public boolean isQuotaExceeded() {
        if (myQuotaType != QuotaType.None && myBytesQuota > 0) {
            checkQuotaReset();
            return myBytesQuota > 0 && myQuotaDownBytes >= myBytesQuota;
        }
        return false;
    }

    public long getQuotaRemaining() {
        if (myQuotaType != QuotaType.None && myBytesQuota > 0) {
            checkQuotaReset();
            return Math.max(myBytesQuota - myQuotaDownBytes, 0);
        }
        return 0;
    }

    private void checkQuotaReset() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(myQuotaResetTime));
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        switch (myQuotaType) {
            case Month:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, 1);
                break;
            case Week:
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case Day:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
        }
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            myQuotaDownBytes = 0;
            myQuotaResetTime = System.currentTimeMillis();
        }
    }

    public StreamSender.OutputStreamWrapper getOutputStreamWrapper(final int bitrate) {
        return getOutputStreamWrapper(bitrate, 0, null);
    }

    public StreamSender.OutputStreamWrapper getOutputStreamWrapper(int bitrate, int dataOffset, RangeHeader rangeHeader) {
        final int limit = Math.min(bitrate, getBandwidthLimit() > 0 ? getBandwidthLimit() : Integer.MAX_VALUE);
        if (limit > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using bandwidth limited output stream with " + limit + " kbit.");
            }
            return new MyTunesRSSOutputStreamWrapper(bitrate, dataOffset, rangeHeader.getFirstByte(), 2);
        }
        return new StreamSender.OutputStreamWrapper() {
            public OutputStream wrapStream(OutputStream stream) {
                return stream;
            }
        };
    }

    public String getWebSettings() {
        return myWebSettings;
    }

    public void setWebSettings(String webSettings) {
        myWebSettings = webSettings;
    }

    public void loadFromPreferences(Preferences userNode) {
        setActive(userNode.getBoolean("active", true));
        setPasswordHash(userNode.getByteArray("password", null));
        setRss(userNode.getBoolean("featureRss", false));
        setPlaylist(userNode.getBoolean("featurePlaylist", false));
        setDownload(userNode.getBoolean("featureDownload", false));
        setUpload(userNode.getBoolean("featureUpload", false));
        setPlayer(userNode.getBoolean("featurePlayer", false));
        setChangePassword(userNode.getBoolean("featureChangePassword", false));
        setSpecialPlaylists(userNode.getBoolean("featureSpecialPlaylists", false));
        setResetTime(userNode.getLong("resetTime", System.currentTimeMillis()));
        setQuotaResetTime(userNode.getLong("quotaResetTime", System.currentTimeMillis()));
        setDownBytes(userNode.getLong("downBytes", 0));
        setQuotaDownBytes(userNode.getLong("quotaDownBytes", 0));
        setBytesQuota(userNode.getLong("bytesQuota", 0));
        setQuotaType(QuotaType.valueOf(userNode.get("quotaType", QuotaType.None.name())));
        setMaximumZipEntries(userNode.getInt("maximumZipEntries", 0));
        setFileTypes(userNode.get("fileTypes", null));
        setTranscoder(userNode.getBoolean("featureTranscoder", false));
        setSessionTimeout(userNode.getInt("sessionTimeout", 10));
        setBandwidthLimit(userNode.getInt("bandwidthLimit", 0));
        setPlaylistId(userNode.get("playlistId", null));
        setSaveWebSettings(userNode.getBoolean("saveWebSettings", false));
        setWebSettings(userNode.get("webSettings", null));
    }

    public void saveToPreferences(Preferences userNode) {
        if (getPasswordHash() != null && getPasswordHash().length > 0) {
            userNode.putByteArray("password", getPasswordHash());
        } else {
            userNode.remove("password");
        }
        userNode.putBoolean("active", isActive());
        userNode.putBoolean("featureRss", isRss());
        userNode.putBoolean("featurePlaylist", isPlaylist());
        userNode.putBoolean("featureDownload", isDownload());
        userNode.putBoolean("featureUpload", isUpload());
        userNode.putBoolean("featurePlayer", isPlayer());
        userNode.putBoolean("featureChangePassword", isChangePassword());
        userNode.putBoolean("featureSpecialPlaylists", isSpecialPlaylists());
        userNode.putLong("resetTime", getResetTime());
        userNode.putLong("quotaResetTime", getQuotaResetTime());
        userNode.putLong("downBytes", getDownBytes());
        userNode.putLong("quotaDownBytes", getQuotaDownBytes());
        userNode.putLong("bytesQuota", getBytesQuota());
        userNode.put("quotaType", getQuotaType().name());
        userNode.putInt("maximumZipEntries", getMaximumZipEntries());
        userNode.put("fileTypes", getFileTypes() != null ? getFileTypes() : "");
        userNode.putBoolean("featureTranscoder", isTranscoder());
        userNode.putInt("sessionTimeout", getSessionTimeout());
        userNode.putInt("bandwidthLimit", getBandwidthLimit());
        if (StringUtils.isNotEmpty(getPlaylistId())) {
            userNode.put("playlistId", getPlaylistId());
        } else {
            userNode.remove("playlistId");
        }
        userNode.putBoolean("saveWebSettings", isSaveWebSettings());
        if (StringUtils.isNotEmpty(getWebSettings())) {
            userNode.put("webSettings", getWebSettings());
        } else {
            userNode.remove("webSettings");
        }
    }
}
