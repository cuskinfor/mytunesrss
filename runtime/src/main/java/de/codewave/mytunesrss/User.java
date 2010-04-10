package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.lastfm.LastFmSession;
import de.codewave.mytunesrss.lastfm.LastFmSubmission;
import de.codewave.mytunesrss.lastfm.LastFmUtils;
import de.codewave.utils.servlet.RangeHeader;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.OutputStream;
import java.util.*;

/**
 * de.codewave.mytunesrss.User
 */
public class User implements MyTunesRssEventListener, Cloneable, Comparable<User> {
    private static final Logger LOG = LoggerFactory.getLogger(User.class);

    public void handleEvent(MyTunesRssEvent event) {
        if (event.getType() == MyTunesRssEvent.EventType.SERVER_STOPPED) {
            if (myLastFmSession != null) {
                LastFmUtils.sendSubmissions(myLastFmSession);
            }
        }
    }

    public boolean isForceTranscoders() {
        return myForceTranscoders != null && !myForceTranscoders.isEmpty();
    }

    public int compareTo(User o) {
        return myName == null ? -1 : myName.compareTo(o.myName);
    }

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
    private boolean myEditLastFmAccount;
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
    private Set<String> myPlaylistIds = new HashSet<String>();
    private boolean mySaveWebSettings;
    private String myWebSettings;
    private boolean myCreatePlaylists;
    private boolean myEditWebSettings;
    private String myLastFmUsername;
    private byte[] myLastFmPasswordHash;
    private LastFmSession myLastFmSession;
    private int myLastFmHardFailureCount;
    private long myLastFmHandshakeTime;
    private long myLastFmHandshakeWaitTime;
    private boolean myUrlEncryption;
    private String myEmail;
    private boolean myChangeEmail;
    private boolean myRemoteControl;
    private User myParent;
    private boolean myExternalSites;
    private int mySearchFuzziness;
    private boolean myEditTags;
    private Set<String> myForceTranscoders = new HashSet<String>();

