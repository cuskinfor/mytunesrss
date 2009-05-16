/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.DialogLayout;
import de.codewave.utils.PrefsUtils;
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
    private List<String> myDatasources = new ArrayList<String>();
    private boolean myCheckUpdateOnStart = true;
    private boolean myAutoStartServer;
    private String myVersion;
    private boolean myIgnoreTimestamps;
    private int myFileSystemAlbumNameFolder = 1;
    private int myFileSystemArtistNameFolder = 2;
    private Collection<User> myUsers = new HashSet<User>();
    private String mySupportName = "";
    private String mySupportEmail = "";
    private boolean myProxyServer;
    private String myProxyHost = "";
    private int myProxyPort = -1;
    private boolean myItunesDeleteMissingFiles = true;
    private String myUploadDir = "";
    private boolean myUploadCreateUserDir = true;
    private String myMyTunesRssComUser = "";
    private byte[] myMyTunesRssComPasswordHash = null;
    private boolean myMyTunesRssComSsl = false;
    private boolean myUpdateDatabaseOnServerStart = true;
    private String myArtistDropWords = "";
    private boolean myLocalTempArchive;
    private boolean myQuitConfirmation;
    private SecretKey myPathInfoKey;
    private String myWebWelcomeMessage = "";
    private String myLameBinary = "";
    private String myFaadBinary = "";
    private String myAlacBinary = "";
    private int myStreamingCacheTimeout = 20;
    private int myStreamingCacheMaxFiles = 300;
    private boolean myBandwidthLimit;
    private BigDecimal myBandwidthLimitFactor;
    private boolean myIgnoreArtwork;
    private Level myCodewaveLogLevel;
    private int myWindowX;
    private int myWindowY;
    private String myLastNewVersionInfo;
    private boolean myDeleteDatabaseOnNextStartOnError;
    private String myUpdateIgnoreVersion;
    private List<String> myDatabaseCronTriggers = new ArrayList<String>();
    private String myDatabaseType;
    private String myDatabaseConnection;
    private String myDatabaseUser;
    private String myDatabasePassword;
    private String myDatabaseDriver;
    private String myWebappContext;
    private String myId3v2TrackComment;
    private String myJmxHost;
    private int myJmxPort;
    private String myJmxUser;
    private String myJmxPassword;
    private String myTomcatMaxThreads;
    private int myTomcatAjpPort;
    private String mySslKeystoreFile;
    private String mySslKeystorePass;
    private int mySslPort;
    private String mySslKeystoreKeyAlias;
    private List<String> myAdditionalContexts;
    private String myTomcatProxyHost;
    private int myTomcatProxyPort;
    private String myTomcatProxyScheme;
    private String myTomcatSslProxyHost;
    private int myTomcatSslProxyPort;
    private String myTomcatSslProxyScheme;
    private Map<String, DialogLayout> myDialogLayouts;
    private String myLameOnlyOptions = "--quiet -b {bitrate} --resample {samplerate} {infile} -";
    private String myLameTargetOptions = "--quiet -r -b {bitrate} --resample {samplerate} - -";
    private String myAlacSourceOptions = "{infile}";
    private String myFaadSourceOptions = "-f 2 -g -w {infile}";
    private List<FileType> myFileTypes = new ArrayList<FileType>();
    private String myMailHost;
    private int myMailPort;
    private boolean myMailTls;
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

    public String[] getDatasources() {
        return myDatasources.toArray(new String[myDatasources.size()]);
    }

    public void setDatasources(String[] datasources) {
        myDatasources = new ArrayList<String>();
        for (String datasource : datasources) {
            if (StringUtils.isNotBlank(datasource)) {
                myDatasources.add(datasource.trim());
            }
        }
        Collections.sort(myDatasources);
    }

    public String addDatasource(String datasource) {
        if (new File(datasource).exists()) {
            for (String eachDatasource : myDatasources) {
                try {
                    if (datasource.equals(eachDatasource)) {
                        return MyTunesRssUtils.getBundleString("error.watchFolderAlreadyExists", eachDatasource);
                    } else if (IOUtils.isContained(new File(eachDatasource), new File(datasource))) {
                        return MyTunesRssUtils.getBundleString("error.existingWatchFolderContainsNewOne", eachDatasource);
                    } else if (IOUtils.isContained(new File(datasource), new File(eachDatasource))) {
                        return MyTunesRssUtils.getBundleString("error.newWatchFolderContainsExistingOne", eachDatasource);
                    }
                } catch (IOException e) {
                    LOGGER.error("Could not check if existing datasource contains new datasource or vice versa, assuming everything is okay.", e);
                }
            }
            myDatasources.add(datasource);
            Collections.sort(myDatasources);
            return null;
        }
        return MyTunesRssUtils.getBundleString("error.datasourceDoesNotExist");
    }

    public String removeDatasource(String datasource) {
        if (myDatasources.contains(datasource)) {
            myDatasources.remove(datasource);
            return null;
        }
        return MyTunesRssUtils.getBundleString("error.datasourceDoesNotExist");
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

    public boolean isAutoStartServer() {
        return myAutoStartServer;
    }

    public void setAutoStartServer(boolean autoStartServer) {
        myAutoStartServer = autoStartServer;
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

    public int getFileSystemAlbumNameFolder() {
        return myFileSystemAlbumNameFolder;
    }

    public void setFileSystemAlbumNameFolder(int fileSystemAlbumNameFolder) {
        myFileSystemAlbumNameFolder = fileSystemAlbumNameFolder;
    }

    public int getFileSystemArtistNameFolder() {
        return myFileSystemArtistNameFolder;
    }

    public void setFileSystemArtistNameFolder(int fileSystemArtistNameFolder) {
        myFileSystemArtistNameFolder = fileSystemArtistNameFolder;
    }

    public boolean isItunesDeleteMissingFiles() {
        return myItunesDeleteMissingFiles;
    }

    public void setItunesDeleteMissingFiles(boolean itunesDeleteMissingFiles) {
        myItunesDeleteMissingFiles = itunesDeleteMissingFiles;
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

    public boolean isQuitConfirmation() {
        return myQuitConfirmation;
    }

    public void setQuitConfirmation(boolean quitConfirmation) {
        myQuitConfirmation = quitConfirmation;
    }

    public SecretKey getPathInfoKey() {
        return myPathInfoKey;
    }

    public String getLameBinary() {
        return myLameBinary;
    }

    public boolean isValidLameBinary() {
        return StringUtils.isNotEmpty(myLameBinary) && new File(myLameBinary).isFile();
    }

    public void setLameBinary(String lameBinary) {
        myLameBinary = lameBinary;
    }

    public String getFaadBinary() {
        return myFaadBinary;
    }

    public boolean isValidFaadBinary() {
        return StringUtils.isNotEmpty(myFaadBinary) && new File(myFaadBinary).isFile();
    }

    public void setFaadBinary(String faadBinary) {
        myFaadBinary = faadBinary;
    }

    public String getAlacBinary() {
        return myAlacBinary;
    }

    public boolean isValidAlacBinary() {
        return StringUtils.isNotEmpty(myAlacBinary) && new File(myAlacBinary).isFile();
    }


    public void setAlacBinary(String alacBinary) {
        myAlacBinary = alacBinary;
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
        return new HashSet<User>(myUsers);
    }

    public User getUser(String name) {
        for (User user : getUsers()) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public void addUser(User user) {
        myUsers.remove(user);
        myUsers.add(user);
    }

    public void removeUser(String userName) {
        myUsers.remove(new User(userName));
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
        return myProxyServer;
    }

    public void setProxyServer(boolean proxyServer) {
        myProxyServer = proxyServer;
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

    public boolean isUpdateDatabaseOnServerStart() {
        return myUpdateDatabaseOnServerStart;
    }

    public void setUpdateDatabaseOnServerStart(boolean updateOnServerStart) {
        myUpdateDatabaseOnServerStart = updateOnServerStart;
    }

    public String getWebWelcomeMessage() {
        return myWebWelcomeMessage;
    }

    public void setWebWelcomeMessage(String webWelcomeMessage) {
        myWebWelcomeMessage = webWelcomeMessage;
    }

    public int getWindowX() {
        return myWindowX;
    }

    public void setWindowX(int windowX) {
        myWindowX = windowX;
    }

    public int getWindowY() {
        return myWindowY;
    }

    public void setWindowY(int windowY) {
        myWindowY = windowY;
    }

    public String getLastNewVersionInfo() {
        return myLastNewVersionInfo;
    }

    public void setLastNewVersionInfo(String lastNewVersionInfo) {
        myLastNewVersionInfo = lastNewVersionInfo;
    }

    public boolean isDeleteDatabaseOnNextStartOnError() {
        return myDeleteDatabaseOnNextStartOnError;
    }

    public void setDeleteDatabaseOnNextStartOnError(boolean deleteDatabaseOnNextStartOnError) {
        myDeleteDatabaseOnNextStartOnError = deleteDatabaseOnNextStartOnError;
    }

    public String getUpdateIgnoreVersion() {
        return myUpdateIgnoreVersion;
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

    public String getJmxHost() {
        return myJmxHost;
    }

    public void setJmxHost(String jmxHost) {
        myJmxHost = jmxHost;
    }

    public String getJmxPassword() {
        return myJmxPassword;
    }

    public void setJmxPassword(String jmxPassword) {
        myJmxPassword = jmxPassword;
    }

    public int getJmxPort() {
        return myJmxPort;
    }

    public void setJmxPort(int jmxPort) {
        myJmxPort = jmxPort;
    }

    public String getJmxUser() {
        return myJmxUser;
    }

    public void setJmxUser(String jmxUser) {
        myJmxUser = jmxUser;
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

    public List<String> getAdditionalContexts() {
        return myAdditionalContexts;
    }

    public void setAdditionalContexts(List<String> additionalContexts) {
        myAdditionalContexts = additionalContexts;
    }

    public DialogLayout getDialogLayout(Class clazz) {
        return myDialogLayouts != null ? myDialogLayouts.get(clazz.getSimpleName()) : null;
    }

    public DialogLayout createDialogLayout(Class clazz) {
        DialogLayout layout = new DialogLayout();
        myDialogLayouts.put(clazz.getSimpleName(), layout);
        return layout;
    }

    public String getAlacSourceOptions() {
        return myAlacSourceOptions;
    }

    public void setAlacSourceOptions(String alacSourceOptions) {
        myAlacSourceOptions = alacSourceOptions;
    }

    public String getFaadSourceOptions() {
        return myFaadSourceOptions;
    }

    public void setFaadSourceOptions(String faadSourceOptions) {
        myFaadSourceOptions = faadSourceOptions;
    }

    public String getLameOnlyOptions() {
        return myLameOnlyOptions;
    }

    public void setLameOnlyOptions(String lameOnlyOptions) {
        myLameOnlyOptions = lameOnlyOptions;
    }

    public String getLameTargetOptions() {
        return myLameTargetOptions;
    }

    public void setLameTargetOptions(String lameTargetOptions) {
        myLameTargetOptions = lameTargetOptions;
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

    public boolean isValidMailConfig() {
        return StringUtils.isNotEmpty(getMailHost()) && StringUtils.isNotEmpty(getMailSender());
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

    public boolean isMailTls() {
        return myMailTls;
    }

    public void setMailTls(boolean mailTls) {
        myMailTls = mailTls;
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
        LOGGER.info("Loading configuration.");
        try {
            File file = getSettingsFile();
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
            if (currentAppVersion.getMajor() != currentConfigVersion.getMajor() || currentAppVersion.getMinor() != currentConfigVersion.getMinor()) {
                myCryptedCreationTime = freshCryptedCreationTime;
            }
            if (currentConfigVersion.compareTo(currentAppVersion) < 0) {
                migrate();
            }
            setPort(JXPathUtils.getIntValue(settings, "serverPort", getPort()));
            setServerName(JXPathUtils.getStringValue(settings, "serverName", getServerName()));
            setAvailableOnLocalNet(JXPathUtils.getBooleanValue(settings, "availableOnLocalNet", isAvailableOnLocalNet()));
            setCheckUpdateOnStart(JXPathUtils.getBooleanValue(settings, "checkUpdateOnStart", isCheckUpdateOnStart()));
            setAutoStartServer(JXPathUtils.getBooleanValue(settings, "autoStartServer", isAutoStartServer()));
            setUpdateDatabaseOnServerStart(JXPathUtils.getBooleanValue(settings, "updateDatabaseOnServerStart", isUpdateDatabaseOnServerStart()));
            setIgnoreTimestamps(JXPathUtils.getBooleanValue(settings, "ignoreTimestamps", isIgnoreTimestamps()));
            List<String> dataSources = new ArrayList<String>();
            Iterator<JXPathContext> contextIterator = JXPathUtils.getContextIterator(settings, "datasources/datasource");
            while (contextIterator.hasNext()) {
                dataSources.add(JXPathUtils.getStringValue(contextIterator.next(), ".", null));
            }
            setDatasources(dataSources.toArray(new String[dataSources.size()]));
            setFileSystemArtistNameFolder(JXPathUtils.getIntValue(settings, "artistFolder", getFileSystemArtistNameFolder()));
            setFileSystemAlbumNameFolder(JXPathUtils.getIntValue(settings, "albumFolder", getFileSystemAlbumNameFolder()));
            setItunesDeleteMissingFiles(JXPathUtils.getBooleanValue(settings, "iTunesDeleteMissingFiles", isItunesDeleteMissingFiles()));
            setUploadDir(JXPathUtils.getStringValue(settings, "uploadDir", getUploadDir()));
            setUploadCreateUserDir(JXPathUtils.getBooleanValue(settings, "uploadCreateUserDir", isUploadCreateUserDir()));
            setLocalTempArchive(JXPathUtils.getBooleanValue(settings, "localTempArchive", isLocalTempArchive()));
            Iterator<JXPathContext> users = JXPathUtils.getContextIterator(settings, "users/user");
            while (users != null && users.hasNext()) {
                JXPathContext userContext = users.next();
                User user = new User(JXPathUtils.getStringValue(userContext, "name", null));
                user.loadFromPreferences(userContext);
                addUser(user);
            }
            setSupportName(JXPathUtils.getStringValue(settings, "supportName", getSupportName()));
            setSupportEmail(JXPathUtils.getStringValue(settings, "supportEmail", getSupportEmail()));
            setProxyServer(JXPathUtils.getBooleanValue(settings, "proxyServer", isProxyServer()));
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
                fileType.setVideo(JXPathUtils.getBooleanValue(fileTypeContext, "video", false));
                fileType.setProtected(JXPathUtils.getBooleanValue(fileTypeContext, "protected", false));
                fileType.setActive(JXPathUtils.getBooleanValue(fileTypeContext, "active", true));
                myFileTypes.add(fileType);
            }
            setArtistDropWords(JXPathUtils.getStringValue(settings, "artistDropWords", getArtistDropWords()));
            setQuitConfirmation(JXPathUtils.getBooleanValue(settings, "quitConfirmation", isQuitConfirmation()));
            setWebWelcomeMessage(JXPathUtils.getStringValue(settings, "webWelcomeMessage", getWebWelcomeMessage()));
            readPathInfoEncryptionKey(settings);
            setLameBinary(JXPathUtils.getStringValue(settings, "lameBinary", getLameBinary()));
            setFaadBinary(JXPathUtils.getStringValue(settings, "faadBinary", getFaadBinary()));
            setAlacBinary(JXPathUtils.getStringValue(settings, "alacBinary", getAlacBinary()));
            setLameOnlyOptions(JXPathUtils.getStringValue(settings, "lameOnlyOptions", getLameOnlyOptions()));
            setLameTargetOptions(JXPathUtils.getStringValue(settings, "lameTargetOptions", getLameTargetOptions()));
            setAlacSourceOptions(JXPathUtils.getStringValue(settings, "alacSourceOptions", getAlacSourceOptions()));
            setFaadSourceOptions(JXPathUtils.getStringValue(settings, "faadSourceOptions", getFaadSourceOptions()));
            setStreamingCacheTimeout(JXPathUtils.getIntValue(settings, "streamingCacheTimeout", getStreamingCacheTimeout()));
            setStreamingCacheMaxFiles(JXPathUtils.getIntValue(settings, "streamingCacheMaxFiles", getStreamingCacheMaxFiles()));
            setBandwidthLimit(JXPathUtils.getBooleanValue(settings, "bandwidthLimit", false));
            setBandwidthLimitFactor(new BigDecimal(JXPathUtils.getStringValue(settings, "bandwidthLimitFactor", "0")));
            setIgnoreArtwork(JXPathUtils.getBooleanValue(settings, "ignoreArtwork", false));
            setCodewaveLogLevel(Level.toLevel(JXPathUtils.getStringValue(settings, "codewaveLogLevel", Level.INFO.toString()).toUpperCase()));
            setWindowX(JXPathUtils.getIntValue(settings, "window/x", Integer.MAX_VALUE));
            setWindowY(JXPathUtils.getIntValue(settings, "window/y", Integer.MAX_VALUE));
            setLastNewVersionInfo(JXPathUtils.getStringValue(settings, "lastNewVersionInfo", "0"));
            setDeleteDatabaseOnNextStartOnError(JXPathUtils.getBooleanValue(settings, "deleteDatabaseOnNextStartOnError", false));
            setUpdateIgnoreVersion(JXPathUtils.getStringValue(settings, "updateIgnoreVersion", MyTunesRss.VERSION));
            Iterator<JXPathContext> cronTriggerIterator = JXPathUtils.getContextIterator(settings, "crontriggers/database");
            myDatabaseCronTriggers = new ArrayList<String>();
            while (cronTriggerIterator.hasNext()) {
                myDatabaseCronTriggers.add(JXPathUtils.getStringValue(cronTriggerIterator.next(), ".", ""));
            }
            loadDatabaseSettings(settings);
            setId3v2TrackComment(JXPathUtils.getStringValue(settings, "id3v2-track-comment", ""));
            setJmxHost(JXPathUtils.getStringValue(settings, "jmx/host", "0.0.0.0"));
            setJmxPort(JXPathUtils.getIntValue(settings, "jmx/port", 8500));
            setJmxUser(StringUtils.trimToNull(JXPathUtils.getStringValue(settings, "jmx/user", null)));
            setJmxPassword(StringUtils.trimToNull(JXPathUtils.getStringValue(settings, "jmx/password", null)));
            setTomcatMaxThreads(JXPathUtils.getStringValue(settings, "tomcat/max-threads", "200"));
            setTomcatAjpPort(JXPathUtils.getIntValue(settings, "tomcat/ajp-port", 0));
            String context = StringUtils.trimToNull(StringUtils.strip(JXPathUtils.getStringValue(settings, "tomcat/webapp-context", ""), "/"));
            setWebappContext(context != null ? "/" + context : "");
            setTomcatProxyHost(JXPathUtils.getStringValue(settings, "tomcat/proxy-host", null));
            setTomcatProxyScheme(JXPathUtils.getStringValue(settings, "tomcat/proxy-scheme", null));
            setTomcatProxyPort(JXPathUtils.getIntValue(settings, "tomcat/proxy-port", 0));
            setSslKeystoreFile(JXPathUtils.getStringValue(settings, "ssl/keystore/file", null));
            setSslKeystoreKeyAlias(JXPathUtils.getStringValue(settings, "ssl/keystore/keyalias", null));
            setSslKeystorePass(JXPathUtils.getStringValue(settings, "ssl/keystore/pass", null));
            setSslPort(JXPathUtils.getIntValue(settings, "ssl/port", 0));
            setTomcatSslProxyScheme(JXPathUtils.getStringValue(settings, "ssl/proxy-scheme", null));
            setTomcatSslProxyHost(JXPathUtils.getStringValue(settings, "ssl/proxy-host", null));
            setTomcatSslProxyPort(JXPathUtils.getIntValue(settings, "ssl/proxy-port", 0));
            myAdditionalContexts = new ArrayList<String>();
            Iterator<JXPathContext> additionalContextsIterator = JXPathUtils.getContextIterator(settings, "tomcat/additionalContexts/context");
            while (additionalContextsIterator.hasNext()) {
                JXPathContext additionalContext = additionalContextsIterator.next();
                myAdditionalContexts.add(JXPathUtils.getStringValue(additionalContext, "name", "").trim() + ":" + JXPathUtils.getStringValue(
                        additionalContext,
                        "docbase",
                        "").trim());
            }
            myDialogLayouts = new HashMap<String, DialogLayout>();
            Iterator<JXPathContext> dialogLayoutsIterator = JXPathUtils.getContextIterator(settings, "dialogs/layout");
            while (dialogLayoutsIterator.hasNext()) {
                JXPathContext dialogLayout = dialogLayoutsIterator.next();
                DialogLayout layout = new DialogLayout();
                layout.setX(JXPathUtils.getIntValue(dialogLayout, "x", -1));
                layout.setY(JXPathUtils.getIntValue(dialogLayout, "y", -1));
                layout.setWidth(JXPathUtils.getIntValue(dialogLayout, "width", -1));
                layout.setHeight(JXPathUtils.getIntValue(dialogLayout, "height", -1));
                myDialogLayouts.put(JXPathUtils.getStringValue(dialogLayout, "class", "").trim(), layout);
            }
            setMailHost(JXPathUtils.getStringValue(settings, "mail-host", null));
            setMailPort(JXPathUtils.getIntValue(settings, "mail-port", -1));
            setMailTls(JXPathUtils.getBooleanValue(settings, "mail-tls", false));
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
        } catch (IOException e) {
            LOGGER.error("Could not read configuration file.", e);
        }
    }

    private void loadDatabaseSettings(JXPathContext settings) throws IOException {
        setDefaultDatabaseSettings();
        setDatabaseType(JXPathUtils.getStringValue(settings, "database/type", getDatabaseType()));
        setDatabaseDriver(JXPathUtils.getStringValue(settings, "database/driver", getDatabaseDriver()));
        setDatabaseConnection(JXPathUtils.getStringValue(settings, "database/connection", getDatabaseConnection()));
        setDatabaseUser(JXPathUtils.getStringValue(settings, "database/user", getDatabaseUser()));
        setDatabasePassword(JXPathUtils.getStringValue(settings, "database/password", getDatabasePassword()));
    }

    public void setDefaultDatabaseSettings() throws IOException {
        setDatabaseType("h2");
        setDatabaseDriver("org.h2.Driver");
        setDatabaseConnection("jdbc:h2:file:" + PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/" + "h2/MyTunesRSS");
        setDatabaseUser("sa");
        setDatabasePassword("");
    }

    private static File getSettingsFile() throws IOException {
        String filename = System.getProperty("settings-file", "settings.xml");
        return new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/" + filename);
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
        LOGGER.info("Saving configuration.");
        try {
            Document settings = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = settings.createElement("settings");
            settings.appendChild(root);
            root.appendChild(DOMUtils.createTextElement(settings, "version", myVersion));
            root.appendChild(DOMUtils.createIntElement(settings, "serverPort", myPort));
            root.appendChild(DOMUtils.createTextElement(settings, "serverName", myServerName));
            root.appendChild(DOMUtils.createBooleanElement(settings, "availableOnLocalNet", myAvailableOnLocalNet));
            root.appendChild(DOMUtils.createBooleanElement(settings, "checkUpdateOnStart", myCheckUpdateOnStart));
            root.appendChild(DOMUtils.createBooleanElement(settings, "autoStartServer", myAutoStartServer));
            root.appendChild(DOMUtils.createBooleanElement(settings, "updateDatabaseOnServerStart", myUpdateDatabaseOnServerStart));
            root.appendChild(DOMUtils.createBooleanElement(settings, "ignoreTimestamps", myIgnoreTimestamps));
            root.appendChild(DOMUtils.createIntElement(settings, "baseDirCount", myDatasources.size()));
            Element dataSources = settings.createElement("datasources");
            root.appendChild(dataSources);
            for (int i = 0; i < myDatasources.size(); i++) {
                dataSources.appendChild(DOMUtils.createTextElement(settings, "datasource", myDatasources.get(i)));
            }
            root.appendChild(DOMUtils.createIntElement(settings, "artistFolder", myFileSystemArtistNameFolder));
            root.appendChild(DOMUtils.createIntElement(settings, "albumFolder", myFileSystemAlbumNameFolder));
            root.appendChild(DOMUtils.createBooleanElement(settings, "iTunesDeleteMissingFiles", myItunesDeleteMissingFiles));
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
            root.appendChild(DOMUtils.createBooleanElement(settings, "proxyServer", myProxyServer));
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
                fileTypeElement.appendChild(DOMUtils.createBooleanElement(settings, "video", fileType.isVideo()));
                fileTypeElement.appendChild(DOMUtils.createBooleanElement(settings, "protected", fileType.isProtected()));
                fileTypeElement.appendChild(DOMUtils.createBooleanElement(settings, "active", fileType.isActive()));
            }
            root.appendChild(DOMUtils.createTextElement(settings, "artistDropWords", myArtistDropWords));
            root.appendChild(DOMUtils.createTextElement(settings, CREATION_TIME_KEY, myCryptedCreationTime));
            root.appendChild(DOMUtils.createBooleanElement(settings, "quitConfirmation", myQuitConfirmation));
            root.appendChild(DOMUtils.createTextElement(settings, "webWelcomeMessage", myWebWelcomeMessage));
            if (myPathInfoKey != null) {
                root.appendChild(DOMUtils.createByteArrayElement(settings, "pathInfoKey", myPathInfoKey.getEncoded()));
            }
            root.appendChild(DOMUtils.createTextElement(settings, "lameBinary", myLameBinary));
            root.appendChild(DOMUtils.createTextElement(settings, "faadBinary", myFaadBinary));
            root.appendChild(DOMUtils.createTextElement(settings, "alacBinary", myAlacBinary));
            root.appendChild(DOMUtils.createTextElement(settings, "lameOnlyOptions", myLameOnlyOptions));
            root.appendChild(DOMUtils.createTextElement(settings, "lameTargetOptions", myLameTargetOptions));
            root.appendChild(DOMUtils.createTextElement(settings, "alacSourceOptions", myAlacSourceOptions));
            root.appendChild(DOMUtils.createTextElement(settings, "faadSourceOptions", myFaadSourceOptions));
            root.appendChild(DOMUtils.createIntElement(settings, "streamingCacheTimeout", myStreamingCacheTimeout));
            root.appendChild(DOMUtils.createIntElement(settings, "streamingCacheMaxFiles", myStreamingCacheMaxFiles));
            root.appendChild(DOMUtils.createBooleanElement(settings, "bandwidthLimit", myBandwidthLimit));
            root.appendChild(DOMUtils.createTextElement(settings, "bandwidthLimitFactor", myBandwidthLimitFactor.toString()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "ignoreArtwork", myIgnoreArtwork));
            root.appendChild(DOMUtils.createTextElement(settings, "codewaveLogLevel", myCodewaveLogLevel.toString().toUpperCase()));
            Element window = settings.createElement("window");
            root.appendChild(window);
            window.appendChild(DOMUtils.createIntElement(settings, "x", myWindowX));
            window.appendChild(DOMUtils.createIntElement(settings, "y", myWindowY));
            root.appendChild(DOMUtils.createTextElement(settings, "lastNewVersionInfo", myLastNewVersionInfo));
            root.appendChild(DOMUtils.createBooleanElement(settings, "deleteDatabaseOnNextStartOnError", myDeleteDatabaseOnNextStartOnError));
            root.appendChild(DOMUtils.createTextElement(settings, "updateIgnoreVersion", myUpdateIgnoreVersion));
            if (myDatabaseCronTriggers != null && myDatabaseCronTriggers.size() > 0) {
                Element cronTriggers = settings.createElement("crontriggers");
                root.appendChild(cronTriggers);
                for (String databaseCronTrigger : myDatabaseCronTriggers) {
                    cronTriggers.appendChild(DOMUtils.createTextElement(settings, "database", databaseCronTrigger));
                }
            }
            Element database = settings.createElement("database");
            root.appendChild(database);
            database.appendChild(DOMUtils.createTextElement(settings, "type", getDatabaseType()));
            database.appendChild(DOMUtils.createTextElement(settings, "driver", getDatabaseDriver()));
            database.appendChild(DOMUtils.createTextElement(settings, "connection", getDatabaseConnection()));
            database.appendChild(DOMUtils.createTextElement(settings, "user", getDatabaseUser()));
            database.appendChild(DOMUtils.createTextElement(settings, "password", getDatabasePassword()));
            root.appendChild(DOMUtils.createTextElement(settings, "id3v2-track-comment", getId3v2TrackComment()));
            Element jmx = settings.createElement("jmx");
            root.appendChild(jmx);
            jmx.appendChild(DOMUtils.createTextElement(settings, "host", getJmxHost()));
            jmx.appendChild(DOMUtils.createIntElement(settings, "port", getJmxPort()));
            jmx.appendChild(DOMUtils.createTextElement(settings, "user", getJmxUser()));
            jmx.appendChild(DOMUtils.createTextElement(settings, "password", getJmxPassword()));
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
            if (myAdditionalContexts != null && !myAdditionalContexts.isEmpty()) {
                Element additionalContexts = settings.createElement("additionalContexts");
                tomcat.appendChild(additionalContexts);
                for (String contextInfo : myAdditionalContexts) {
                    Element context = settings.createElement("context");
                    additionalContexts.appendChild(context);
                    context.appendChild(DOMUtils.createTextElement(settings, "name", contextInfo.split(":", 2)[0]));
                    context.appendChild(DOMUtils.createTextElement(settings, "docbase", contextInfo.split(":", 2)[1]));
                }
            }
            if (myDialogLayouts != null && !myDialogLayouts.isEmpty()) {
                Element dialogLayouts = settings.createElement("dialogs");
                root.appendChild(dialogLayouts);
                for (Map.Entry<String, DialogLayout> layout : myDialogLayouts.entrySet()) {
                    Element dialogLayout = settings.createElement("layout");
                    dialogLayouts.appendChild(dialogLayout);
                    dialogLayout.appendChild(DOMUtils.createTextElement(settings, "class", layout.getKey()));
                    dialogLayout.appendChild(DOMUtils.createIntElement(settings, "x", layout.getValue().getX()));
                    dialogLayout.appendChild(DOMUtils.createIntElement(settings, "y", layout.getValue().getY()));
                    dialogLayout.appendChild(DOMUtils.createIntElement(settings, "width", layout.getValue().getWidth()));
                    dialogLayout.appendChild(DOMUtils.createIntElement(settings, "height", layout.getValue().getHeight()));
                }
            }
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
            root.appendChild(DOMUtils.createBooleanElement(settings, "mail-tls", isMailTls()));
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

    private void migrate() {
        Version current = new Version(getVersion());
        if (current.compareTo(new Version("3.6")) < 0) {
            setVersion("3.6");
        }
    }

    public boolean isDefaultDatabase() {
        return StringUtils.isEmpty(myDatabaseType) || "h2".equalsIgnoreCase(myDatabaseType);
    }

    public List<FileType> getDeepFileTypesClone() {
        List<FileType> clone = new ArrayList<FileType>(getFileTypes().size());
        for (FileType type : getFileTypes()) {
            clone.add(new FileType(type));
        }
        return clone;
    }

    public boolean isTomcatProxy() {
        return StringUtils.isNotBlank(myTomcatProxyHost) && myTomcatProxyPort > 0 && myTomcatProxyPort < 65536;
    }

    public boolean isTomcatSslProxy() {
        return StringUtils.isNotBlank(myTomcatSslProxyHost) && myTomcatSslProxyPort > 0 && myTomcatSslProxyPort < 65536;
    }
}