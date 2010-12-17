/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;
import de.codewave.utils.Version;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * de.codewave.mytunesrss.MyTunesRssConfig
 */
public class MyTunesRssConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssConfig.class);
    private static final SecretKeySpec CHECKSUM_KEY = new SecretKeySpec("codewave".getBytes(), "DES");
    private static final String CREATION_TIME_KEY = "playmode";

    private int myPort = 8080;
    private String myServerName = "MyTunesRSS";
    private boolean myAvailableOnLocalNet = true;
    private List<DatasourceConfig> myDatasources = new ArrayList<DatasourceConfig>();
    private boolean myCheckUpdateOnStart = true;
    private String myVersion;
    private boolean myIgnoreTimestamps;
    private Collection<User> myUsers = new HashSet<User>();
    private String mySupportName = "";
    private String mySupportEmail = "";
    private String myProxyHost = "";
    private int myProxyPort = -1;
    private String myUploadDir = "";
    private boolean myUploadCreateUserDir = true;
    private String myMyTunesRssComUser = "";
    private byte[] myMyTunesRssComPasswordHash = null;
    private boolean myMyTunesRssComSsl = false;
    private String myArtistDropWords = "";
    private boolean myLocalTempArchive;
    private SecretKey myPathInfoKey;
    private String myWebWelcomeMessage = "";
    private String myWebLoginMessage = "";
    private int myStreamingCacheTimeout = 20;
    private int myStreamingCacheMaxFiles = 300;
    private boolean myBandwidthLimit;
    private BigDecimal myBandwidthLimitFactor;
    private boolean myIgnoreArtwork;
    private Level myCodewaveLogLevel;
    private String myLastNewVersionInfo;
    private boolean myDeleteDatabaseOnExit;
    private String myUpdateIgnoreVersion;
    private List<String> myDatabaseCronTriggers = new ArrayList<String>();
    private String myDatabaseType;
    private String myDatabaseConnection;
    private String myDatabaseUser;
    private String myDatabasePassword;
    private String myDatabaseDriver;
    private String myWebappContext;
    private String myId3v2TrackComment;
    private String myTomcatMaxThreads;
    private int myTomcatAjpPort;
    private String mySslKeystoreFile;
    private String mySslKeystorePass;
    private int mySslPort;
    private String mySslKeystoreKeyAlias;
    private String myTomcatProxyHost;
    private int myTomcatProxyPort;
    private String myTomcatProxyScheme;
    private String myTomcatSslProxyHost;
    private int myTomcatSslProxyPort;
    private String myTomcatSslProxyScheme;
    private List<FileType> myFileTypes = new ArrayList<FileType>();
    private String myMailHost;
    private int myMailPort;
    private SmtpProtocol mySmtpProtocol;
    private String myMailLogin;
    private String myMailPassword;
    private String myMailSender;
    private String myAdminEmail;
    private boolean myNotifyOnPasswordChange;
    private boolean myNotifyOnEmailChange;
    private boolean myNotifyOnQuotaExceeded;
    private boolean myNotifyOnLoginFailure;
    private boolean myNotifyOnWebUpload;
    private boolean myNotifyOnTranscodingFailure;
    private boolean myNotifyOnInternalError;
    private boolean myNotifyOnDatabaseUpdate;
    private boolean myNotifyOnMissingFile;
    private int myStatisticKeepTime = 60;
    private String myCryptedCreationTime;
    private String myDisabledMp4Codecs;
    private List<TranscoderConfig> myTranscoderConfigs = new ArrayList<TranscoderConfig>();
    private List<ExternalSiteDefinition> myExternalSites = new ArrayList<ExternalSiteDefinition>();
    private String myAutoLogin;
    private boolean myDisableBrowser;
    private boolean myServerBrowserActive;
    private boolean myDisableWebLogin;
    private boolean myQuicktime64BitWarned;
    private LdapConfig myLdapConfig;
    private byte[] myAdminPasswordHash;
    private int myAdminPort;
    private boolean myImportOriginalImageSize = false;
    private Set<FlashPlayerConfig> myFlashPlayers = new HashSet<FlashPlayerConfig>();
    private boolean myInitialWizard;

    public List<DatasourceConfig> getDatasources() {
        return new ArrayList<DatasourceConfig>(myDatasources);
    }

    public void setDatasources(List<DatasourceConfig> datasources) {
        myDatasources = new ArrayList<DatasourceConfig>(datasources);
        Collections.sort(myDatasources);
    }

    public int getPort() {
        return myPort;
    }

    public void setPort(int port) {
        myPort = port;
    }

    public String getServerName() {
        return myServerName;
    }

    public void setServerName(String serverName) {
        if (StringUtils.isNotEmpty(serverName)) {
            myServerName = serverName;
        }
    }

    public boolean isAvailableOnLocalNet() {
        return myAvailableOnLocalNet;
    }

    public void setAvailableOnLocalNet(boolean availableOnLocalNet) {
        myAvailableOnLocalNet = availableOnLocalNet;
    }

    public boolean isCheckUpdateOnStart() {
        return myCheckUpdateOnStart;
    }

    public void setCheckUpdateOnStart(boolean checkUpdateOnStart) {
        myCheckUpdateOnStart = checkUpdateOnStart;
    }

    public String getVersion() {
        return myVersion;
    }

    public void setVersion(String version) {
        myVersion = version;
    }

    public boolean isIgnoreTimestamps() {
        return myIgnoreTimestamps;
    }

    public void setIgnoreTimestamps(boolean ignoreTimestamps) {
        myIgnoreTimestamps = ignoreTimestamps;
    }

    public boolean isUploadCreateUserDir() {
        return myUploadCreateUserDir;
    }

    public void setUploadCreateUserDir(boolean uploadCreateUserDir) {
        myUploadCreateUserDir = uploadCreateUserDir;
    }

    public String getUploadDir() {
        return myUploadDir;
    }

    public void setUploadDir(String uploadDir) {
        myUploadDir = uploadDir;
    }

    public List<FileType> getFileTypes() {
        return myFileTypes;
    }

    public String getArtistDropWords() {
        return myArtistDropWords;
    }

    public void setArtistDropWords(String artistDropWords) {
        myArtistDropWords = artistDropWords;
    }

    public boolean isLocalTempArchive() {
        return myLocalTempArchive;
    }

    public void setLocalTempArchive(boolean localTempArchive) {
        myLocalTempArchive = localTempArchive;
    }

    public SecretKey getPathInfoKey() {
        return myPathInfoKey;
    }

    public void setPathInfoKey(SecretKey pathInfoKey) {
        myPathInfoKey = pathInfoKey;
    }

    public int getStreamingCacheTimeout() {
        return myStreamingCacheTimeout;
    }

    public void setStreamingCacheTimeout(int streamingCacheTimeout) {
        myStreamingCacheTimeout = streamingCacheTimeout;
    }

    public int getStreamingCacheMaxFiles() {
        return myStreamingCacheMaxFiles;
    }

    public void setStreamingCacheMaxFiles(int streamingCacheMaxFiles) {
        myStreamingCacheMaxFiles = streamingCacheMaxFiles;
    }

    public boolean isBandwidthLimit() {
        return myBandwidthLimit;
    }

    public void setBandwidthLimit(boolean bandwidthLimit) {
        myBandwidthLimit = bandwidthLimit;
    }

    public BigDecimal getBandwidthLimitFactor() {
        return myBandwidthLimitFactor;
    }

    public void setBandwidthLimitFactor(BigDecimal bandwidthLimitFactor) {
        myBandwidthLimitFactor = bandwidthLimitFactor;
    }

    public boolean isIgnoreArtwork() {
        return myIgnoreArtwork;
    }

    public void setIgnoreArtwork(boolean ignoreArtwork) {
        myIgnoreArtwork = ignoreArtwork;
    }

    public Level getCodewaveLogLevel() {
        return myCodewaveLogLevel;
    }

    public void setCodewaveLogLevel(Level codewaveLogLevel) {
        myCodewaveLogLevel = codewaveLogLevel;
    }

    public String getTomcatProxyHost() {
        return myTomcatProxyHost;
    }

    public void setTomcatProxyHost(String tomcatProxyHost) {
        myTomcatProxyHost = tomcatProxyHost;
    }

    public String getTomcatSslProxyHost() {
        return myTomcatSslProxyHost;
    }

    public void setTomcatSslProxyHost(String tomcatSslProxyHost) {
        myTomcatSslProxyHost = tomcatSslProxyHost;
    }

    public int getTomcatSslProxyPort() {
        return myTomcatSslProxyPort;
    }

    public void setTomcatSslProxyPort(int tomcatSslProxyPort) {
        myTomcatSslProxyPort = tomcatSslProxyPort;
    }

    public int getTomcatProxyPort() {
        return myTomcatProxyPort;
    }

    public void setTomcatProxyPort(int tomcatProxyPort) {
        myTomcatProxyPort = tomcatProxyPort;
    }

    public String getTomcatProxyScheme() {
        return myTomcatProxyScheme;
    }

    public void setTomcatProxyScheme(String tomcatProxyScheme) {
        myTomcatProxyScheme = tomcatProxyScheme;
    }

    public String getTomcatSslProxyScheme() {
        return myTomcatSslProxyScheme;
    }

    public void setTomcatSslProxyScheme(String tomcatSslProxyScheme) {
        myTomcatSslProxyScheme = tomcatSslProxyScheme;
    }

    public Collection<User> getUsers() {
        Collection<User> users = new HashSet<User>();
        for (User user : myUsers) {
            users.add(user);
        }
        return users;
    }

    public User getUser(String name) {
        for (User user : getUsers()) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;
    }

    public void removeUser(User user) {
        myUsers.remove(user);
        for (User eachUser : myUsers) {
            if (eachUser.getParent() == user) {
                eachUser.setParent(null);
            }
        }
    }

    public void addUser(User user) {
        myUsers.add(user);
    }

    public String getSupportEmail() {
        return mySupportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        mySupportEmail = supportEmail;
    }

    public String getSupportName() {
        return mySupportName;
    }

    public void setSupportName(String supportName) {
        mySupportName = supportName;
    }

    public String getProxyHost() {
        return myProxyHost;
    }

    public void setProxyHost(String proxyHost) {
        myProxyHost = proxyHost;
    }

    public int getProxyPort() {
        return myProxyPort;
    }

    public void setProxyPort(int proxyPort) {
        myProxyPort = proxyPort;
    }

    public boolean isProxyServer() {
        return StringUtils.isNotBlank(myProxyHost) && myProxyPort > 0 && myProxyPort < 65536;
    }

    public byte[] getMyTunesRssComPasswordHash() {
        return myMyTunesRssComPasswordHash;
    }

    public void setMyTunesRssComPasswordHash(byte[] myTunesRssComPasswordHash) {
        myMyTunesRssComPasswordHash = myTunesRssComPasswordHash;
    }

    public String getMyTunesRssComUser() {
        return myMyTunesRssComUser;
    }

    public void setMyTunesRssComUser(String myTunesRssComUser) {
        myMyTunesRssComUser = myTunesRssComUser;
    }

    public boolean isMyTunesRssComSsl() {
        return myMyTunesRssComSsl;
    }

    public void setMyTunesRssComSsl(boolean myTunesRssComSsl) {
        myMyTunesRssComSsl = myTunesRssComSsl;
    }

    public String getWebWelcomeMessage() {
        return myWebWelcomeMessage;
    }

    public void setWebWelcomeMessage(String webWelcomeMessage) {
        myWebWelcomeMessage = webWelcomeMessage;
    }

    public String getWebLoginMessage() {
        return myWebLoginMessage;
    }

    public void setWebLoginMessage(String webLoginMessage) {
        myWebLoginMessage = webLoginMessage;
    }

    public void setLastNewVersionInfo(String lastNewVersionInfo) {
        myLastNewVersionInfo = lastNewVersionInfo;
    }

    public boolean isDeleteDatabaseOnExit() {
        return myDeleteDatabaseOnExit;
    }

    public void setDeleteDatabaseOnExit(boolean deleteDatabaseOnExit) {
        myDeleteDatabaseOnExit = deleteDatabaseOnExit;
    }

    public void setUpdateIgnoreVersion(String updateIgnoreVersion) {
        myUpdateIgnoreVersion = updateIgnoreVersion;
    }

    public List<String> getDatabaseCronTriggers() {
        return myDatabaseCronTriggers;
    }

    public void setDatabaseCronTriggers(List<String> databaseCronTriggers) {
        myDatabaseCronTriggers = databaseCronTriggers;
    }

    public String getDatabaseConnection() {
        return myDatabaseConnection;
    }

    public void setDatabaseConnection(String databaseConnection) {
        myDatabaseConnection = databaseConnection;
    }

    public String getDatabasePassword() {
        return myDatabasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        myDatabasePassword = databasePassword;
    }

    public String getDatabaseType() {
        return myDatabaseType;
    }

    public void setDatabaseType(String databaseType) {
        myDatabaseType = databaseType;
    }

    public String getDatabaseUser() {
        return myDatabaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        myDatabaseUser = databaseUser;
    }

    public String getDatabaseDriver() {
        return myDatabaseDriver;
    }

    public void setDatabaseDriver(String databaseDriver) {
        myDatabaseDriver = databaseDriver;
    }

    public String getWebappContext() {
        return myWebappContext;
    }

    public void setWebappContext(String webappContext) {
        myWebappContext = webappContext;
    }

    public String getId3v2TrackComment() {
        return myId3v2TrackComment;
    }

    public void setId3v2TrackComment(String id3v2TrackComment) {
        myId3v2TrackComment = id3v2TrackComment;
    }

    public String getTomcatMaxThreads() {
        return myTomcatMaxThreads;
    }

    public void setTomcatMaxThreads(String tomcatMaxThreads) {
        myTomcatMaxThreads = tomcatMaxThreads;
    }

    public int getTomcatAjpPort() {
        return myTomcatAjpPort;
    }

    public void setTomcatAjpPort(int tomcatAjpPort) {
        myTomcatAjpPort = tomcatAjpPort;
    }

    public String getSslKeystoreFile() {
        return mySslKeystoreFile;
    }

    public void setSslKeystoreFile(String sslKeystoreFile) {
        mySslKeystoreFile = sslKeystoreFile;
    }

    public String getSslKeystoreKeyAlias() {
        return mySslKeystoreKeyAlias;
    }

    public void setSslKeystoreKeyAlias(String sslKeystoreKeyAlias) {
        mySslKeystoreKeyAlias = sslKeystoreKeyAlias;
    }

    public String getSslKeystorePass() {
        return mySslKeystorePass;
    }

    public void setSslKeystorePass(String sslKeystorePass) {
        mySslKeystorePass = sslKeystorePass;
    }

    public int getSslPort() {
        return mySslPort;
    }

    public void setSslPort(int sslPort) {
        mySslPort = sslPort;
    }

    public FileType getFileType(String suffix) {
        if (suffix != null) {
            for (FileType type : myFileTypes) {
                if (suffix.equalsIgnoreCase(type.getSuffix())) {
                    return type;
                }
            }
        }
        return null;
    }

    public String getMailHost() {
        return myMailHost;
    }

    public void setMailHost(String mailHost) {
        myMailHost = mailHost;
    }

    public String getMailLogin() {
        return myMailLogin;
    }

    public void setMailLogin(String mailLogin) {
        myMailLogin = mailLogin;
    }

    public String getMailPassword() {
        return myMailPassword;
    }

    public void setMailPassword(String mailPassword) {
        myMailPassword = mailPassword;
    }

    public int getMailPort() {
        return myMailPort;
    }

    public void setMailPort(int mailPort) {
        myMailPort = mailPort;
    }

    public SmtpProtocol getSmtpProtocol() {
        return mySmtpProtocol;
    }

    public void setSmtpProtocol(SmtpProtocol smtpProtocol) {
        mySmtpProtocol = smtpProtocol;
    }

    public String getMailSender() {
        return myMailSender;
    }

    public void setMailSender(String mailSender) {
        myMailSender = mailSender;
    }

    public String getAdminEmail() {
        return myAdminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        myAdminEmail = adminEmail;
    }

    public boolean isNotifyOnPasswordChange() {
        return myNotifyOnPasswordChange;
    }

    public void setNotifyOnPasswordChange(boolean notifyOnPasswordChange) {
        myNotifyOnPasswordChange = notifyOnPasswordChange;
    }

    public boolean isNotifyOnEmailChange() {
        return myNotifyOnEmailChange;
    }

    public void setNotifyOnEmailChange(boolean notifyOnEmailChange) {
        myNotifyOnEmailChange = notifyOnEmailChange;
    }

    public boolean isNotifyOnQuotaExceeded() {
        return myNotifyOnQuotaExceeded;
    }

    public void setNotifyOnQuotaExceeded(boolean notifyOnQuotaExceeded) {
        myNotifyOnQuotaExceeded = notifyOnQuotaExceeded;
    }

    public boolean isNotifyOnLoginFailure() {
        return myNotifyOnLoginFailure;
    }

    public void setNotifyOnLoginFailure(boolean notifyOnLoginFailure) {
        myNotifyOnLoginFailure = notifyOnLoginFailure;
    }

    public boolean isNotifyOnWebUpload() {
        return myNotifyOnWebUpload;
    }

    public void setNotifyOnWebUpload(boolean notifyOnWebUpload) {
        myNotifyOnWebUpload = notifyOnWebUpload;
    }

    public boolean isNotifyOnTranscodingFailure() {
        return myNotifyOnTranscodingFailure;
    }

    public void setNotifyOnTranscodingFailure(boolean notifyOnTranscodingFailure) {
        myNotifyOnTranscodingFailure = notifyOnTranscodingFailure;
    }

    public boolean isNotifyOnInternalError() {
        return myNotifyOnInternalError;
    }

    public void setNotifyOnInternalError(boolean notifyOnInternalError) {
        myNotifyOnInternalError = notifyOnInternalError;
    }

    public boolean isNotifyOnDatabaseUpdate() {
        return myNotifyOnDatabaseUpdate;
    }

    public void setNotifyOnDatabaseUpdate(boolean notifyOnDatabaseUpdate) {
        myNotifyOnDatabaseUpdate = notifyOnDatabaseUpdate;
    }

    public boolean isNotifyOnMissingFile() {
        return myNotifyOnMissingFile;
    }

    public void setNotifyOnMissingFile(boolean notifyOnMissingFile) {
        myNotifyOnMissingFile = notifyOnMissingFile;
    }

    public int getStatisticKeepTime() {
        return myStatisticKeepTime;
    }

    public void setStatisticKeepTime(int statisticKeepTime) {
        myStatisticKeepTime = statisticKeepTime;
    }

    public String getDisabledMp4Codecs() {
        return myDisabledMp4Codecs;
    }

    public void setDisabledMp4Codecs(String disabledMp4Codecs) {
        myDisabledMp4Codecs = disabledMp4Codecs;
    }

    public List<TranscoderConfig> getTranscoderConfigs() {
        return myTranscoderConfigs;
    }

    public List<ExternalSiteDefinition> getExternalSites() {
        return new ArrayList<ExternalSiteDefinition>(myExternalSites);
    }

    public List<ExternalSiteDefinition> getExternalSites(String type) {
        List<ExternalSiteDefinition> result = new ArrayList<ExternalSiteDefinition>();
        for (ExternalSiteDefinition def : myExternalSites) {
            if (StringUtils.equals(type, def.getType())) {
                result.add(def);
            }
        }
        return result;
    }

    public void addExternalSite(ExternalSiteDefinition definition) {
        myExternalSites.add(definition);
    }

    public void removeExternalSite(ExternalSiteDefinition definition) {
        for (Iterator<ExternalSiteDefinition> iter = myExternalSites.iterator(); iter.hasNext();) {
            if (definition.equals(iter.next())) {
                iter.remove();
                break;
            }
        }
    }

    public String getAutoLogin() {
        return myAutoLogin;
    }

    public void setAutoLogin(String autoLogin) {
        myAutoLogin = autoLogin;
    }

    public boolean isDisableBrowser() {
        return myDisableBrowser;
    }

    public void setDisableBrowser(boolean disableBrowser) {
        myDisableBrowser = disableBrowser;
    }

    public boolean isServerBrowserActive() {
        return myServerBrowserActive;
    }

    public void setServerBrowserActive(boolean serverBrowserActive) {
        myServerBrowserActive = serverBrowserActive;
    }

    public boolean isDisableWebLogin() {
        return myDisableWebLogin;
    }

    public void setDisableWebLogin(boolean disableWebLogin) {
        myDisableWebLogin = disableWebLogin;
    }

    public boolean isQuicktime64BitWarned() {
        return myQuicktime64BitWarned;
    }

    public void setQuicktime64BitWarned(boolean quicktime64BitWarned) {
        myQuicktime64BitWarned = quicktime64BitWarned;
    }

    public LdapConfig getLdapConfig() {
        return myLdapConfig;
    }

    public boolean isAdminPassword() {
        return !Arrays.equals(MyTunesRss.CONFIG.getAdminPasswordHash(), MyTunesRss.SHA1_DIGEST.digest(new byte[0]));
    }

    public byte[] getAdminPasswordHash() {
        return myAdminPasswordHash != null ? myAdminPasswordHash : MyTunesRss.SHA1_DIGEST.digest(MyTunesRssUtils.getUtf8Bytes(""));
    }

    public void setAdminPasswordHash(byte[] adminPasswordHash) {
        myAdminPasswordHash = adminPasswordHash;
    }

    public int getAdminPort() {
        return myAdminPort;
    }

    public void setAdminPort(int adminPort) {
        myAdminPort = adminPort;
    }

    public boolean isImportOriginalImageSize() {
        return myImportOriginalImageSize;
    }

    public void setImportOriginalImageSize(boolean importOriginalImageSize) {
        myImportOriginalImageSize = importOriginalImageSize;
    }

    public Set<FlashPlayerConfig> getFlashPlayers() {
        return new HashSet<FlashPlayerConfig>(myFlashPlayers);
    }

    public FlashPlayerConfig getFlashPlayer(String id) {
        for (FlashPlayerConfig flashPlayer : myFlashPlayers) {
            if (flashPlayer.getId().equals(id)) {
                return flashPlayer;
            }
        }
        return null;
    }

    public void addFlashPlayer(FlashPlayerConfig flashPlayer) {
        myFlashPlayers.add(flashPlayer);
    }

    public void clearFlashPlayer() {
        myFlashPlayers.clear();
    }

    public boolean isInitialWizard() {
        return myInitialWizard;
    }

    public void setInitialWizard(boolean initialWizard) {
        myInitialWizard = initialWizard;
    }

    private String encryptCreationTime(long creationTime) {
        String checksum = Long.toString(creationTime);
        try {
            Cipher cipher = Cipher.getInstance(CHECKSUM_KEY.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, CHECKSUM_KEY);
            return new String(Base64.encodeBase64(cipher.doFinal(checksum.getBytes("UTF-8"))), "UTF-8");
        } catch (Exception e) {
            LOGGER.error("Could not encrypt creation time!", e);
        }
        return null;
    }

    public long getConfigCreationTime() {
        try {
            Cipher cipher = Cipher.getInstance(CHECKSUM_KEY.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, CHECKSUM_KEY);
            String creationTime = new String(cipher.doFinal(Base64.decodeBase64(myCryptedCreationTime.getBytes("UTF-8"))), "UTF-8");
            return Long.parseLong(creationTime);
        } catch (Exception e) {
            LOGGER.error("Could not decrypt creation time!", e);
        }
        return 1;
    }

    public void load() {
        try {
            File file = getSettingsFile();
            LOGGER.info("Loading configuration from \"" + file.getAbsolutePath() + "\".");
            String freshCryptedCreationTime = encryptCreationTime(System.currentTimeMillis());
            if (!file.isFile()) {
                FileUtils.writeStringToFile(file,
                        "<settings><" + CREATION_TIME_KEY + ">" + freshCryptedCreationTime + "</" + CREATION_TIME_KEY +
                                "></settings>");
            }
            JXPathContext settings = JXPathUtils.getContext(JXPathUtils.getContext(file.toURL()), "settings");
            setVersion(StringUtils.defaultIfEmpty(JXPathUtils.getStringValue(settings, "version", "0"), "0"));
            Version currentAppVersion = new Version(MyTunesRss.VERSION);
            Version currentConfigVersion = new Version(getVersion());
            Version minimumChecksumVersion = new Version("3.6.2");
            myCryptedCreationTime = JXPathUtils.getStringValue(settings,
                    CREATION_TIME_KEY,
                    currentConfigVersion.compareTo(minimumChecksumVersion) >= 0 ? encryptCreationTime(1) :
                            freshCryptedCreationTime);
            if (StringUtils.isNotBlank(currentConfigVersion.getAppendix())) {
                // fresh evaluation period if last version was not a release version
                myCryptedCreationTime = freshCryptedCreationTime;
            } else if (currentAppVersion.getMajor() != currentConfigVersion.getMajor() || currentAppVersion.getMinor() != currentConfigVersion.getMinor()) {
                // fresh evaluation period if the last version was a different major or minor version
                myCryptedCreationTime = freshCryptedCreationTime;
            }
            if (currentConfigVersion.compareTo(currentAppVersion) < 0) {
                migrate();
            }
            loadFromContext(settings);
        } catch (IOException e) {
            LOGGER.error("Could not read configuration file.", e);
        }
    }

    public void loadFromContext(JXPathContext settings) {
        try {
            setVersion(MyTunesRss.VERSION);
            load(settings);
        } catch (IOException e) {
            LOGGER.error("Could not read configuration file.", e);
        }

    }

    private void load(JXPathContext settings) throws IOException {
        setAdminPasswordHash(JXPathUtils.getByteArray(settings, "adminPassword", getAdminPasswordHash()));
        setAdminPort(JXPathUtils.getIntValue(settings, "adminPort", getAdminPort()));
        setImportOriginalImageSize(JXPathUtils.getBooleanValue(settings, "importOriginalImageSize", isImportOriginalImageSize()));
        setPort(JXPathUtils.getIntValue(settings, "serverPort", getPort()));
        setServerName(JXPathUtils.getStringValue(settings, "serverName", getServerName()));
        setAvailableOnLocalNet(JXPathUtils.getBooleanValue(settings, "availableOnLocalNet", isAvailableOnLocalNet()));
        setCheckUpdateOnStart(JXPathUtils.getBooleanValue(settings, "checkUpdateOnStart", isCheckUpdateOnStart()));
        setIgnoreTimestamps(JXPathUtils.getBooleanValue(settings, "ignoreTimestamps", isIgnoreTimestamps()));
        readDataSources(settings);
        setUploadDir(JXPathUtils.getStringValue(settings, "uploadDir", getUploadDir()));
        setUploadCreateUserDir(JXPathUtils.getBooleanValue(settings, "uploadCreateUserDir", isUploadCreateUserDir()));
        setLocalTempArchive(JXPathUtils.getBooleanValue(settings, "localTempArchive", isLocalTempArchive()));
        setSupportName(JXPathUtils.getStringValue(settings, "supportName", getSupportName()));
        setSupportEmail(JXPathUtils.getStringValue(settings, "supportEmail", getSupportEmail()));
        setProxyHost(JXPathUtils.getStringValue(settings, "proxyHost", getProxyHost()));
        setProxyPort(JXPathUtils.getIntValue(settings, "proxyPort", getProxyPort()));
        setMyTunesRssComSsl(JXPathUtils.getBooleanValue(settings, "myTunesRssComSsl", isMyTunesRssComSsl()));
        setMyTunesRssComUser(JXPathUtils.getStringValue(settings, "myTunesRssComUser", getMyTunesRssComUser()));
        setMyTunesRssComPasswordHash(JXPathUtils.getByteArray(settings, "myTunesRssComPassword", getMyTunesRssComPasswordHash()));
        myFileTypes = new ArrayList<FileType>();
        Iterator<JXPathContext> fileTypes = JXPathUtils.getContextIterator(settings, "file-types/type");
        while (fileTypes != null && fileTypes.hasNext()) {
            JXPathContext fileTypeContext = fileTypes.next();
            FileType fileType = new FileType();
            fileType.setMimeType(JXPathUtils.getStringValue(fileTypeContext, "mime-type", "audio/mp3"));
            fileType.setSuffix(JXPathUtils.getStringValue(fileTypeContext, "suffix", "mp3"));
            // Loading the element "video" is for migration purposes from older versions
            String mediaTypeFromOldVideoElement = JXPathUtils.getBooleanValue(fileTypeContext, "video", false) ? MediaType.Video.name() : MediaType.Audio.name();
            fileType.setMediaType(MediaType.valueOf(JXPathUtils.getStringValue(fileTypeContext, "mediatype", mediaTypeFromOldVideoElement)));
            fileType.setProtected(JXPathUtils.getBooleanValue(fileTypeContext, "protected", false));
            fileType.setActive(JXPathUtils.getBooleanValue(fileTypeContext, "active", true));
            myFileTypes.add(fileType);
        }
        setArtistDropWords(JXPathUtils.getStringValue(settings, "artistDropWords", getArtistDropWords()));
        setWebWelcomeMessage(JXPathUtils.getStringValue(settings, "webWelcomeMessage", getWebWelcomeMessage()));
        setWebLoginMessage(JXPathUtils.getStringValue(settings, "webLoginMessage", getWebLoginMessage()));
        readPathInfoEncryptionKey(settings);
        setStreamingCacheTimeout(JXPathUtils.getIntValue(settings, "streamingCacheTimeout", getStreamingCacheTimeout()));
        setStreamingCacheMaxFiles(JXPathUtils.getIntValue(settings, "streamingCacheMaxFiles", getStreamingCacheMaxFiles()));
        setBandwidthLimit(JXPathUtils.getBooleanValue(settings, "bandwidthLimit", false));
        setBandwidthLimitFactor(new BigDecimal(JXPathUtils.getStringValue(settings, "bandwidthLimitFactor", "0")));
        setIgnoreArtwork(JXPathUtils.getBooleanValue(settings, "ignoreArtwork", false));
        setCodewaveLogLevel(Level.toLevel(JXPathUtils.getStringValue(settings, "codewaveLogLevel", Level.INFO.toString()).toUpperCase()));
        setLastNewVersionInfo(JXPathUtils.getStringValue(settings, "lastNewVersionInfo", "0"));
        setUpdateIgnoreVersion(JXPathUtils.getStringValue(settings, "updateIgnoreVersion", MyTunesRss.VERSION));
        Iterator<JXPathContext> cronTriggerIterator = JXPathUtils.getContextIterator(settings, "crontriggers/database");
        myDatabaseCronTriggers = new ArrayList<String>();
        while (cronTriggerIterator.hasNext()) {
            myDatabaseCronTriggers.add(JXPathUtils.getStringValue(cronTriggerIterator.next(), ".", ""));
        }
        loadDatabaseSettings(settings);
        setId3v2TrackComment(JXPathUtils.getStringValue(settings, "id3v2-track-comment", ""));
        setTomcatMaxThreads(JXPathUtils.getStringValue(settings, "tomcat/max-threads", "200"));
        setTomcatAjpPort(JXPathUtils.getIntValue(settings, "tomcat/ajp-port", 0));
        String context = StringUtils.trimToNull(StringUtils.strip(JXPathUtils.getStringValue(settings, "tomcat/webapp-context", ""), "/"));
        setWebappContext(context != null ? "/" + context : "");
        setTomcatProxyHost(JXPathUtils.getStringValue(settings, "tomcat/proxy-host", null));
        setTomcatProxyScheme(JXPathUtils.getStringValue(settings, "tomcat/proxy-scheme", "HTTP"));
        setTomcatProxyPort(JXPathUtils.getIntValue(settings, "tomcat/proxy-port", 0));
        setSslKeystoreFile(JXPathUtils.getStringValue(settings, "ssl/keystore/file", null));
        setSslKeystoreKeyAlias(JXPathUtils.getStringValue(settings, "ssl/keystore/keyalias", null));
        setSslKeystorePass(JXPathUtils.getStringValue(settings, "ssl/keystore/pass", null));
        setSslPort(JXPathUtils.getIntValue(settings, "ssl/port", 0));
        setTomcatSslProxyScheme(JXPathUtils.getStringValue(settings, "ssl/proxy-scheme", "HTTPS"));
        setTomcatSslProxyHost(JXPathUtils.getStringValue(settings, "ssl/proxy-host", null));
        setTomcatSslProxyPort(JXPathUtils.getIntValue(settings, "ssl/proxy-port", 0));
        setMailHost(JXPathUtils.getStringValue(settings, "mail-host", null));
        setMailPort(JXPathUtils.getIntValue(settings, "mail-port", -1));
        setSmtpProtocol(SmtpProtocol.valueOf(JXPathUtils.getStringValue(settings, "smtp-protocol", SmtpProtocol.PLAINTEXT.name())));
        setMailLogin(JXPathUtils.getStringValue(settings, "mail-login", null));
        setMailPassword(JXPathUtils.getStringValue(settings, "mail-password", null));
        setMailSender(JXPathUtils.getStringValue(settings, "mail-sender", null));
        setAdminEmail(JXPathUtils.getStringValue(settings, "admin-email", null));
        setNotifyOnDatabaseUpdate(JXPathUtils.getBooleanValue(settings, "admin-notify/database-update", false));
        setNotifyOnEmailChange(JXPathUtils.getBooleanValue(settings, "admin-notify/email-change", false));
        setNotifyOnInternalError(JXPathUtils.getBooleanValue(settings, "admin-notify/internal-error", false));
        setNotifyOnLoginFailure(JXPathUtils.getBooleanValue(settings, "admin-notify/login-failure", false));
        setNotifyOnPasswordChange(JXPathUtils.getBooleanValue(settings, "admin-notify/password-change", false));
        setNotifyOnQuotaExceeded(JXPathUtils.getBooleanValue(settings, "admin-notify/quota-exceeded", false));
        setNotifyOnTranscodingFailure(JXPathUtils.getBooleanValue(settings, "admin-notify/transcoding-failure", false));
        setNotifyOnWebUpload(JXPathUtils.getBooleanValue(settings, "admin-notify/web-upload", false));
        setNotifyOnMissingFile(JXPathUtils.getBooleanValue(settings, "admin-notify/missing-file", false));
        if (myFileTypes.isEmpty()) {
            myFileTypes = FileType.getDefaults();
        }
        try {
            setStatisticKeepTime(JXPathUtils.getIntValue(settings, "statistics-keep-time", getStatisticKeepTime()));
        } catch (Exception e) {
            LOGGER.warn("Could not read/parse statistics keep time, keeping default.");
            // intentionally left blank; keep default
        }
        setDisabledMp4Codecs(JXPathUtils.getStringValue(settings, "disabled-mp4-codecs", null));
        Iterator<JXPathContext> transcoderConfigIterator = JXPathUtils.getContextIterator(settings, "transcoders/config");
        myTranscoderConfigs = new ArrayList<TranscoderConfig>();
        while (transcoderConfigIterator.hasNext()) {
            JXPathContext transcoderConfigContext = transcoderConfigIterator.next();
            myTranscoderConfigs.add(new TranscoderConfig(transcoderConfigContext));
        }
        Iterator<JXPathContext> externalSitesIterator = JXPathUtils.getContextIterator(settings, "external-sites/site");
        myExternalSites = new ArrayList<ExternalSiteDefinition>();
        while (externalSitesIterator.hasNext()) {
            JXPathContext externalSiteContext = externalSitesIterator.next();
            String name = JXPathUtils.getStringValue(externalSiteContext, "name", null);
            String url = JXPathUtils.getStringValue(externalSiteContext, "url", null);
            String type = JXPathUtils.getStringValue(externalSiteContext, "type", null);
            myExternalSites.add(new ExternalSiteDefinition(type, name, url));
        }
        setServerBrowserActive(JXPathUtils.getBooleanValue(settings, "serverBrowserActive", true));
        setAutoLogin(JXPathUtils.getStringValue(settings, "autoLogin", null));
        setDisableBrowser(JXPathUtils.getBooleanValue(settings, "disableBrowser", false));
        setDisableWebLogin(JXPathUtils.getBooleanValue(settings, "disableWebLogin", false));
        setQuicktime64BitWarned(JXPathUtils.getBooleanValue(settings, "qt64BitWarned", false));
        myLdapConfig = new LdapConfig(settings);
        Iterator<JXPathContext> users = JXPathUtils.getContextIterator(settings, "users/user");
        while (users != null && users.hasNext()) {
            JXPathContext userContext = users.next();
            User user = new User(JXPathUtils.getStringValue(userContext, "name", null));
            user.loadFromPreferences(userContext);
            addUser(user);
        }
        markGroupUsers();
        myFlashPlayers.clear();
        Iterator<JXPathContext> flashPlayerIterator = JXPathUtils.getContextIterator(settings, "flash-players/player");
        while (flashPlayerIterator.hasNext()) {
            JXPathContext flashPlayerContext = flashPlayerIterator.next();
            FlashPlayerConfig flashPlayerConfig = new FlashPlayerConfig(
                    JXPathUtils.getStringValue(flashPlayerContext, "id", UUID.randomUUID().toString()),
                    JXPathUtils.getStringValue(flashPlayerContext, "name", "Unknown Flash Player"),
                    new String(JXPathUtils.getByteArray(flashPlayerContext, "html", "<!-- missing flash player html -->".getBytes("UTF-8")), "UTF-8")
            );
            addFlashPlayer(flashPlayerConfig);
        }
        setInitialWizard(JXPathUtils.getBooleanValue(settings, "initialWizard", true));
    }

    /**
     * Mark all groups users as groups
     */
    private void markGroupUsers() {
        // mark them
        for (User userToCheck : myUsers) {
            for (User user : myUsers) {
                if (userToCheck.equals(user.getParent())) {
                    userToCheck.setGroup(true);
                    break;
                }
            }
        }
        // and flatten them
        for (User user : myUsers) {
            if (user.isGroup()) {
                user.setParent(null);
            }
        }
    }

    private void readDataSources(JXPathContext settings) {
        List<DatasourceConfig> dataSources = new ArrayList<DatasourceConfig>();
        Iterator<JXPathContext> contextIterator = JXPathUtils.getContextIterator(settings, "datasources/datasource");
        while (contextIterator.hasNext()) {
            JXPathContext datasourceContext = contextIterator.next();
            if (JXPathUtils.getStringValue(datasourceContext, "type", null) == null) {
                // read pre-4.0.0-EAP-6 data source definitions
                String definition = JXPathUtils.getStringValue(datasourceContext, ".", null);
                if (definition != null) {
                    dataSources.add(DatasourceConfig.create(definition));
                }
            } else {
                try {
                    DatasourceType type = DatasourceType.valueOf(JXPathUtils.getStringValue(datasourceContext, "type", DatasourceType.Itunes.name()));
                    String definition = JXPathUtils.getStringValue(datasourceContext, "definition", "");
                    switch (type) {
                        case Watchfolder:
                            WatchfolderDatasourceConfig watchfolderDatasourceConfig = new WatchfolderDatasourceConfig(definition);
                            watchfolderDatasourceConfig.setMinFileSize(JXPathUtils.getLongValue(datasourceContext, "minFileSize", 0));
                            watchfolderDatasourceConfig.setMaxFileSize(JXPathUtils.getLongValue(datasourceContext, "maxFileSize", 0));
                            watchfolderDatasourceConfig.setIncludePattern(JXPathUtils.getStringValue(datasourceContext, "include", null));
                            watchfolderDatasourceConfig.setExcludePattern(JXPathUtils.getStringValue(datasourceContext, "exclude", null));
                            watchfolderDatasourceConfig.setAlbumFallback(JXPathUtils.getStringValue(datasourceContext, "albumFallback", "[dir:0]"));
                            watchfolderDatasourceConfig.setArtistFallback(JXPathUtils.getStringValue(datasourceContext, "artistFallback", "[dir:1]"));
                            dataSources.add(watchfolderDatasourceConfig);
                            break;
                        case Itunes:
                            ItunesDatasourceConfig itunesDatasourceConfig = new ItunesDatasourceConfig(definition);
                            Iterator<JXPathContext> pathReplacementsIterator = JXPathUtils.getContextIterator(datasourceContext, "path-replacements/replacement");
                            itunesDatasourceConfig.clearPathReplacements();
                            while (pathReplacementsIterator.hasNext()) {
                                JXPathContext pathReplacementContext = pathReplacementsIterator.next();
                                String search = JXPathUtils.getStringValue(pathReplacementContext, "search", null);
                                String replacement = JXPathUtils.getStringValue(pathReplacementContext, "replacement", null);
                                itunesDatasourceConfig.addPathReplacement(new PathReplacement(search, replacement));
                            }
                            Iterator<JXPathContext> ignorePlaylistsIterator = JXPathUtils.getContextIterator(datasourceContext, "ignore-playlists/type");
                            itunesDatasourceConfig.clearIgnorePlaylists();
                            while (ignorePlaylistsIterator.hasNext()) {
                                JXPathContext ignorePlaylistsContext = ignorePlaylistsIterator.next();
                                try {
                                    itunesDatasourceConfig.addIgnorePlaylist(ItunesPlaylistType.valueOf(JXPathUtils.getStringValue(ignorePlaylistsContext, ".", ItunesPlaylistType.Master.name())));
                                } catch (IllegalArgumentException e) {
                                    // ignore illegal config entry
                                }
                            }
                            itunesDatasourceConfig.setDeleteMissingFiles(JXPathUtils.getBooleanValue(datasourceContext, "deleteMissingFiles", true));

                            dataSources.add(itunesDatasourceConfig);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown datasource type!");
                    }
                } catch (IllegalArgumentException e) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Illegal data source of type \"" + JXPathUtils.getStringValue(datasourceContext, "type", DatasourceType.Itunes.name()) + "\" ignored.");
                    }

                }
            }
        }
        setDatasources(dataSources);
    }

    private void loadDatabaseSettings(JXPathContext settings) throws IOException {
        setDefaultDatabaseSettings();
        setDatabaseType(JXPathUtils.getStringValue(settings, "database/type", getDatabaseType()));
        // for default h2, always use calculated defaults
        if (!StringUtils.equals(myDatabaseType, DatabaseType.h2.name())) {
            setDatabaseDriver(JXPathUtils.getStringValue(settings, "database/driver", getDatabaseDriver()));
            setDatabaseConnection(JXPathUtils.getStringValue(settings, "database/connection", getDatabaseConnection()));
            setDatabaseUser(JXPathUtils.getStringValue(settings, "database/user", getDatabaseUser()));
            setDatabasePassword(JXPathUtils.getStringValue(settings, "database/password", getDatabasePassword()));
        }
    }

    public void setDefaultDatabaseSettings() throws IOException {
        setDatabaseType("h2");
        setDatabaseDriver("org.h2.Driver");
        setDatabaseConnection("jdbc:h2:file:" + MyTunesRssUtils.getCacheDataPath() + "/" + "h2/MyTunesRSS");
        setDatabaseUser("sa");
        setDatabasePassword("");
    }

    private static File getSettingsFile() throws IOException {
        String filename = "settings.xml";
        return new File(MyTunesRssUtils.getPreferencesDataPath() + "/" + filename);
    }

    private void readPathInfoEncryptionKey(JXPathContext settings) {
        byte[] keyBytes = JXPathUtils.getByteArray(settings, "pathInfoKey", null);
        if (keyBytes != null && keyBytes.length > 0) {
            myPathInfoKey = new SecretKeySpec(keyBytes, "DES");
        }
        if (myPathInfoKey == null) {
            LOGGER.info("No path info encryption key found, generating a new one.");
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
                keyGenerator.init(56);
                myPathInfoKey = keyGenerator.generateKey();
            } catch (Exception e) {
                LOGGER.error("Could not generate path info encryption key.", e);
            }
        }
    }

    public void save() {
        try {
            LOGGER.info("Saving configuration to \"" + getSettingsFile().getAbsolutePath() + "\".");
            Document settings = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = settings.createElement("settings");
            settings.appendChild(root);
            root.appendChild(DOMUtils.createByteArrayElement(settings, "adminPassword", getAdminPasswordHash()));
            root.appendChild(DOMUtils.createIntElement(settings, "adminPort", myAdminPort));
            root.appendChild(DOMUtils.createBooleanElement(settings, "importOriginalImageSize", myImportOriginalImageSize));
            root.appendChild(DOMUtils.createTextElement(settings, "version", myVersion));
            root.appendChild(DOMUtils.createIntElement(settings, "serverPort", myPort));
            root.appendChild(DOMUtils.createTextElement(settings, "serverName", myServerName));
            root.appendChild(DOMUtils.createBooleanElement(settings, "availableOnLocalNet", myAvailableOnLocalNet));
            root.appendChild(DOMUtils.createBooleanElement(settings, "checkUpdateOnStart", myCheckUpdateOnStart));
            root.appendChild(DOMUtils.createBooleanElement(settings, "ignoreTimestamps", myIgnoreTimestamps));
            root.appendChild(DOMUtils.createIntElement(settings, "baseDirCount", myDatasources.size()));
            writeDataSources(settings, root);
            root.appendChild(DOMUtils.createTextElement(settings, "uploadDir", myUploadDir));
            root.appendChild(DOMUtils.createBooleanElement(settings, "uploadCreateUserDir", myUploadCreateUserDir));
            root.appendChild(DOMUtils.createBooleanElement(settings, "localTempArchive", myLocalTempArchive));
            Element users = settings.createElement("users");
            root.appendChild(users);
            for (User user : myUsers) {
                Element userElement = settings.createElement("user");
                users.appendChild(userElement);
                user.saveToPreferences(settings, userElement);
            }
            root.appendChild(DOMUtils.createTextElement(settings, "supportName", mySupportName));
            root.appendChild(DOMUtils.createTextElement(settings, "supportEmail", mySupportEmail));
            root.appendChild(DOMUtils.createTextElement(settings, "proxyHost", myProxyHost));
            root.appendChild(DOMUtils.createIntElement(settings, "proxyPort", myProxyPort));
            root.appendChild(DOMUtils.createBooleanElement(settings, "myTunesRssComSsl", myMyTunesRssComSsl));
            root.appendChild(DOMUtils.createTextElement(settings, "myTunesRssComUser", myMyTunesRssComUser));
            if (myMyTunesRssComPasswordHash != null && myMyTunesRssComPasswordHash.length > 0) {
                root.appendChild(DOMUtils.createByteArrayElement(settings, "myTunesRssComPassword", myMyTunesRssComPasswordHash));
            }
            Element fileTypes = settings.createElement("file-types");
            root.appendChild(fileTypes);
            for (FileType fileType : myFileTypes) {
                Element fileTypeElement = settings.createElement("type");
                fileTypes.appendChild(fileTypeElement);
                fileTypeElement.appendChild(DOMUtils.createTextElement(settings, "mime-type", fileType.getMimeType()));
                fileTypeElement.appendChild(DOMUtils.createTextElement(settings, "suffix", fileType.getSuffix()));
                fileTypeElement.appendChild(DOMUtils.createTextElement(settings, "mediatype", fileType.getMediaType().name()));
                fileTypeElement.appendChild(DOMUtils.createBooleanElement(settings, "protected", fileType.isProtected()));
                fileTypeElement.appendChild(DOMUtils.createBooleanElement(settings, "active", fileType.isActive()));
            }
            root.appendChild(DOMUtils.createTextElement(settings, "artistDropWords", myArtistDropWords));
            root.appendChild(DOMUtils.createTextElement(settings, CREATION_TIME_KEY, myCryptedCreationTime));
            root.appendChild(DOMUtils.createTextElement(settings, "webWelcomeMessage", myWebWelcomeMessage));
            root.appendChild(DOMUtils.createTextElement(settings, "webLoginMessage", myWebLoginMessage));
            if (myPathInfoKey != null) {
                root.appendChild(DOMUtils.createByteArrayElement(settings, "pathInfoKey", myPathInfoKey.getEncoded()));
            }
            root.appendChild(DOMUtils.createIntElement(settings, "streamingCacheTimeout", myStreamingCacheTimeout));
            root.appendChild(DOMUtils.createIntElement(settings, "streamingCacheMaxFiles", myStreamingCacheMaxFiles));
            root.appendChild(DOMUtils.createBooleanElement(settings, "bandwidthLimit", myBandwidthLimit));
            root.appendChild(DOMUtils.createTextElement(settings, "bandwidthLimitFactor", myBandwidthLimitFactor.toString()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "ignoreArtwork", myIgnoreArtwork));
            root.appendChild(DOMUtils.createTextElement(settings, "codewaveLogLevel", myCodewaveLogLevel.toString().toUpperCase()));
            Element window = settings.createElement("window");
            root.appendChild(window);
            root.appendChild(DOMUtils.createTextElement(settings, "lastNewVersionInfo", myLastNewVersionInfo));
            root.appendChild(DOMUtils.createTextElement(settings, "updateIgnoreVersion", myUpdateIgnoreVersion));
            if (myDatabaseCronTriggers != null && myDatabaseCronTriggers.size() > 0) {
                Element cronTriggers = settings.createElement("crontriggers");
                root.appendChild(cronTriggers);
                for (String databaseCronTrigger : myDatabaseCronTriggers) {
                    cronTriggers.appendChild(DOMUtils.createTextElement(settings, "database", databaseCronTrigger));
                }
            }
            // for default h2 database we shoud not save anything to the config
            if (!StringUtils.equals(getDatabaseType(), DatabaseType.h2.name())) {
                Element database = settings.createElement("database");
                root.appendChild(database);
                database.appendChild(DOMUtils.createTextElement(settings, "type", getDatabaseType()));
                database.appendChild(DOMUtils.createTextElement(settings, "driver", getDatabaseDriver()));
                database.appendChild(DOMUtils.createTextElement(settings, "connection", getDatabaseConnection()));
                database.appendChild(DOMUtils.createTextElement(settings, "user", getDatabaseUser()));
                database.appendChild(DOMUtils.createTextElement(settings, "password", getDatabasePassword()));
            }
            root.appendChild(DOMUtils.createTextElement(settings, "id3v2-track-comment", getId3v2TrackComment()));
            Element tomcat = settings.createElement("tomcat");
            root.appendChild(tomcat);
            tomcat.appendChild(DOMUtils.createTextElement(settings, "max-threads", getTomcatMaxThreads()));
            if (getTomcatAjpPort() > 0) {
                tomcat.appendChild(DOMUtils.createIntElement(settings, "ajp-port", getTomcatAjpPort()));
            }
            if (StringUtils.isNotEmpty(getTomcatProxyScheme())) {
                tomcat.appendChild(DOMUtils.createTextElement(settings, "proxy-scheme", getTomcatProxyScheme()));
            }
            if (StringUtils.isNotEmpty(getTomcatProxyHost())) {
                tomcat.appendChild(DOMUtils.createTextElement(settings, "proxy-host", getTomcatProxyHost()));
            }
            if (getTomcatProxyPort() > 0 && getTomcatProxyPort() < 65536) {
                tomcat.appendChild(DOMUtils.createIntElement(settings, "proxy-port", getTomcatProxyPort()));
            }
            tomcat.appendChild(DOMUtils.createTextElement(settings, "webapp-context", getWebappContext()));
            Element ssl = settings.createElement("ssl");
            root.appendChild(ssl);
            ssl.appendChild(DOMUtils.createIntElement(settings, "port", getSslPort()));
            if (StringUtils.isNotEmpty(getTomcatSslProxyScheme())) {
                ssl.appendChild(DOMUtils.createTextElement(settings, "proxy-scheme", getTomcatSslProxyScheme()));
            }
            if (StringUtils.isNotEmpty(getTomcatSslProxyHost())) {
                ssl.appendChild(DOMUtils.createTextElement(settings, "proxy-host", getTomcatSslProxyHost()));
            }
            if (getTomcatSslProxyPort() > 0 && getTomcatSslProxyPort() < 65536) {
                ssl.appendChild(DOMUtils.createIntElement(settings, "proxy-port", getTomcatSslProxyPort()));
            }
            Element keystore = settings.createElement("keystore");
            ssl.appendChild(keystore);
            keystore.appendChild(DOMUtils.createTextElement(settings, "file", getSslKeystoreFile()));
            keystore.appendChild(DOMUtils.createTextElement(settings, "pass", getSslKeystorePass()));
            keystore.appendChild(DOMUtils.createTextElement(settings, "keyalias", getSslKeystoreKeyAlias()));
            root.appendChild(DOMUtils.createTextElement(settings, "mail-host", getMailHost()));
            root.appendChild(DOMUtils.createIntElement(settings, "mail-port", getMailPort()));
            root.appendChild(DOMUtils.createTextElement(settings, "smtp-protocol", getSmtpProtocol().name()));
            root.appendChild(DOMUtils.createTextElement(settings, "mail-login", getMailLogin()));
            root.appendChild(DOMUtils.createTextElement(settings, "mail-password", getMailPassword()));
            root.appendChild(DOMUtils.createTextElement(settings, "mail-sender", getMailSender()));
            root.appendChild(DOMUtils.createTextElement(settings, "admin-email", getAdminEmail()));
            Element notify = settings.createElement("admin-notify");
            root.appendChild(notify);
            notify.appendChild(DOMUtils.createBooleanElement(settings, "database-update", isNotifyOnDatabaseUpdate()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "email-change", isNotifyOnEmailChange()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "internal-error", isNotifyOnInternalError()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "login-failure", isNotifyOnLoginFailure()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "password-change", isNotifyOnPasswordChange()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "quota-exceeded", isNotifyOnQuotaExceeded()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "transcoding-failure", isNotifyOnTranscodingFailure()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "web-upload", isNotifyOnWebUpload()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "missing-file", isNotifyOnMissingFile()));
            root.appendChild(DOMUtils.createIntElement(settings, "statistics-keep-time", getStatisticKeepTime()));
            root.appendChild(DOMUtils.createTextElement(settings, "disabled-mp4-codecs", getDisabledMp4Codecs()));
            Element transcoderConfigs = settings.createElement("transcoders");
            root.appendChild(transcoderConfigs);
            for (TranscoderConfig transcoderConfig : getTranscoderConfigs()) {
                Element config = settings.createElement("config");
                transcoderConfigs.appendChild(config);
                transcoderConfig.writeTo(settings, config);
            }
            if (myExternalSites != null && !myExternalSites.isEmpty()) {
                Element externalSites = settings.createElement("external-sites");
                root.appendChild(externalSites);
                for (ExternalSiteDefinition def : myExternalSites) {
                    Element xmlSite = settings.createElement("site");
                    externalSites.appendChild(xmlSite);
                    xmlSite.appendChild(DOMUtils.createTextElement(settings, "type", def.getType()));
                    xmlSite.appendChild(DOMUtils.createTextElement(settings, "name", def.getName()));
                    xmlSite.appendChild(DOMUtils.createTextElement(settings, "url", def.getUrl()));
                }
            }
            root.appendChild(DOMUtils.createBooleanElement(settings, "serverBrowserActive", isServerBrowserActive()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "qt64BitWarned", isQuicktime64BitWarned()));
            root.appendChild(myLdapConfig.createSettingsElement(settings));
            if (!getFlashPlayers().isEmpty()) {
                Element flashPlayers = settings.createElement("flash-players");
                root.appendChild(flashPlayers);
                for (FlashPlayerConfig flashPlayerConfig : getFlashPlayers()) {
                    Element player = settings.createElement("player");
                    flashPlayers.appendChild(player);
                    player.appendChild(DOMUtils.createTextElement(settings, "id", flashPlayerConfig.getId()));
                    player.appendChild(DOMUtils.createTextElement(settings, "name", flashPlayerConfig.getName()));
                    player.appendChild(DOMUtils.createByteArrayElement(settings, "html", flashPlayerConfig.getHtml().getBytes("UTF-8")));
                }
            }
            root.appendChild(DOMUtils.createBooleanElement(settings, "initialWizard", isInitialWizard()));
            FileOutputStream outputStream = null;
            try {
                File settingsFile = getSettingsFile();
                settingsFile.renameTo(new File(settingsFile.getParentFile(), settingsFile.getName() + ".bak"));
                outputStream = new FileOutputStream(settingsFile);
                DOMUtils.prettyPrint(settings, outputStream);
            } finally {
                IOUtils.close(outputStream);
            }
        } catch (Exception e) {
            LOGGER.error("Could not write settings file.", e);
        }
    }

    private void writeDataSources(Document settings, Element root) {
        Element dataSources = settings.createElement("datasources");
        root.appendChild(dataSources);
        for (int i = 0; i < myDatasources.size(); i++) {
            Element dataSource = settings.createElement("datasource");
            dataSources.appendChild(dataSource);
            dataSource.appendChild(DOMUtils.createTextElement(settings, "type", myDatasources.get(i).getType().name()));
            dataSource.appendChild(DOMUtils.createTextElement(settings, "definition", myDatasources.get(i).getDefinition()));
            switch (myDatasources.get(i).getType()) {
                case Watchfolder:
                    dataSource.appendChild(DOMUtils.createLongElement(settings, "minFileSize", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getMinFileSize()));
                    dataSource.appendChild(DOMUtils.createLongElement(settings, "maxFileSize", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getMaxFileSize()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "include", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getIncludePattern()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "exclude", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getExcludePattern()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "artistFallback", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getArtistFallback()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "albumFallback", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getAlbumFallback()));
                    break;
                case Itunes:
                    ItunesDatasourceConfig itunesDatasourceConfig = (ItunesDatasourceConfig) myDatasources.get(i);
                    if (itunesDatasourceConfig.getPathReplacements() != null && !itunesDatasourceConfig.getPathReplacements().isEmpty()) {
                        Element pathReplacementsElement = settings.createElement("path-replacements");
                        dataSource.appendChild(pathReplacementsElement);
                        for (PathReplacement pathReplacement : itunesDatasourceConfig.getPathReplacements()) {
                            Element pathReplacementElement = settings.createElement("replacement");
                            pathReplacementsElement.appendChild(pathReplacementElement);
                            pathReplacementElement.appendChild(DOMUtils.createTextElement(settings, "search", pathReplacement.getSearchPattern()));
                            pathReplacementElement.appendChild(DOMUtils.createTextElement(settings, "replacement", pathReplacement.getReplacement()));
                        }
                    }
                    if (itunesDatasourceConfig.getIgnorePlaylists() != null && !itunesDatasourceConfig.getIgnorePlaylists().isEmpty()) {
                        Element ignorePlaylistsElement = settings.createElement("ignore-playlists");
                        dataSource.appendChild(ignorePlaylistsElement);
                        for (ItunesPlaylistType type : itunesDatasourceConfig.getIgnorePlaylists()) {
                            ignorePlaylistsElement.appendChild(DOMUtils.createTextElement(settings, "type", type.name()));
                        }
                    }
                    dataSource.appendChild(DOMUtils.createBooleanElement(settings, "deleteMissingFiles", itunesDatasourceConfig.isDeleteMissingFiles()));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown datasource type!");
            }
        }
    }

    private void migrate() {
        Version current = new Version(getVersion());
        if (current.compareTo(new Version("3.6")) < 0) {
            setVersion("3.6");
        }
    }

    public boolean isDefaultDatabase() {
        return StringUtils.isEmpty(myDatabaseType) || "h2".equalsIgnoreCase(myDatabaseType);
    }

    public boolean isRemoteControl() {
        return MyTunesRss.QUICKTIME_PLAYER != null;
    }

    public boolean isMyTunesRssComActive() {
        return StringUtils.isNotEmpty(myMyTunesRssComUser) && myMyTunesRssComPasswordHash != null && myMyTunesRssComPasswordHash.length > 0;
    }

    public boolean isValidMailConfig() {
        return StringUtils.isNotEmpty(getMailHost()) && StringUtils.isNotEmpty(getMailSender());
    }

    public boolean isShowInitialWizard() {
        return isInitialWizard() && getUsers().isEmpty() && getDatasources().isEmpty();
    }
}