    public User(String name) {
        myName = name;
        myResetTime = System.currentTimeMillis();
        myQuotaResetTime = myResetTime;
        myQuotaType = QuotaType.None;
        mySearchFuzziness = -1;
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

    public String getHexEncodedPasswordHash() {
        return new String(Hex.encodeHex(getPasswordHash()));
    }

    public void setPasswordHash(byte[] passwordHash) {
        myPasswordHash = passwordHash;
    }

    public boolean isDownload() {
        return getParent() != null ? getParent().isDownload() : myDownload;
    }

    public void setDownload(boolean download) {
        myDownload = download;
    }

    public boolean isPlaylist() {
        return getParent() != null ? getParent().isPlaylist() : myPlaylist;
    }

    public void setPlaylist(boolean playlist) {
        myPlaylist = playlist;
    }

    public boolean isRss() {
        return getParent() != null ? getParent().isRss() : myRss;
    }

    public void setRss(boolean rss) {
        myRss = rss;
    }

    public boolean isUpload() {
        return getParent() != null ? getParent().isUpload() : myUpload;
    }

    public void setUpload(boolean upload) {
        myUpload = upload;
    }

    public boolean isPlayer() {
        return getParent() != null ? getParent().isPlayer() : myPlayer;
    }

    public void setPlayer(boolean player) {
        myPlayer = player;
    }

    public boolean isChangePassword() {
        return getParent() != null ? getParent().isChangePassword() : myChangePassword;
    }

    public void setChangePassword(boolean changePassword) {
        myChangePassword = changePassword;
    }

    public boolean isEditLastFmAccount() {
        return getParent() != null ? getParent().isEditLastFmAccount() : myEditLastFmAccount;
    }

    public void setEditLastFmAccount(boolean editLastFmAccount) {
        myEditLastFmAccount = editLastFmAccount;
    }

    public long getBytesQuota() {
        return getParent() != null ? getParent().getBytesQuota() : myBytesQuota;
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
        return getParent() != null ? getParent().getQuotaType() : (myQuotaType != null ? myQuotaType : QuotaType.None);
    }

    public void setQuotaType(QuotaType quotaType) {
        myQuotaType = quotaType;
    }

    public boolean isQuota() {
        return getParent() != null ? getParent().isQuota() : (myQuotaType != null && myQuotaType != QuotaType.None);
    }

    public long getResetTime() {
        return myResetTime;
    }

    public void setResetTime(long resetTime) {
        myResetTime = resetTime;
    }

    public int getMaximumZipEntries() {
        return getParent() != null ? getParent().getMaximumZipEntries() : myMaximumZipEntries;
    }

    public void setMaximumZipEntries(int maximumZipEntries) {
        myMaximumZipEntries = maximumZipEntries;
    }

    public String getFileTypes() {
        return getParent() != null ? getParent().getFileTypes() : myFileTypes;
    }

    public void setFileTypes(String fileTypes) {
        myFileTypes = fileTypes;
    }

    public int getSessionTimeout() {
        return getParent() != null ? getParent().getSessionTimeout() : mySessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        mySessionTimeout = sessionTimeout;
    }

    public boolean isSpecialPlaylists() {
        return getParent() != null ? getParent().isSpecialPlaylists() : mySpecialPlaylists;
    }

    public void setSpecialPlaylists(boolean specialPlaylists) {
        mySpecialPlaylists = specialPlaylists;
    }

    public boolean isTranscoder() {
        return getParent() != null ? getParent().isTranscoder() : myTranscoder;
    }

    public void setTranscoder(boolean transcoder) {
        myTranscoder = transcoder;
    }

    public int getBandwidthLimit() {
        return getParent() != null ? getParent().getBandwidthLimit() : myBandwidthLimit;
    }

    public void setBandwidthLimit(int bandwidthLimit) {
        myBandwidthLimit = bandwidthLimit;
    }

    public List<String> getPlaylistIds() {
        return getParent() != null ? getParent().getPlaylistIds() : new ArrayList<String>(myPlaylistIds);
    }

    public void addPlaylistId(String playlistId) {
        myPlaylistIds.add(playlistId);
    }

    public void removePlaylistId(String playlistId) {
        myPlaylistIds.remove(playlistId);
    }

    public void setPlaylistIds(Set<String> playlistIds) {
        myPlaylistIds = new HashSet<String>(playlistIds);
    }

    public boolean isSaveWebSettings() {
        return getParent() != null ? getParent().isSaveWebSettings() : mySaveWebSettings;
    }

    public void setSaveWebSettings(boolean saveWebSettings) {
        mySaveWebSettings = saveWebSettings;
    }

    public boolean isCreatePlaylists() {
        return getParent() != null ? getParent().isCreatePlaylists() : myCreatePlaylists;
    }

    public void setCreatePlaylists(boolean createPlaylists) {
        myCreatePlaylists = createPlaylists;
    }

    public boolean isEditWebSettings() {
        return getParent() != null ? getParent().isEditWebSettings() : myEditWebSettings;
    }

    public void setEditWebSettings(boolean editWebSettings) {
        myEditWebSettings = editWebSettings;
    }

    public String getLastFmUsername() {
        return myLastFmUsername;
    }

    public void setLastFmUsername(String lastFmUsername) {
        myLastFmUsername = lastFmUsername;
    }

    public byte[] getLastFmPasswordHash() {
        return myLastFmPasswordHash;
    }

    public void setLastFmPasswordHash(byte[] lastFmPasswordHash) {
        myLastFmPasswordHash = lastFmPasswordHash;
    }

    public boolean isLastFmAccount() {
        return StringUtils.isNotEmpty(getLastFmUsername()) && getLastFmPasswordHash() != null && getLastFmPasswordHash().length > 0;
    }

    public boolean isUrlEncryption() {
        return getParent() != null ? getParent().isUrlEncryption() : myUrlEncryption;
    }

    public void setUrlEncryption(boolean urlEncryption) {
        myUrlEncryption = urlEncryption;
    }

    public String getEmail() {
        return myEmail;
    }

    public void setEmail(String email) {
        myEmail = email;
    }

    public boolean isChangeEmail() {
        return getParent() != null ? getParent().isChangeEmail() : myChangeEmail;
    }

    public void setChangeEmail(boolean changeEmail) {
        myChangeEmail = changeEmail;
    }

    public boolean isRemoteControl() {
        return getParent() != null ? getParent().isRemoteControl() : myRemoteControl;
    }

    public void setRemoteControl(boolean remoteControl) {
        myRemoteControl = remoteControl;
    }

    public boolean isExternalSites() {
        return getParent() != null ? getParent().isExternalSites() : myExternalSites;
    }

    public void setExternalSites(boolean externalSites) {
        myExternalSites = externalSites;
    }

    public int getSearchFuzziness() {
        return getParent() != null ? getParent().getSearchFuzziness() : mySearchFuzziness;
    }

    public void setSearchFuzziness(int searchFuzziness) {
        mySearchFuzziness = searchFuzziness;
    }

    public boolean isEditTags() {
        return getParent() != null ? getParent().isEditTags() : myEditTags;
    }

    public void setEditTags(boolean editTags) {
        myEditTags = editTags;
    }

    public Set<String> getForceTranscoders() {
        return new HashSet<String>(myForceTranscoders);
    }

    public void clearForceTranscoders() {
        myForceTranscoders.clear();
    }

    public void addForceTranscoder(String transcoder) {
        myForceTranscoders.add(transcoder);
    }

    public void removeForceTranscoder(String transcoder) {
        myForceTranscoders.remove(transcoder);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof User && getName().equalsIgnoreCase(((User) object).getName());
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().toLowerCase(Locale.ENGLISH).hashCode() : 0;
    }

    public boolean isQuotaExceeded() {
        if (myQuotaType != null && myQuotaType != QuotaType.None && myBytesQuota > 0) {
            checkQuotaReset();
            return myBytesQuota > 0 && myQuotaDownBytes >= myBytesQuota;
        }
        return false;
    }

    public long getQuotaRemaining() {
        if (myQuotaType != null && myQuotaType != QuotaType.None && myBytesQuota > 0) {
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
            return new MyTunesRSSOutputStreamWrapper(bitrate, dataOffset, rangeHeader.getRangeFrom(), 2);
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

    public void loadFromPreferences(JXPathContext settings) {
        setActive(JXPathUtils.getBooleanValue(settings, "active", true));
        setPasswordHash(JXPathUtils.getByteArray(settings, "password", null));
        setRss(JXPathUtils.getBooleanValue(settings, "featureRss", false));
        setPlaylist(JXPathUtils.getBooleanValue(settings, "featurePlaylist", false));
        setDownload(JXPathUtils.getBooleanValue(settings, "featureDownload", false));
        setUpload(JXPathUtils.getBooleanValue(settings, "featureUpload", false));
        setPlayer(JXPathUtils.getBooleanValue(settings, "featurePlayer", false));
        setChangePassword(JXPathUtils.getBooleanValue(settings, "featureChangePassword", false));
        setEditLastFmAccount(JXPathUtils.getBooleanValue(settings, "featureLastFmAccount", true));
        setSpecialPlaylists(JXPathUtils.getBooleanValue(settings, "featureSpecialPlaylists", false));
        setCreatePlaylists(JXPathUtils.getBooleanValue(settings, "featureCreatePlaylists", false));
        setEditWebSettings(JXPathUtils.getBooleanValue(settings, "featureEditWebSettings", false));
        setResetTime(JXPathUtils.getLongValue(settings, "resetTime", System.currentTimeMillis()));
        setQuotaResetTime(JXPathUtils.getLongValue(settings, "quotaResetTime", System.currentTimeMillis()));
        setDownBytes(JXPathUtils.getLongValue(settings, "downBytes", 0));
        setQuotaDownBytes(JXPathUtils.getLongValue(settings, "quotaDownBytes", 0));
        setBytesQuota(JXPathUtils.getLongValue(settings, "bytesQuota", 0));
        setQuotaType(QuotaType.valueOf(JXPathUtils.getStringValue(settings, "quotaType", QuotaType.None.name())));
        setMaximumZipEntries(JXPathUtils.getIntValue(settings, "maximumZipEntries", 0));
        setFileTypes(JXPathUtils.getStringValue(settings, "fileTypes", null));
        setTranscoder(JXPathUtils.getBooleanValue(settings, "featureTranscoder", false));
        setSessionTimeout(JXPathUtils.getIntValue(settings, "sessionTimeout", 10));
        setBandwidthLimit(JXPathUtils.getIntValue(settings, "bandwidthLimit", 0));
        Iterator<JXPathContext> playlistIdIterator = JXPathUtils.getContextIterator(settings, "playlists/id");
        while (playlistIdIterator.hasNext()) {
            addPlaylistId(JXPathUtils.getStringValue(playlistIdIterator.next(), ".", null));
        }
        setSaveWebSettings(JXPathUtils.getBooleanValue(settings, "saveWebSettings", false));
        setWebSettings(JXPathUtils.getStringValue(settings, "webSettings", null));
        setLastFmUsername(JXPathUtils.getStringValue(settings, "lastFmUser", null));
        setLastFmPasswordHash(JXPathUtils.getByteArray(settings, "lastFmPassword", null));
        setUrlEncryption(JXPathUtils.getBooleanValue(settings, "urlEncryption", true));
        setEmail(JXPathUtils.getStringValue(settings, "email", null));
        setRemoteControl(JXPathUtils.getBooleanValue(settings, "remoteControl", false));
        myParent = new UserProxy(JXPathUtils.getStringValue(settings, "parent", null));
        setExternalSites(JXPathUtils.getBooleanValue(settings, "externalSites", false));
        setChangeEmail(JXPathUtils.getBooleanValue(settings, "changeEmail", false));
        setSearchFuzziness(JXPathUtils.getIntValue(settings, "searchFuzziness", -1));
        setEditTags(JXPathUtils.getBooleanValue(settings, "editTags", false));
        Set<String> availableTranscoders = new HashSet<String>();
        for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            availableTranscoders.add(config.getName());
        }
        Iterator<JXPathContext> forceTranscoderIterator = JXPathUtils.getContextIterator(settings, "forcetranscoder/name");
        while (forceTranscoderIterator.hasNext()) {
            String name = JXPathUtils.getStringValue(forceTranscoderIterator.next(), ".", null);
            if (availableTranscoders.contains(name)) {
                myForceTranscoders.add(name);
            }
        }
        //        try {
        //            setLastFmPasswordHash(MyTunesRss.REGISTRATION.isRegistered() ? MyTunesRss.MD5_DIGEST.digest(JXPathUtils.getStringValue(settings, "lastFmPassword", "").getBytes("UTF-8")) : null);
        //        } catch (Exception e) {
        //                LOG.error("Could not create password hash for last fm user.", e);
        //        }
    }

    public void saveToPreferences(Document settings, Element users) {
        users.appendChild(DOMUtils.createTextElement(settings, "name", getName()));
        if (getPasswordHash() != null && getPasswordHash().length > 0) {
            users.appendChild(DOMUtils.createByteArrayElement(settings, "password", getPasswordHash()));
        }
        users.appendChild(DOMUtils.createBooleanElement(settings, "active", isActive()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureRss", isRss()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featurePlaylist", isPlaylist()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureDownload", isDownload()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureUpload", isUpload()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featurePlayer", isPlayer()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureChangePassword", isChangePassword()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureLastFmAccount", isEditLastFmAccount()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureSpecialPlaylists", isSpecialPlaylists()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureCreatePlaylists", isCreatePlaylists()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureEditWebSettings", isEditWebSettings()));
        users.appendChild(DOMUtils.createLongElement(settings, "resetTime", getResetTime()));
        users.appendChild(DOMUtils.createLongElement(settings, "quotaResetTime", getQuotaResetTime()));
        users.appendChild(DOMUtils.createLongElement(settings, "downBytes", getDownBytes()));
        users.appendChild(DOMUtils.createLongElement(settings, "quotaDownBytes", getQuotaDownBytes()));
        users.appendChild(DOMUtils.createLongElement(settings, "bytesQuota", getBytesQuota()));
        users.appendChild(DOMUtils.createTextElement(settings, "quotaType", getQuotaType().name()));
        users.appendChild(DOMUtils.createIntElement(settings, "maximumZipEntries", getMaximumZipEntries()));
        users.appendChild(DOMUtils.createTextElement(settings, "fileTypes", getFileTypes() != null ? getFileTypes() : ""));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureTranscoder", isTranscoder()));
        users.appendChild(DOMUtils.createIntElement(settings, "sessionTimeout", getSessionTimeout()));
        users.appendChild(DOMUtils.createIntElement(settings, "bandwidthLimit", getBandwidthLimit()));
        users.appendChild(DOMUtils.createTextElement(settings, "email", getEmail()));
        if (!CollectionUtils.isEmpty(getPlaylistIds())) {
            Element playlists = settings.createElement("playlists");
            users.appendChild(playlists);
            for (String playlistId : getPlaylistIds()) {
                playlists.appendChild(DOMUtils.createTextElement(settings, "id", playlistId));
            }
        }
        users.appendChild(DOMUtils.createBooleanElement(settings, "saveWebSettings", isSaveWebSettings()));
        if (StringUtils.isNotEmpty(getWebSettings())) {
            users.appendChild(DOMUtils.createTextElement(settings, "webSettings", getWebSettings()));
        }
        if (StringUtils.isNotEmpty(getLastFmUsername()) && getLastFmPasswordHash() != null && getLastFmPasswordHash().length > 0) {
            users.appendChild(DOMUtils.createTextElement(settings, "lastFmUser", getLastFmUsername()));
            users.appendChild(DOMUtils.createByteArrayElement(settings, "lastFmPassword", getLastFmPasswordHash()));
        }
        users.appendChild(DOMUtils.createBooleanElement(settings, "urlEncryption", isUrlEncryption()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "remoteControl", isRemoteControl()));
        users.appendChild(DOMUtils.createTextElement(settings, "parent", getParent() != null ? getParent().getName() : null));
        users.appendChild(DOMUtils.createBooleanElement(settings, "externalSites", isExternalSites()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "changeEmail", isChangeEmail()));
        users.appendChild(DOMUtils.createIntElement(settings, "searchFuzziness", getSearchFuzziness()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "editTags", isEditTags()));
        if (myForceTranscoders != null && myForceTranscoders.size() > 0) {
            Element forceTranscoders = settings.createElement("forcetranscoder");
            users.appendChild(forceTranscoders);
            for (String transcoder : myForceTranscoders) {
                forceTranscoders.appendChild(DOMUtils.createTextElement(settings, "name", transcoder));
            }
        }
    }

    public synchronized void playLastFmTrack(final Track track) {
        if (isLastFmAccount()) {
            final long playTime = System.currentTimeMillis();
            Runnable runnable = new Runnable() {
                public void run() {
                    if (myLastFmSession == null && myLastFmHandshakeTime + myLastFmHandshakeWaitTime < System.currentTimeMillis()) {
                        LOG.debug("Trying to create a new LastFM session for user \"" + getName() + "\".");
                        myLastFmSession = LastFmUtils.doHandshake(User.this);
                        myLastFmHandshakeTime = System.currentTimeMillis();
                        if (myLastFmSession != null) {
                            LOG.debug("Got LastFM session, adding user as event listener.");
                            MyTunesRssEventManager.getInstance().addListener(User.this);
                        } else {
                            myLastFmHandshakeWaitTime = Math.min(myLastFmHandshakeWaitTime == 0 ? 60000 : myLastFmHandshakeWaitTime * 2, 7200000);
                            LOG.warn("No LastFM session for user \"" + getName() + "\", setting handshake wait time to " +
                                    (myLastFmHandshakeWaitTime / 1000) + " seconds.");
                        }
                    }
                    if (myLastFmSession != null) {
                        if (LastFmUtils.sendSubmissions(myLastFmSession) && LastFmUtils.sendNowPlaying(myLastFmSession, track)) {
                            if (myLastFmSession != null) {
                                myLastFmSession.offerSubmission(new LastFmSubmission(track, playTime));
                            }
                        } else {
                            myLastFmHardFailureCount++;
                            LOG.warn("Hard LastFM failure (count = " + myLastFmHardFailureCount + ").");
                            if (myLastFmHardFailureCount == 3) {
                                LOG.debug("Critical LastFM failure count reached, destroying session and removing event listener.");
                                myLastFmHardFailureCount = 0;
                                myLastFmSession = null;
                                MyTunesRssEventManager.getInstance().removeListener(User.this);
                            }
                        }
                    }
                }
            };
            new Thread(runnable).start();
        }
    }

    public User getParent() {
        if (myParent instanceof UserProxy) {
            myParent = ((UserProxy) myParent).resolveUser();
        }
        return myParent;
    }

    public void setParent(User parent) {
        myParent = parent;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning user \"" + myName + "\" failed.", e);

        }
    }

    public TranscoderConfig getTranscoder(Track track) {
        for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            if (isActiveTranscoder(config.getName()) && config.isValidFor(track.getFilename(), track.getMp4Codec())) {
                return config;
            }
        }
        return null;
    }

    public boolean isActiveTranscoder(String name) {
        return myForceTranscoders != null && myForceTranscoders.contains(name);
    }
}
