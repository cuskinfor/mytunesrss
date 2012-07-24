/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.Version;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * de.codewave.mytunesrss.config.MyTunesRssConfig
 */
public class MyTunesRssConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssConfig.class);
    private static final SecretKeySpec CHECKSUM_KEY = new SecretKeySpec("codewave".getBytes(), "DES");
    private static final String CREATION_TIME_KEY = "playmode";

    private String myHost;
    private int myPort;
    private String myServerName = "MyTunesRSS";
    private boolean myAvailableOnLocalNet = true;
    private List<DatasourceConfig> myDatasources = new ArrayList<DatasourceConfig>();
    private boolean myCheckUpdateOnStart = true;
    private String myVersion;
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
    private boolean myIgnoreArtwork;
    private Level myCodewaveLogLevel;
    private String myLastNewVersionInfo;
    private boolean myDeleteDatabaseOnExit;
    private String myUpdateIgnoreVersion;
    private List<String> myDatabaseUpdateTriggers = new ArrayList<String>();
    private List<String> myDatabaseBackupTriggers = new ArrayList<String>();
    private DatabaseType myDatabaseType;
    private String myDatabaseConnection;
    private String myDatabaseUser;
    private String myDatabasePassword;
    private String myDatabaseDriver;
    private String myWebappContext;
    private String myId3v2TrackComment;
    private String myTomcatMaxThreads;
    private String myAjpHost;
    private int myTomcatAjpPort;
    private String mySslKeystoreFile;
    private String mySslKeystorePass;
    private String mySslHost;
    private int mySslPort;
    private String mySslKeystoreKeyAlias;
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
    private boolean myNotifyOnOutdatedItunesXml;
    private boolean myNotifyOnSkippedDatabaseUpdate;
    private int myStatisticKeepTime = 60;
    private String myCryptedCreationTime;
    private String myDisabledMp4Codecs;
    private List<TranscoderConfig> myTranscoderConfigs = new ArrayList<TranscoderConfig>();
    private List<ExternalSiteDefinition> myExternalSites = new ArrayList<ExternalSiteDefinition>();
    private String myAutoLogin;
    private boolean myDisableBrowser;
    private boolean myServerBrowserActive;
    private boolean myOpenIdActive;
    private boolean myDisableWebLogin;
    private LdapConfig myLdapConfig;
    private byte[] myAdminPasswordHash;
    private String myAdminHost;
    private int myAdminPort;
    private boolean myImportOriginalImageSize = false;
    private Set<FlashPlayerConfig> myFlashPlayers = new HashSet<FlashPlayerConfig>();
    private boolean myInitialWizard;
    private boolean myUpnpAdmin;
    private boolean myUpnpUserHttp;
    private boolean myUpnpUserHttps;
    private String mySelfRegisterTemplateUser;
    private boolean mySelfRegAdminEmail;
    private String myDefaultUserInterfaceTheme;
    private String myFacebookApiKey = "102138059883364";
    private int myNumberKeepDatabaseBackups;
    private boolean myBackupDatabaseAfterInit;
    private boolean myHeadless = false;
    private List<ReplacementRule> trackImageMappings = new ArrayList<ReplacementRule>();
    private File myVlcExecutable;
    private int myVlcSocketTimeout;
    private String myRssDescription;
    private int myVlcRaopVolume = 75;

    public List<DatasourceConfig> getDatasources() {
        return new ArrayList<DatasourceConfig>(myDatasources);
    }

    public void setDatasources(List<DatasourceConfig> datasources) {
        myDatasources = new ArrayList<DatasourceConfig>(datasources);
        Collections.sort(myDatasources);
    }

    public String getHost() {
        return myHost;
    }

    public void setHost(String host) {
        myHost = StringUtils.trimToNull(host);
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

    public synchronized Collection<User> getUsers() {
        Collection<User> users = new HashSet<User>();
        for (User user : myUsers) {
            users.add(user);
        }
        return users;
    }

    public synchronized User getUser(String name) {
        for (User user : getUsers()) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;
    }

    public synchronized void removeUser(User user) {
        myUsers.remove(user);
        for (User eachUser : myUsers) {
            if (eachUser.getParent() == user) {
                eachUser.setParent(null);
            }
        }
    }

    public synchronized boolean addUser(User user) {
        if (myUsers.contains(user)) {
            return false;
        }
        myUsers.add(user);
        return true;
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

    public List<String> getDatabaseUpdateTriggers() {
        return myDatabaseUpdateTriggers;
    }

    public void setDatabaseUpdateTriggers(List<String> databaseCronTriggers) {
        myDatabaseUpdateTriggers = databaseCronTriggers;
    }

    public List<String> getDatabaseBackupTriggers() {
        return myDatabaseBackupTriggers;
    }

    public void setDatabaseBackupTriggers(List<String> databaseBackupTriggers) {
        myDatabaseBackupTriggers = databaseBackupTriggers;
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

    public DatabaseType getDatabaseType() {
        return myDatabaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
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

    public String getAjpHost() {
        return myAjpHost;
    }

    public void setAjpHost(String ajpHost) {
        myAjpHost = StringUtils.trimToNull(ajpHost);
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

    public String getSslHost() {
        return mySslHost;
    }

    public void setSslHost(String sslHost) {
        mySslHost = StringUtils.trimToNull(sslHost);
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

    public boolean isNotifyOnOutdatedItunesXml() {
        return myNotifyOnOutdatedItunesXml;
    }

    public void setNotifyOnOutdatedItunesXml(boolean notifyOnOutdatedItunesXml) {
        myNotifyOnOutdatedItunesXml = notifyOnOutdatedItunesXml;
    }

    public boolean isNotifyOnSkippedDatabaseUpdate() {
        return myNotifyOnSkippedDatabaseUpdate;
    }

    public void setNotifyOnSkippedDatabaseUpdate(boolean notifyOnSkippedDatabaseUpdate) {
        myNotifyOnSkippedDatabaseUpdate = notifyOnSkippedDatabaseUpdate;
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

    public boolean isOpenIdActive() {
        return myOpenIdActive;
    }

    public void setOpenIdActive(boolean openIdActive) {
        myOpenIdActive = openIdActive;
    }

    public boolean isDisableWebLogin() {
        return myDisableWebLogin;
    }

    public void setDisableWebLogin(boolean disableWebLogin) {
        myDisableWebLogin = disableWebLogin;
    }

    public LdapConfig getLdapConfig() {
        return myLdapConfig;
    }

    public boolean isAdminPassword() {
        return !Arrays.equals(MyTunesRss.CONFIG.getAdminPasswordHash(), MyTunesRss.SHA1_DIGEST.digest(new byte[0]));
    }

    public byte[] getAdminPasswordHash() {
        return myAdminPasswordHash != null ? myAdminPasswordHash : MyTunesRss.SHA1_DIGEST.digest(MiscUtils.getUtf8Bytes(""));
    }

    public void setAdminPasswordHash(byte[] adminPasswordHash) {
        myAdminPasswordHash = adminPasswordHash;
    }

    public String getAdminHost() {
        return myAdminHost;
    }

    public void setAdminHost(String adminHost) {
        myAdminHost = StringUtils.trimToNull(adminHost);
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

    public boolean isFlashPlayer() {
        return myFlashPlayers != null && !myFlashPlayers.isEmpty();
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

    public FlashPlayerConfig removeFlashPlayer(String id) {
        FlashPlayerConfig config = getFlashPlayer(id);
        if (config != null) {
            myFlashPlayers.remove(new FlashPlayerConfig(id, null, null, null, 0, 0, TimeUnit.SECONDS));
        }
        return config;
    }

    public boolean isInitialWizard() {
        return myInitialWizard;
    }

    public void setInitialWizard(boolean initialWizard) {
        myInitialWizard = initialWizard;
    }

    public boolean isUpnpAdmin() {
        return myUpnpAdmin;
    }

    public void setUpnpAdmin(boolean upnpAdmin) {
        myUpnpAdmin = upnpAdmin;
    }

    public boolean isUpnpUserHttp() {
        return myUpnpUserHttp;
    }

    public void setUpnpUserHttp(boolean upnpUserHttp) {
        myUpnpUserHttp = upnpUserHttp;
    }

    public boolean isUpnpUserHttps() {
        return myUpnpUserHttps;
    }

    public void setUpnpUserHttps(boolean upnpUserHttps) {
        myUpnpUserHttps = upnpUserHttps;
    }

    public String getSelfRegisterTemplateUser() {
        return mySelfRegisterTemplateUser;
    }

    public void setSelfRegisterTemplateUser(String selfRegisterTemplateUser) {
        mySelfRegisterTemplateUser = selfRegisterTemplateUser;
    }

    public boolean isSelfRegAdminEmail() {
        return mySelfRegAdminEmail;
    }

    public void setSelfRegAdminEmail(boolean selfRegAdminEmail) {
        mySelfRegAdminEmail = selfRegAdminEmail;
    }

    public String getDefaultUserInterfaceTheme() {
        return myDefaultUserInterfaceTheme;
    }

    public void setDefaultUserInterfaceTheme(String defaultUserInterfaceTheme) {
        myDefaultUserInterfaceTheme = defaultUserInterfaceTheme;
    }

    public String getFacebookApiKey() {
        return myFacebookApiKey;
    }

    public void setFacebookApiKey(String facebookApiKey) {
        myFacebookApiKey = facebookApiKey;
    }

    public int getNumberKeepDatabaseBackups() {
        return myNumberKeepDatabaseBackups;
    }

    public void setNumberKeepDatabaseBackups(int numberKeepDatabaseBackups) {
        myNumberKeepDatabaseBackups = numberKeepDatabaseBackups;
    }

    public boolean isBackupDatabaseAfterInit() {
        return myBackupDatabaseAfterInit;
    }

    public void setBackupDatabaseAfterInit(boolean backupDatabaseAfterInit) {
        myBackupDatabaseAfterInit = backupDatabaseAfterInit;
    }

    public boolean isHeadless() {
        return myHeadless;
    }

    public void setHeadless(boolean headless) {
        myHeadless = headless;
    }

    public List<ReplacementRule> getTrackImageMappings() {
        return new ArrayList<ReplacementRule>(trackImageMappings);
    }

    public void setTrackImageMappings(List<ReplacementRule> trackImageMappings) {
        this.trackImageMappings = new ArrayList<ReplacementRule>(trackImageMappings);
    }

    public File getVlcExecutable() {
        return myVlcExecutable;
    }

    public void setVlcExecutable(File vlcExecutable) {
        myVlcExecutable = vlcExecutable;
    }

    public int getVlcSocketTimeout() {
        return myVlcSocketTimeout;
    }

    public void setVlcSocketTimeout(int vlcSocketTimeout) {
        myVlcSocketTimeout = vlcSocketTimeout;
    }

    public int getVlcRaopVolume() {
        return myVlcRaopVolume;
    }

    public void setVlcRaopVolume(int vlcRaopVolume) {
        myVlcRaopVolume = vlcRaopVolume;
    }

    public String getRssDescription() {
        return myRssDescription;
    }

    public void setRssDescription(String rssDescription) {
        myRssDescription = rssDescription;
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
            loadFromContext(settings);
            if (currentConfigVersion.compareTo(currentAppVersion) < 0) {
                migrate(currentConfigVersion);
            }
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
        setAdminHost(JXPathUtils.getStringValue(settings, "adminHost", getAdminHost()));
        setAdminPort(JXPathUtils.getIntValue(settings, "adminPort", getAdminPort()));
        setImportOriginalImageSize(JXPathUtils.getBooleanValue(settings, "importOriginalImageSize", isImportOriginalImageSize()));
        setHost(JXPathUtils.getStringValue(settings, "serverHost", getHost()));
        setPort(JXPathUtils.getIntValue(settings, "serverPort", getPort()));
        setServerName(JXPathUtils.getStringValue(settings, "serverName", getServerName()));
        setAvailableOnLocalNet(JXPathUtils.getBooleanValue(settings, "availableOnLocalNet", isAvailableOnLocalNet()));
        setCheckUpdateOnStart(JXPathUtils.getBooleanValue(settings, "checkUpdateOnStart", isCheckUpdateOnStart()));
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
        setIgnoreArtwork(JXPathUtils.getBooleanValue(settings, "ignoreArtwork", false));
        setCodewaveLogLevel(Level.toLevel(JXPathUtils.getStringValue(settings, "codewaveLogLevel", Level.INFO.toString()).toUpperCase()));
        setLastNewVersionInfo(JXPathUtils.getStringValue(settings, "lastNewVersionInfo", "0"));
        setUpdateIgnoreVersion(JXPathUtils.getStringValue(settings, "updateIgnoreVersion", MyTunesRss.VERSION));
        Iterator<JXPathContext> updateTriggerIterator = JXPathUtils.getContextIterator(settings, "crontriggers/database");
        myDatabaseUpdateTriggers = new ArrayList<String>();
        while (updateTriggerIterator.hasNext()) {
            myDatabaseUpdateTriggers.add(JXPathUtils.getStringValue(updateTriggerIterator.next(), ".", ""));
        }
        Iterator<JXPathContext> backupTriggerIterator = JXPathUtils.getContextIterator(settings, "crontriggers/database-backup");
        myDatabaseBackupTriggers = new ArrayList<String>();
        while (backupTriggerIterator.hasNext()) {
            myDatabaseBackupTriggers.add(JXPathUtils.getStringValue(backupTriggerIterator.next(), ".", ""));
        }
        loadDatabaseSettings(settings);
        setId3v2TrackComment(JXPathUtils.getStringValue(settings, "id3v2-track-comment", ""));
        setTomcatMaxThreads(JXPathUtils.getStringValue(settings, "tomcat/max-threads", "200"));
        setAjpHost(JXPathUtils.getStringValue(settings, "tomcat/ajp-host", getAjpHost()));
        setTomcatAjpPort(JXPathUtils.getIntValue(settings, "tomcat/ajp-port", 0));
        String context = StringUtils.trimToNull(StringUtils.strip(JXPathUtils.getStringValue(settings, "tomcat/webapp-context", ""), "/"));
        setWebappContext(context != null ? "/" + context : "");
        setSslKeystoreFile(JXPathUtils.getStringValue(settings, "ssl/keystore/file", null));
        setSslKeystoreKeyAlias(JXPathUtils.getStringValue(settings, "ssl/keystore/keyalias", null));
        setSslKeystorePass(JXPathUtils.getStringValue(settings, "ssl/keystore/pass", null));
        setSslHost(JXPathUtils.getStringValue(settings, "ssl/host", getSslHost()));
        setSslPort(JXPathUtils.getIntValue(settings, "ssl/port", 0));
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
        setNotifyOnOutdatedItunesXml(JXPathUtils.getBooleanValue(settings, "admin-notify/outdated-itunesxml", false));
        setNotifyOnSkippedDatabaseUpdate(JXPathUtils.getBooleanValue(settings, "admin-notify/skipped-db-update", false));
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
        if (myTranscoderConfigs.isEmpty()) {
            myTranscoderConfigs.addAll(TranscoderConfig.DEFAULT_TRANSCODERS);
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
        setOpenIdActive(JXPathUtils.getBooleanValue(settings, "openIdActive", true));
        setAutoLogin(JXPathUtils.getStringValue(settings, "autoLogin", null));
        setDisableBrowser(JXPathUtils.getBooleanValue(settings, "disableBrowser", false));
        setDisableWebLogin(JXPathUtils.getBooleanValue(settings, "disableWebLogin", false));
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
                    new String(JXPathUtils.getByteArray(flashPlayerContext, "html", FlashPlayerConfig.DEFAULT_HTML.getBytes("UTF-8")), "UTF-8"),
                    PlaylistFileType.valueOf(JXPathUtils.getStringValue(flashPlayerContext, "filetype", PlaylistFileType.Xspf.name())),
                    JXPathUtils.getIntValue(flashPlayerContext, "width", 600),
                    JXPathUtils.getIntValue(flashPlayerContext, "height", 276),
                    TimeUnit.valueOf(JXPathUtils.getStringValue(flashPlayerContext, "timeunit", TimeUnit.SECONDS.name()))
            );
            addFlashPlayer(flashPlayerConfig);
        }
        setInitialWizard(JXPathUtils.getBooleanValue(settings, "initialWizard", true));
        setUpnpAdmin(JXPathUtils.getBooleanValue(settings, "upnp-admin", false));
        setUpnpUserHttp(JXPathUtils.getBooleanValue(settings, "upnp-user-http", true));
        setUpnpUserHttps(JXPathUtils.getBooleanValue(settings, "upnp-user-https", true));
        setSelfRegisterTemplateUser(JXPathUtils.getStringValue(settings, "selfreg-template-user", null));
        setSelfRegAdminEmail(JXPathUtils.getBooleanValue(settings, "selfreg-admin-email", true));
        setDefaultUserInterfaceTheme(JXPathUtils.getStringValue(settings, "default-ui-theme", null));
        //setFacebookApiKey(JXPathUtils.getStringValue(settings, "facebook-api-key", null));
        setNumberKeepDatabaseBackups(JXPathUtils.getIntValue(settings, "database-backup-max", 5));
        setBackupDatabaseAfterInit(JXPathUtils.getBooleanValue(settings, "database-backup-after-init", true));
        setHeadless(JXPathUtils.getBooleanValue(settings, "headless", false));
        List<ReplacementRule> mappings = new ArrayList<ReplacementRule>();
        Iterator<JXPathContext> trackImageMappingIterator = JXPathUtils.getContextIterator(settings, "track-image-mappings/mapping");
        while (trackImageMappingIterator.hasNext()) {
            JXPathContext mappingContext = trackImageMappingIterator.next();
            mappings.add(new ReplacementRule(JXPathUtils.getStringValue(mappingContext, "search-pattern", null), JXPathUtils.getStringValue(mappingContext, "replacement", null)));
        }
        setTrackImageMappings(mappings);
        String vlc = JXPathUtils.getStringValue(settings, "vlc", MyTunesRssUtils.findVlcExecutable());
        setVlcExecutable(vlc != null ? new File(vlc) : null);
        setVlcSocketTimeout(JXPathUtils.getIntValue(settings, "vlc-timeout", 100));
        setVlcRaopVolume(JXPathUtils.getIntValue(settings, "vlc-raop-volume", 75));
        setRssDescription(JXPathUtils.getStringValue(settings, "rss-description", "Visit http://www.codewave.de for more information."));
    }

    /**
     * Mark all groups users as groups
     */
    private synchronized void markGroupUsers() {
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
                    dataSources.add(DatasourceConfig.create(UUID.randomUUID().toString(), definition));
                }
            } else {
                try {
                    DatasourceType type = DatasourceType.valueOf(JXPathUtils.getStringValue(datasourceContext, "type", DatasourceType.Itunes.name()));
                    String id = JXPathUtils.getStringValue(datasourceContext, "id", UUID.randomUUID().toString());
                    String definition = JXPathUtils.getStringValue(datasourceContext, "definition", "");
                    switch (type) {
                        case Watchfolder:
                            WatchfolderDatasourceConfig watchfolderDatasourceConfig = new WatchfolderDatasourceConfig(id, definition);
                            watchfolderDatasourceConfig.setMinFileSize(JXPathUtils.getLongValue(datasourceContext, "minFileSize", 0));
                            watchfolderDatasourceConfig.setMaxFileSize(JXPathUtils.getLongValue(datasourceContext, "maxFileSize", 0));
                            watchfolderDatasourceConfig.setIncludePattern(JXPathUtils.getStringValue(datasourceContext, "include", null));
                            watchfolderDatasourceConfig.setExcludePattern(JXPathUtils.getStringValue(datasourceContext, "exclude", null));
                            watchfolderDatasourceConfig.setTitleFallback(JXPathUtils.getStringValue(datasourceContext, "titleFallback", WatchfolderDatasourceConfig.DEFAULT_TITLE_FALLBACK));
                            watchfolderDatasourceConfig.setAlbumFallback(JXPathUtils.getStringValue(datasourceContext, "albumFallback", WatchfolderDatasourceConfig.DEFAULT_ALBUM_FALLBACK));
                            watchfolderDatasourceConfig.setArtistFallback(JXPathUtils.getStringValue(datasourceContext, "artistFallback", WatchfolderDatasourceConfig.DEFAULT_ARTIST_FALLBACK));
                            watchfolderDatasourceConfig.setSeriesFallback(JXPathUtils.getStringValue(datasourceContext, "seriesFallback", WatchfolderDatasourceConfig.DEFAULT_SERIES_FALLBACK));
                            watchfolderDatasourceConfig.setSeasonFallback(JXPathUtils.getStringValue(datasourceContext, "seasonFallback", WatchfolderDatasourceConfig.DEFAULT_SEASON_FALLBACK));
                            watchfolderDatasourceConfig.setEpisodeFallback(JXPathUtils.getStringValue(datasourceContext, "episodeFallback", WatchfolderDatasourceConfig.DEFAULT_EPISODE_FALLBACK));
                            watchfolderDatasourceConfig.setVideoType(VideoType.valueOf(JXPathUtils.getStringValue(datasourceContext, "videoType", VideoType.Movie.name())));
                            watchfolderDatasourceConfig.setPhotoAlbumPattern(JXPathUtils.getStringValue(datasourceContext, "photoAlbumPattern", WatchfolderDatasourceConfig.DEFAULT_PHOTO_ALBUM_PATTERN));
                            watchfolderDatasourceConfig.setIgnoreFileMeta(JXPathUtils.getBooleanValue(datasourceContext, "ignoreFileMeta", false));
                            dataSources.add(watchfolderDatasourceConfig);
                            break;
                        case Itunes:
                            ItunesDatasourceConfig itunesDatasourceConfig = new ItunesDatasourceConfig(id, definition);
                            Iterator<JXPathContext> pathReplacementsIterator = JXPathUtils.getContextIterator(datasourceContext, "path-replacements/replacement");
                            itunesDatasourceConfig.clearPathReplacements();
                            while (pathReplacementsIterator.hasNext()) {
                                JXPathContext pathReplacementContext = pathReplacementsIterator.next();
                                String search = JXPathUtils.getStringValue(pathReplacementContext, "search", null);
                                String replacement = JXPathUtils.getStringValue(pathReplacementContext, "replacement", null);
                                itunesDatasourceConfig.addPathReplacement(new ReplacementRule(search, replacement));
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
                        case Iphoto:
                            IphotoDatasourceConfig iphotoDatasourceConfig = new IphotoDatasourceConfig(id, definition);
                            pathReplacementsIterator = JXPathUtils.getContextIterator(datasourceContext, "path-replacements/replacement");
                            iphotoDatasourceConfig.clearPathReplacements();
                            while (pathReplacementsIterator.hasNext()) {
                                JXPathContext pathReplacementContext = pathReplacementsIterator.next();
                                String search = JXPathUtils.getStringValue(pathReplacementContext, "search", null);
                                String replacement = JXPathUtils.getStringValue(pathReplacementContext, "replacement", null);
                                iphotoDatasourceConfig.addPathReplacement(new ReplacementRule(search, replacement));
                            }
                            iphotoDatasourceConfig.setImportRolls(JXPathUtils.getBooleanValue(datasourceContext, "importRolls", true));
                            iphotoDatasourceConfig.setImportAlbums(JXPathUtils.getBooleanValue(datasourceContext, "importAlbums", true));
                            dataSources.add(iphotoDatasourceConfig);
                            break;
                        case Aperture:
                            ApertureDatasourceConfig apertureDatasourceConfig = new ApertureDatasourceConfig(id, definition);
                            pathReplacementsIterator = JXPathUtils.getContextIterator(datasourceContext, "path-replacements/replacement");
                            apertureDatasourceConfig.clearPathReplacements();
                            while (pathReplacementsIterator.hasNext()) {
                                JXPathContext pathReplacementContext = pathReplacementsIterator.next();
                                String search = JXPathUtils.getStringValue(pathReplacementContext, "search", null);
                                String replacement = JXPathUtils.getStringValue(pathReplacementContext, "replacement", null);
                                apertureDatasourceConfig.addPathReplacement(new ReplacementRule(search, replacement));
                            }
                            dataSources.add(apertureDatasourceConfig);
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
        setDatabaseType(DatabaseType.valueOf(JXPathUtils.getStringValue(settings, "database/type", getDatabaseType().name())));
        // for default h2, always use calculated defaults
        if (getDatabaseType() != DatabaseType.h2) {
            setDatabaseDriver(JXPathUtils.getStringValue(settings, "database/driver", getDatabaseDriver()));
            setDatabaseConnection(JXPathUtils.getStringValue(settings, "database/connection", getDatabaseConnection()));
            setDatabaseUser(JXPathUtils.getStringValue(settings, "database/user", getDatabaseUser()));
            setDatabasePassword(JXPathUtils.getStringValue(settings, "database/password", getDatabasePassword()));
        }
    }

    public void setDefaultDatabaseSettings() throws IOException {
        setDatabaseType(DatabaseType.h2);
        setDatabaseDriver("org.h2.Driver");
        setDatabaseConnection("jdbc:h2:file:" + MyTunesRss.CACHE_DATA_PATH + "/" + "h2/MyTunesRSS");
        setDatabaseUser("sa");
        setDatabasePassword("");
    }

    private static File getSettingsFile() throws IOException {
        String filename = "settings.xml";
        return new File(MyTunesRss.PREFERENCES_DATA_PATH + "/" + filename);
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

    public synchronized void save() {
        try {
            LOGGER.info("Saving configuration to \"" + getSettingsFile().getAbsolutePath() + "\".");
            Document settings = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = settings.createElement("settings");
            settings.appendChild(root);
            root.appendChild(DOMUtils.createByteArrayElement(settings, "adminPassword", getAdminPasswordHash()));
            root.appendChild(DOMUtils.createTextElement(settings, "adminHost", myAdminHost));
            root.appendChild(DOMUtils.createIntElement(settings, "adminPort", myAdminPort));
            root.appendChild(DOMUtils.createBooleanElement(settings, "importOriginalImageSize", myImportOriginalImageSize));
            root.appendChild(DOMUtils.createTextElement(settings, "version", myVersion));
            if (StringUtils.isNotBlank(myHost)) {
                root.appendChild(DOMUtils.createTextElement(settings, "serverHost", myHost));
            }
            root.appendChild(DOMUtils.createIntElement(settings, "serverPort", myPort));
            root.appendChild(DOMUtils.createTextElement(settings, "serverName", myServerName));
            root.appendChild(DOMUtils.createBooleanElement(settings, "availableOnLocalNet", myAvailableOnLocalNet));
            root.appendChild(DOMUtils.createBooleanElement(settings, "checkUpdateOnStart", myCheckUpdateOnStart));
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
            root.appendChild(DOMUtils.createBooleanElement(settings, "ignoreArtwork", myIgnoreArtwork));
            root.appendChild(DOMUtils.createTextElement(settings, "codewaveLogLevel", myCodewaveLogLevel.toString().toUpperCase()));
            root.appendChild(DOMUtils.createTextElement(settings, "lastNewVersionInfo", myLastNewVersionInfo));
            root.appendChild(DOMUtils.createTextElement(settings, "updateIgnoreVersion", myUpdateIgnoreVersion));
            if ((myDatabaseUpdateTriggers != null && myDatabaseUpdateTriggers.size() > 0) || (myDatabaseBackupTriggers != null && myDatabaseBackupTriggers.size() > 0)) {
                Element cronTriggers = settings.createElement("crontriggers");
                root.appendChild(cronTriggers);
                if (myDatabaseUpdateTriggers != null && myDatabaseUpdateTriggers.size() > 0) {
                    for (String trigger : myDatabaseUpdateTriggers) {
                        cronTriggers.appendChild(DOMUtils.createTextElement(settings, "database", trigger));
                    }
                }
                if (myDatabaseBackupTriggers != null && myDatabaseBackupTriggers.size() > 0) {
                    for (String trigger : myDatabaseBackupTriggers) {
                        cronTriggers.appendChild(DOMUtils.createTextElement(settings, "database-backup", trigger));
                    }
                }
            }
            // for default h2 database we shoud not save anything to the config
            if (getDatabaseType() != DatabaseType.h2) {
                Element database = settings.createElement("database");
                root.appendChild(database);
                database.appendChild(DOMUtils.createTextElement(settings, "type", getDatabaseType().name()));
                database.appendChild(DOMUtils.createTextElement(settings, "driver", getDatabaseDriver()));
                database.appendChild(DOMUtils.createTextElement(settings, "connection", getDatabaseConnection()));
                database.appendChild(DOMUtils.createTextElement(settings, "user", getDatabaseUser()));
                database.appendChild(DOMUtils.createTextElement(settings, "password", getDatabasePassword()));
            }
            root.appendChild(DOMUtils.createTextElement(settings, "id3v2-track-comment", getId3v2TrackComment()));
            Element tomcat = settings.createElement("tomcat");
            root.appendChild(tomcat);
            tomcat.appendChild(DOMUtils.createTextElement(settings, "max-threads", getTomcatMaxThreads()));
            if (StringUtils.isNotBlank(getAjpHost())) {
                tomcat.appendChild(DOMUtils.createTextElement(settings, "ajp-host", getAjpHost()));
            }
            if (getTomcatAjpPort() > 0) {
                tomcat.appendChild(DOMUtils.createIntElement(settings, "ajp-port", getTomcatAjpPort()));
            }
            tomcat.appendChild(DOMUtils.createTextElement(settings, "webapp-context", getWebappContext()));
            Element ssl = settings.createElement("ssl");
            root.appendChild(ssl);
            if (StringUtils.isNotBlank(getSslHost())) {
                ssl.appendChild(DOMUtils.createTextElement(settings, "host", getSslHost()));
            }
            ssl.appendChild(DOMUtils.createIntElement(settings, "port", getSslPort()));
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
            notify.appendChild(DOMUtils.createBooleanElement(settings, "outdated-itunesxml", isNotifyOnOutdatedItunesXml()));
            notify.appendChild(DOMUtils.createBooleanElement(settings, "skipped-db-update", isNotifyOnSkippedDatabaseUpdate()));
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
            root.appendChild(DOMUtils.createBooleanElement(settings, "openIdActive", isOpenIdActive()));
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
                    player.appendChild(DOMUtils.createTextElement(settings, "filetype", flashPlayerConfig.getPlaylistFileType().name()));
                    player.appendChild(DOMUtils.createTextElement(settings, "timeunit", flashPlayerConfig.getTimeUnit().name()));
                    player.appendChild(DOMUtils.createIntElement(settings, "width", flashPlayerConfig.getWidth()));
                    player.appendChild(DOMUtils.createIntElement(settings, "height", flashPlayerConfig.getHeight()));
                }
            }
            root.appendChild(DOMUtils.createBooleanElement(settings, "initialWizard", isInitialWizard()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "upnp-admin", isUpnpAdmin()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "upnp-user-http", isUpnpUserHttp()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "upnp-user-https", isUpnpUserHttps()));
            root.appendChild(DOMUtils.createTextElement(settings, "selfreg-template-user", getSelfRegisterTemplateUser()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "selfreg-admin-email", isSelfRegAdminEmail()));
            root.appendChild(DOMUtils.createTextElement(settings, "default-ui-theme", getDefaultUserInterfaceTheme()));
            //root.appendChild(DOMUtils.createTextElement(settings, "facebook-api-key", getFacebookApiKey()));
            root.appendChild(DOMUtils.createIntElement(settings, "database-backup-max", getNumberKeepDatabaseBackups()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "database-backup-after-init", isBackupDatabaseAfterInit()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "headless", isHeadless()));
            if (!getTrackImageMappings().isEmpty()) {
                Element trackImageMappingsElement = settings.createElement("track-image-mappings");
                root.appendChild(trackImageMappingsElement);
                for (ReplacementRule rule : trackImageMappings) {
                    Element mappingElement = settings.createElement("mapping");
                    trackImageMappingsElement.appendChild(mappingElement);
                    mappingElement.appendChild(DOMUtils.createTextElement(settings, "search-pattern", rule.getSearchPattern()));
                    mappingElement.appendChild(DOMUtils.createTextElement(settings, "replacement", rule.getReplacement()));
                }
            }
            if (getVlcExecutable() != null) {
                root.appendChild(DOMUtils.createTextElement(settings, "vlc", getVlcExecutable().getAbsolutePath()));
                root.appendChild(DOMUtils.createIntElement(settings, "vlc-timeout", getVlcSocketTimeout()));
                root.appendChild(DOMUtils.createIntElement(settings, "vlc-raop-volume", getVlcRaopVolume()));
            }
            root.appendChild(DOMUtils.createTextElement(settings, "rss-description", getRssDescription()));
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
            dataSource.appendChild(DOMUtils.createTextElement(settings, "id", myDatasources.get(i).getId()));
            switch (myDatasources.get(i).getType()) {
                case Watchfolder:
                    dataSource.appendChild(DOMUtils.createLongElement(settings, "minFileSize", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getMinFileSize()));
                    dataSource.appendChild(DOMUtils.createLongElement(settings, "maxFileSize", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getMaxFileSize()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "include", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getIncludePattern()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "exclude", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getExcludePattern()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "artistFallback", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getArtistFallback()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "titleFallback", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getTitleFallback()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "albumFallback", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getAlbumFallback()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "seriesFallback", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getSeriesFallback()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "seasonFallback", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getSeasonFallback()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "episodeFallback", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getEpisodeFallback()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "videoType", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getVideoType().name()));
                    dataSource.appendChild(DOMUtils.createTextElement(settings, "photoAlbumPattern", ((WatchfolderDatasourceConfig) myDatasources.get(i)).getPhotoAlbumPattern()));
                    dataSource.appendChild(DOMUtils.createBooleanElement(settings, "ignoreFileMeta", ((WatchfolderDatasourceConfig) myDatasources.get(i)).isIgnoreFileMeta()));
                    break;
                case Itunes:
                    ItunesDatasourceConfig itunesDatasourceConfig = (ItunesDatasourceConfig) myDatasources.get(i);
                    if (itunesDatasourceConfig.getPathReplacements() != null && !itunesDatasourceConfig.getPathReplacements().isEmpty()) {
                        Element pathReplacementsElement = settings.createElement("path-replacements");
                        dataSource.appendChild(pathReplacementsElement);
                        for (ReplacementRule pathReplacement : itunesDatasourceConfig.getPathReplacements()) {
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
                case Iphoto:
                    IphotoDatasourceConfig iphotoDatasourceConfig = (IphotoDatasourceConfig) myDatasources.get(i);
                    if (iphotoDatasourceConfig.getPathReplacements() != null && !iphotoDatasourceConfig.getPathReplacements().isEmpty()) {
                        Element pathReplacementsElement = settings.createElement("path-replacements");
                        dataSource.appendChild(pathReplacementsElement);
                        for (ReplacementRule pathReplacement : iphotoDatasourceConfig.getPathReplacements()) {
                            Element pathReplacementElement = settings.createElement("replacement");
                            pathReplacementsElement.appendChild(pathReplacementElement);
                            pathReplacementElement.appendChild(DOMUtils.createTextElement(settings, "search", pathReplacement.getSearchPattern()));
                            pathReplacementElement.appendChild(DOMUtils.createTextElement(settings, "replacement", pathReplacement.getReplacement()));
                        }
                    }
                    dataSource.appendChild(DOMUtils.createBooleanElement(settings, "importRolls", iphotoDatasourceConfig.isImportRolls()));
                    dataSource.appendChild(DOMUtils.createBooleanElement(settings, "importAlbums", iphotoDatasourceConfig.isImportAlbums()));
                    break;
                case Aperture:
                    ApertureDatasourceConfig apertureDatasourceConfig = (ApertureDatasourceConfig) myDatasources.get(i);
                    if (apertureDatasourceConfig.getPathReplacements() != null && !apertureDatasourceConfig.getPathReplacements().isEmpty()) {
                        Element pathReplacementsElement = settings.createElement("path-replacements");
                        dataSource.appendChild(pathReplacementsElement);
                        for (ReplacementRule pathReplacement : apertureDatasourceConfig.getPathReplacements()) {
                            Element pathReplacementElement = settings.createElement("replacement");
                            pathReplacementsElement.appendChild(pathReplacementElement);
                            pathReplacementElement.appendChild(DOMUtils.createTextElement(settings, "search", pathReplacement.getSearchPattern()));
                            pathReplacementElement.appendChild(DOMUtils.createTextElement(settings, "replacement", pathReplacement.getReplacement()));
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown datasource type!");
            }
        }
    }

    private void migrate(Version current) {
        if (current.compareTo(new Version("4.1.0")) < 0) {
            setVersion("4.1.0");
            for (DatasourceConfig dc : getDatasources()) {
                if (dc.getType() == DatasourceType.Watchfolder) {
                    WatchfolderDatasourceConfig wdc = (WatchfolderDatasourceConfig) dc;
                    wdc.setAlbumFallback(migrateFallback(wdc.getAlbumFallback()));
                    wdc.setArtistFallback(migrateFallback(wdc.getArtistFallback()));
                }
            }
        }
        if (current.compareTo(new Version("4.3.0")) < 0) {
            setVersion("4.3.0");
            myTranscoderConfigs = new ArrayList<TranscoderConfig>(TranscoderConfig.DEFAULT_TRANSCODERS);
            myFlashPlayers.addAll(FlashPlayerConfig.getDefaults());
        }
    }

    private String migrateFallback(String fallback) {
        for (String type : new String[] {"dir", "file"}) {
            for (String token : StringUtils.substringsBetween(fallback, "[" + type + ":", "]")) {
                fallback = fallback.replace("[" + type + ":" + token + "]","[[[dir:" + token + "]]]");
            }
        }
        return fallback;
    }

    public boolean isDefaultDatabase() {
        return getDatabaseType() == null || getDatabaseType() == DatabaseType.h2;
    }

    public boolean isRemoteControl() {
        return isVlc(getVlcExecutable(), false);
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

    public static boolean isVlc(final File executable, boolean checkOutput) {
        if (executable == null || (executable.isFile() && "vlc".equalsIgnoreCase(FilenameUtils.getBaseName(executable.getName())))) {
            if (checkOutput) {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(executable.getAbsolutePath(), "--version");
                    processBuilder.redirectErrorStream(true);
                    final Process process = processBuilder.start();
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        Thread checkThread = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    org.apache.commons.io.IOUtils.copy(process.getInputStream(), baos);
                                } catch (IOException e) {
                                    LOGGER.info("Could not copy process stream.", e);
                                }
                            }
                        });
                        checkThread.start();
                        try {
                            checkThread.join(3000);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                        return StringUtils.containsIgnoreCase(org.apache.commons.io.IOUtils.toString(new ByteArrayInputStream(baos.toByteArray())), "vlc");
                    } finally {
                        process.destroy();
                    }
                } catch (IOException e) {
                    LOGGER.info("Could not start process.", e);
                }
            } else {
                return true;
            }
        }
        return false;
    }
}