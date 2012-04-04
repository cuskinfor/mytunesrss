package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.UserAgent;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventListener;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.lastfm.LastFmSession;
import de.codewave.mytunesrss.lastfm.LastFmSubmission;
import de.codewave.mytunesrss.lastfm.LastFmUtils;
import de.codewave.utils.MiscUtils;
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

import java.util.*;

/**
 * de.codewave.mytunesrss.config.User
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

    public void retainPlaylists(Set<String> ids) {
        myRestrictedPlaylistIds.retainAll(ids);
        myExcludedPlaylistIds.retainAll(ids);
        myHiddenPlaylistIds.retainAll(ids);
    }

    public void retainPhotoAlbums(Set<String> ids) {
        myRestrictedPhotoAlbumIds.retainAll(ids);
        myExcludedPhotoAlbumIds.retainAll(ids);
    }

    public enum QuotaType {
        None, Day, Week, Month;

        @Override
        public String toString() {
            return MyTunesRssUtils.getBundleString(Locale.getDefault(), "editUser.quotaType." + name());
        }
    }

    private String myName;
    private byte[] myPasswordHash = MyTunesRss.SHA1_DIGEST.digest(MiscUtils.getUtf8Bytes(UUID.randomUUID().toString()));
    private boolean myEmptyPassword = true;
    private boolean myDownload = true;
    private boolean myRss = true;
    private boolean myPlaylist = true;
    private boolean myUpload = true;
    private boolean myPlayer = true;
    private boolean myChangePassword = true;
    private boolean myEditLastFmAccount = true;
    private QuotaType myQuotaType = QuotaType.None;
    private long myDownBytes;
    private long myQuotaDownBytes;
    private long myBytesQuota;
    private long myResetTime = System.currentTimeMillis();
    private long myQuotaResetTime = System.currentTimeMillis();
    private int myMaximumZipEntries;
    private int mySessionTimeout = 20;
    private boolean mySpecialPlaylists = true;
    private boolean myTranscoder = true;
    private Set<String> myRestrictedPlaylistIds = new HashSet<String>();
    private Set<String> myExcludedPlaylistIds = new HashSet<String>();
    private Set<String> myHiddenPlaylistIds = new HashSet<String>();
    private Set<String> myRestrictedPhotoAlbumIds = new HashSet<String>();
    private Set<String> myExcludedPhotoAlbumIds = new HashSet<String>();
    private boolean mySharedUser;
    private Map<String, String> myWebConfigs = new HashMap<String, String>();
    private boolean myCreatePlaylists = true;
    private boolean myEditWebSettings = true;
    private String myLastFmUsername;
    private byte[] myLastFmPasswordHash;
    private LastFmSession myLastFmSession;
    private int myLastFmHardFailureCount;
    private long myLastFmHandshakeTime;
    private long myLastFmHandshakeWaitTime;
    private String myEmail;
    private boolean myChangeEmail = true;
    private boolean myRemoteControl = true;
    private User myParent;
    private boolean myExternalSites = true;
    private int mySearchFuzziness = -1;
    private boolean myEditTags = true;
    private Set<String> myForceTranscoders = new HashSet<String>();
    private long myExpiration;
    private boolean myYahooPlayer = true;
    private boolean myGroup = false;
    private boolean myCreatePublicPlaylists = false;
    private boolean myPhotos = true;
    private boolean myShare = true;
    private boolean myDownloadPhotoAlbum = true;
    private boolean myAudio = true;
    private boolean myMovies = true;
    private boolean myTvShows = true;

    public User(String name) {
        myName = name;
    }

    public boolean isActive() {
        return myExpiration == 0 || myExpiration >= System.currentTimeMillis();
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

    public boolean isEmptyPassword() {
        return myEmptyPassword;
    }

    public void setEmptyPassword(boolean emptyPassword) {
        myEmptyPassword = emptyPassword;
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

    public List<String> getRestrictedPlaylistIds() {
        return getParent() != null ? getParent().getRestrictedPlaylistIds() : new ArrayList<String>(myRestrictedPlaylistIds);
    }

    public void addRestrictedPlaylistId(String playlistId) {
        myRestrictedPlaylistIds.add(playlistId);
    }

    public void removeRestrictedPlaylistId(String playlistId) {
        myRestrictedPlaylistIds.remove(playlistId);
    }

    public void setRestrictedPlaylistIds(Set<String> playlistIds) {
        myRestrictedPlaylistIds = new HashSet<String>(playlistIds);
    }

    public List<String> getExcludedPlaylistIds() {
        return getParent() != null ? getParent().getExcludedPlaylistIds() : new ArrayList<String>(myExcludedPlaylistIds);
    }

    public void addExcludedPlaylistId(String playlistId) {
        myExcludedPlaylistIds.add(playlistId);
    }

    public void removeExcludedPlaylistId(String playlistId) {
        myExcludedPlaylistIds.remove(playlistId);
    }

    public void setExcludedPlaylistIds(Set<String> playlistIds) {
        myExcludedPlaylistIds = new HashSet<String>(playlistIds);
    }

    public List<String> getHiddenPlaylistIds() {
        return getParent() != null ? getParent().getHiddenPlaylistIds() : new ArrayList<String>(myHiddenPlaylistIds);
    }

    public void addHiddenPlaylistId(String playlistId) {
        myHiddenPlaylistIds.add(playlistId);
    }

    public void removeHiddenPlaylistId(String playlistId) {
        myHiddenPlaylistIds.remove(playlistId);
    }

    public void setHiddenPlaylistIds(Set<String> playlistIds) {
        myHiddenPlaylistIds = new HashSet<String>(playlistIds);
    }

    public List<String> getRestrictedPhotoAlbumIds() {
        return getParent() != null ? getParent().getRestrictedPhotoAlbumIds() : new ArrayList<String>(myRestrictedPhotoAlbumIds);
    }

    public void addRestrictedPhotoAlbumId(String photoAlbumId) {
        myRestrictedPhotoAlbumIds.add(photoAlbumId);
    }

    public void removeRestrictedPhotoAlbumId(String photoAlbumId) {
        myRestrictedPhotoAlbumIds.remove(photoAlbumId);
    }

    public void setRestrictedPhotoAlbumIds(Set<String> photoAlbumIds) {
        myRestrictedPhotoAlbumIds = new HashSet<String>(photoAlbumIds);
    }

    public List<String> getExcludedPhotoAlbumIds() {
        return getParent() != null ? getParent().getExcludedPhotoAlbumIds() : new ArrayList<String>(myExcludedPhotoAlbumIds);
    }

    public void addExcludedPhotoAlbumId(String photoAlbumId) {
        myExcludedPhotoAlbumIds.add(photoAlbumId);
    }

    public void removeExcludedPhotoAlbumId(String photoAlbumId) {
        myExcludedPhotoAlbumIds.remove(photoAlbumId);
    }

    public void setExcludedPhotoAlbumIds(Set<String> photoAlbumIds) {
        myExcludedPhotoAlbumIds = new HashSet<String>(photoAlbumIds);
    }

    public boolean isSharedUser() {
        return getParent() != null ? getParent().isSharedUser() : mySharedUser;
    }

    public void setSharedUser(boolean sharedUser) {
        mySharedUser = sharedUser;
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
        return getParent() != null ? getParent().getForceTranscoders() : new HashSet<String>(myForceTranscoders);
    }

    public void clearForceTranscoders() {
        myForceTranscoders.clear();
    }

    public void addForceTranscoder(String transcoder) {
        myForceTranscoders.add(transcoder);
    }

    public long getExpiration() {
        return myExpiration;
    }

    public void setExpiration(long expiration) {
        myExpiration = expiration;
    }

    public boolean isYahooPlayer() {
        return myYahooPlayer;
    }

    public void setYahooPlayer(boolean yahooPlayer) {
        myYahooPlayer = yahooPlayer;
    }

    public boolean isGroup() {
        return myGroup;
    }

    public void setGroup(boolean group) {
        myGroup = group;
    }

    public boolean isCreatePublicPlaylists() {
        return myCreatePublicPlaylists;
    }

    public void setCreatePublicPlaylists(boolean createPublicPlaylists) {
        myCreatePublicPlaylists = createPublicPlaylists;
    }

    public boolean isPhotos() {
        return myPhotos;
    }

    public void setPhotos(boolean photos) {
        myPhotos = photos;
    }

    public boolean isShare() {
        return myShare;
    }

    public void setShare(boolean share) {
        myShare = share;
    }

    public boolean isDownloadPhotoAlbum() {
        return myDownloadPhotoAlbum;
    }

    public void setDownloadPhotoAlbum(boolean downloadPhotoAlbum) {
        myDownloadPhotoAlbum = downloadPhotoAlbum;
    }

    public boolean isAudio() {
        return myAudio;
    }

    public void setAudio(boolean audio) {
        myAudio = audio;
    }

    public boolean isMovies() {
        return myMovies;
    }

    public void setMovies(boolean movies) {
        myMovies = movies;
    }

    public boolean isTvShows() {
        return myTvShows;
    }

    public void setTvShows(boolean tvShows) {
        myTvShows = tvShows;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof User && getName().toLowerCase(Locale.ENGLISH).equals(((User) object).getName().toLowerCase(Locale.ENGLISH));
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

    public String getWebConfig(UserAgent userAgent) {
        return myWebConfigs.get(userAgent.toConfigKey());
    }

    public void setWebConfig(UserAgent userAgent, String webConfig) {
        if (webConfig == null) {
            myWebConfigs.remove(userAgent.toConfigKey());
        } else {
            myWebConfigs.put(userAgent.toConfigKey(), webConfig);
        }
    }

    public void loadFromPreferences(JXPathContext settings) {
        setPasswordHash(JXPathUtils.getByteArray(settings, "password", null));
        setEmptyPassword(JXPathUtils.getBooleanValue(settings, "emptyPassword", false));
        setRss(JXPathUtils.getBooleanValue(settings, "featureRss", myRss));
        setPlaylist(JXPathUtils.getBooleanValue(settings, "featurePlaylist", myPlaylist));
        setDownload(JXPathUtils.getBooleanValue(settings, "featureDownload", myDownload));
        setUpload(JXPathUtils.getBooleanValue(settings, "featureUpload", myUpload));
        setPlayer(JXPathUtils.getBooleanValue(settings, "featurePlayer", myPlayer));
        setChangePassword(JXPathUtils.getBooleanValue(settings, "featureChangePassword", myChangePassword));
        setEditLastFmAccount(JXPathUtils.getBooleanValue(settings, "featureLastFmAccount", myEditLastFmAccount));
        setSpecialPlaylists(JXPathUtils.getBooleanValue(settings, "featureSpecialPlaylists", mySpecialPlaylists));
        setCreatePlaylists(JXPathUtils.getBooleanValue(settings, "featureCreatePlaylists", myCreatePlaylists));
        setEditWebSettings(JXPathUtils.getBooleanValue(settings, "featureEditWebSettings", myEditWebSettings));
        setResetTime(JXPathUtils.getLongValue(settings, "resetTime", System.currentTimeMillis()));
        setQuotaResetTime(JXPathUtils.getLongValue(settings, "quotaResetTime", System.currentTimeMillis()));
        setDownBytes(JXPathUtils.getLongValue(settings, "downBytes", myDownBytes));
        setQuotaDownBytes(JXPathUtils.getLongValue(settings, "quotaDownBytes", myQuotaDownBytes));
        setBytesQuota(JXPathUtils.getLongValue(settings, "bytesQuota", myBytesQuota));
        setQuotaType(QuotaType.valueOf(JXPathUtils.getStringValue(settings, "quotaType", myQuotaType.name())));
        setMaximumZipEntries(JXPathUtils.getIntValue(settings, "maximumZipEntries", myMaximumZipEntries));
        setTranscoder(JXPathUtils.getBooleanValue(settings, "featureTranscoder", myTranscoder));
        setSessionTimeout(JXPathUtils.getIntValue(settings, "sessionTimeout", mySessionTimeout));
        Iterator<JXPathContext> playlistIdIterator = JXPathUtils.getContextIterator(settings, "playlists/restricted");
        while (playlistIdIterator.hasNext()) {
            addRestrictedPlaylistId(JXPathUtils.getStringValue(playlistIdIterator.next(), ".", null));
        }
        playlistIdIterator = JXPathUtils.getContextIterator(settings, "playlists/excluded");
        while (playlistIdIterator.hasNext()) {
            addExcludedPlaylistId(JXPathUtils.getStringValue(playlistIdIterator.next(), ".", null));
        }
        playlistIdIterator = JXPathUtils.getContextIterator(settings, "playlists/hidden");
        while (playlistIdIterator.hasNext()) {
            addHiddenPlaylistId(JXPathUtils.getStringValue(playlistIdIterator.next(), ".", null));
        }
        Iterator<JXPathContext> photoAlbumIdIterator = JXPathUtils.getContextIterator(settings, "photoalbums/restricted");
        while (photoAlbumIdIterator.hasNext()) {
            addRestrictedPhotoAlbumId(JXPathUtils.getStringValue(photoAlbumIdIterator.next(), ".", null));
        }
        photoAlbumIdIterator = JXPathUtils.getContextIterator(settings, "photoalbums/excluded");
        while (photoAlbumIdIterator.hasNext()) {
            addExcludedPhotoAlbumId(JXPathUtils.getStringValue(photoAlbumIdIterator.next(), ".", null));
        }
        setSharedUser(JXPathUtils.getBooleanValue(settings, "shared", false));
        Iterator<JXPathContext> webConfigIterator = JXPathUtils.getContextIterator(settings, "webConfigs/config");
        myWebConfigs.clear();
        while (webConfigIterator.hasNext()) {
            JXPathContext webConfigContext = webConfigIterator.next();
            myWebConfigs.put(JXPathUtils.getStringValue(webConfigContext, "userAgent", UserAgent.Unknown.toConfigKey()), JXPathUtils.getStringValue(webConfigContext, "config", null));
        }
        setLastFmUsername(JXPathUtils.getStringValue(settings, "lastFmUser", myLastFmUsername));
        setLastFmPasswordHash(JXPathUtils.getByteArray(settings, "lastFmPassword", myLastFmPasswordHash));
        setEmail(JXPathUtils.getStringValue(settings, "email", myEmail));
        setRemoteControl(JXPathUtils.getBooleanValue(settings, "remoteControl", myRemoteControl));
        myParent = new UserProxy(JXPathUtils.getStringValue(settings, "parent", null));
        setExternalSites(JXPathUtils.getBooleanValue(settings, "externalSites", myExternalSites));
        setChangeEmail(JXPathUtils.getBooleanValue(settings, "changeEmail", myChangeEmail));
        setSearchFuzziness(JXPathUtils.getIntValue(settings, "searchFuzziness", mySearchFuzziness));
        setEditTags(JXPathUtils.getBooleanValue(settings, "editTags", myEditTags));
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
        setExpiration(JXPathUtils.getLongValue(settings, "expiration", myExpiration));
        setYahooPlayer(JXPathUtils.getBooleanValue(settings, "yahooPlayer", myYahooPlayer));
        setGroup(JXPathUtils.getBooleanValue(settings, "group", myGroup));
        setCreatePublicPlaylists(JXPathUtils.getBooleanValue(settings, "createPublicPlaylists", myCreatePublicPlaylists));
        setPhotos(JXPathUtils.getBooleanValue(settings, "featurePhotos", myPhotos));
        setShare(JXPathUtils.getBooleanValue(settings, "featureShare", myShare));
        setDownloadPhotoAlbum(JXPathUtils.getBooleanValue(settings, "featureDownloadPhotoAlbum", myDownloadPhotoAlbum));
        setAudio(JXPathUtils.getBooleanValue(settings, "featureAudio", myAudio));
        setMovies(JXPathUtils.getBooleanValue(settings, "featureMovies", myMovies));
        setTvShows(JXPathUtils.getBooleanValue(settings, "featureTvShows", myTvShows));
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
        users.appendChild(DOMUtils.createBooleanElement(settings, "emptyPassword", isEmptyPassword()));
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
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureTranscoder", isTranscoder()));
        users.appendChild(DOMUtils.createIntElement(settings, "sessionTimeout", getSessionTimeout()));
        users.appendChild(DOMUtils.createTextElement(settings, "email", getEmail()));
        if (!CollectionUtils.isEmpty(getRestrictedPlaylistIds()) || !CollectionUtils.isEmpty(getExcludedPlaylistIds()) || !CollectionUtils.isEmpty(getHiddenPlaylistIds())) {
            Element playlists = settings.createElement("playlists");
            users.appendChild(playlists);
            for (String playlistId : getRestrictedPlaylistIds()) {
                playlists.appendChild(DOMUtils.createTextElement(settings, "restricted", playlistId));
            }
            for (String playlistId : getExcludedPlaylistIds()) {
                playlists.appendChild(DOMUtils.createTextElement(settings, "excluded", playlistId));
            }
            for (String playlistId : getHiddenPlaylistIds()) {
                playlists.appendChild(DOMUtils.createTextElement(settings, "hidden", playlistId));
            }
        }
        if (!CollectionUtils.isEmpty(getRestrictedPhotoAlbumIds()) || !CollectionUtils.isEmpty(getExcludedPhotoAlbumIds())) {
            Element photoalbums = settings.createElement("photoalbums");
            users.appendChild(photoalbums);
            for (String photoAlbumId : getRestrictedPhotoAlbumIds()) {
                photoalbums.appendChild(DOMUtils.createTextElement(settings, "restricted", photoAlbumId));
            }
            for (String photoAlbumId : getExcludedPhotoAlbumIds()) {
                photoalbums.appendChild(DOMUtils.createTextElement(settings, "excluded", photoAlbumId));
            }
        }
        users.appendChild(DOMUtils.createBooleanElement(settings, "shared", isSharedUser()));
        Element webConfigs = settings.createElement("webConfigs");
        users.appendChild(webConfigs);
        for (Map.Entry<String, String> webConfig : myWebConfigs.entrySet()) {
            if (webConfig.getValue() != null) {
                Element config = settings.createElement("config");
                webConfigs.appendChild(config);
                config.appendChild(DOMUtils.createTextElement(settings, "userAgent", webConfig.getKey()));
                config.appendChild(DOMUtils.createTextElement(settings, "config", webConfig.getValue()));
            }
        }
        if (StringUtils.isNotEmpty(getLastFmUsername()) && getLastFmPasswordHash() != null && getLastFmPasswordHash().length > 0) {
            users.appendChild(DOMUtils.createTextElement(settings, "lastFmUser", getLastFmUsername()));
            users.appendChild(DOMUtils.createByteArrayElement(settings, "lastFmPassword", getLastFmPasswordHash()));
        }
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
        if (getExpiration() > 0) {
            users.appendChild(DOMUtils.createLongElement(settings, "expiration", getExpiration()));
        }
        users.appendChild(DOMUtils.createBooleanElement(settings, "yahooPlayer", isYahooPlayer()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "group", isGroup()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "createPublicPlaylists", isCreatePublicPlaylists()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featurePhotos", isPhotos()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureShare", isShare()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureDownloadPhotoAlbum", isDownloadPhotoAlbum()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureAudio", isAudio()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureMovies", isMovies()));
        users.appendChild(DOMUtils.createBooleanElement(settings, "featureTvShows", isTvShows()));
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
            User clone = (User)super.clone();
            clone.setDownBytes(0);
            clone.setQuotaDownBytes(0);
            clone.setResetTime(0);
            clone.setQuotaResetTime(0);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning user \"" + myName + "\" failed.", e);

        }
    }

    public TranscoderConfig getForceTranscoder(Track track) {
        if (isForceTranscoders()) {
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                if (getForceTranscoders().contains(config.getName()) && config.isValidFor(track.getFilename(), track.getMp4Codec())) {
                    return config;
                }
            }
        }
        return null;
    }
}
