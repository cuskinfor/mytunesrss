/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.*;
import de.codewave.utils.io.*;
import de.codewave.utils.xml.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.w3c.dom.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.parsers.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.MyTunesRssConfig
 */
public class MyTunesRssConfig {
    private static final Log LOG = LogFactory.getLog(MyTunesRssConfig.class);

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
    private boolean myUpdateDatabaseOnServerStart = true;
    private String myFileTypes = "";
    private String myArtistDropWords = "";
    private boolean myLocalTempArchive;
    private boolean myQuitConfirmation;
    private SecretKey myPathInfoKey;
    private String myWebWelcomeMessage = "";
    private String myLameBinary = "";
    private String myFaad2Binary = "";
    private String myAlacBinary = "";
    private int myStreamingCacheTimeout = 20;
    private int myStreamingCacheMaxFiles = 300;
    private boolean myBandwidthLimit;
    private BigDecimal myBandwidthLimitFactor;
    private boolean myIgnoreArtwork;
    private boolean myDebugLogging;
    private int myWindowX;
    private int myWindowY;
    private String myLastNewVersionInfo;
    private boolean myDeleteDatabaseOnNextStartOnError;
    private String myUpdateIgnoreVersion;
    private List<String> myDatabaseCronTriggers = new ArrayList<String>();

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
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not check if existing datasource contains new datasource or vice versa, assuming everything is okay.", e);
                    }
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

    public String getFileTypes() {
        return myFileTypes;
    }

    public void setFileTypes(String fileTypes) {
        myFileTypes = fileTypes;
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

    public String getFaad2Binary() {
        return myFaad2Binary;
    }

    public boolean isValidFaad2Binary() {
        return StringUtils.isNotEmpty(myFaad2Binary) && new File(myFaad2Binary).isFile();
    }

    public void setFaad2Binary(String faad2Binary) {
        myFaad2Binary = faad2Binary;
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

    public boolean isDebugLogging() {
        return myDebugLogging;
    }

    public void setDebugLogging(boolean debugLogging) {
        myDebugLogging = debugLogging;
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

    public void load() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading configuration.");
        }
        try {
            File file = getSettingsFile();
            JXPathContext settings = JXPathUtils.getContext(JXPathUtils.getContext(file.toURL()), "settings");
            setVersion(JXPathUtils.getStringValue(settings, "version", ""));
            migrate();
            setPort(JXPathUtils.getIntValue(settings, "serverPort", getPort()));
            setServerName(JXPathUtils.getStringValue(settings, "serverName", getServerName()));
            setAvailableOnLocalNet(JXPathUtils.getBooleanValue(settings, "availableOnLocalNet", isAvailableOnLocalNet()));
            setCheckUpdateOnStart(JXPathUtils.getBooleanValue(settings, "checkUpdateOnStart", isCheckUpdateOnStart()));
            setAutoStartServer(JXPathUtils.getBooleanValue(settings, "autoStartServer", isAutoStartServer()));
            setUpdateDatabaseOnServerStart(JXPathUtils.getBooleanValue(settings, "updateDatabaseOnServerStart", isUpdateDatabaseOnServerStart()));
            setIgnoreTimestamps(JXPathUtils.getBooleanValue(settings, "ignoreTimestamps", isIgnoreTimestamps()));
            List<String> dataSources = new ArrayList<String>();
            Iterator<JXPathContext> contextIterator = JXPathUtils.getContextIterator(settings, "datasources/datasource");
            int count = 0;
            while (contextIterator.hasNext()) {
                dataSources.add(JXPathUtils.getStringValue(contextIterator.next(), ".", null));
                if (!MyTunesRss.REGISTRATION.isRegistered() && count + 1 == MyTunesRssRegistration.UNREGISTERED_MAX_WATCHFOLDERS) {
                    break;
                }
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
                if (!MyTunesRss.REGISTRATION.isRegistered() && getUsers().size() == MyTunesRssRegistration.UNREGISTERED_MAX_USERS) {
                    break;
                }
            }
            setSupportName(JXPathUtils.getStringValue(settings, "supportName", getSupportName()));
            setSupportEmail(JXPathUtils.getStringValue(settings, "supportEmail", getSupportEmail()));
            setProxyServer(JXPathUtils.getBooleanValue(settings, "proxyServer", isProxyServer()));
            setProxyHost(JXPathUtils.getStringValue(settings, "proxyHost", getProxyHost()));
            setProxyPort(JXPathUtils.getIntValue(settings, "proxyPort", getProxyPort()));
            setMyTunesRssComUser(JXPathUtils.getStringValue(settings, "myTunesRssComUser", getMyTunesRssComUser()));
            setMyTunesRssComPasswordHash(JXPathUtils.getByteArray(settings, "myTunesRssComPassword", getMyTunesRssComPasswordHash()));
            setFileTypes(JXPathUtils.getStringValue(settings, "fileTypes", getFileTypes()));
            setArtistDropWords(JXPathUtils.getStringValue(settings, "artistDropWords", getArtistDropWords()));
            setQuitConfirmation(JXPathUtils.getBooleanValue(settings, "quitConfirmation", isQuitConfirmation()));
            setWebWelcomeMessage(JXPathUtils.getStringValue(settings, "webWelcomeMessage", getWebWelcomeMessage()));
            readPathInfoEncryptionKey(settings);
            setLameBinary(JXPathUtils.getStringValue(settings, "lameBinary", getLameBinary()));
            setFaad2Binary(JXPathUtils.getStringValue(settings, "faad2Binary", getFaad2Binary()));
            setAlacBinary(JXPathUtils.getStringValue(settings, "alacBinary", getAlacBinary()));
            setStreamingCacheTimeout(JXPathUtils.getIntValue(settings, "streamingCacheTimeout", getStreamingCacheTimeout()));
            setStreamingCacheMaxFiles(JXPathUtils.getIntValue(settings, "streamingCacheMaxFiles", getStreamingCacheMaxFiles()));
            setBandwidthLimit(JXPathUtils.getBooleanValue(settings, "bandwidthLimit", false));
            setBandwidthLimitFactor(new BigDecimal(JXPathUtils.getStringValue(settings, "bandwidthLimitFactor", "0")));
            setIgnoreArtwork(JXPathUtils.getBooleanValue(settings, "ignoreArtwork", false));
            setDebugLogging(JXPathUtils.getBooleanValue(settings, "debugLogging", false));
            if (!MyTunesRss.REGISTRATION.isRegistered()) {
                adjustSettingsToUnregisteredState();
            }
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
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read configuration file.", e);
            }
        }
    }

    private static File getSettingsFile() throws IOException {
        return new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/settings.xml");
    }

    public static boolean loadDebugLogging() {
        try {
            File file = getSettingsFile();
            JXPathContext settings = JXPathUtils.getContext(JXPathUtils.getContext(file.toURL()), "settings");
            return JXPathUtils.getBooleanValue(settings, "debugLogging", false);
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load debug logging setting.", e);
            }
        }
        return false;
    }

    public static boolean loadDeleteDatabaseOnNextStartOnError() {
        try {
            File file = getSettingsFile();
            JXPathContext settings = JXPathUtils.getContext(JXPathUtils.getContext(file.toURL()), "settings");
            return JXPathUtils.getBooleanValue(settings, "deleteDatabaseOnNextStartOnError", false);
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load deleteDatabaseOnNextStartOnError flag.", e);
            }
        }
        return false;
    }

    private void adjustSettingsToUnregisteredState() {
        setAvailableOnLocalNet(false);
        setUploadDir(null);
        setMyTunesRssComUser(null);
        setWebWelcomeMessage(null);
        setAlacBinary(null);
        setLameBinary(null);
        setFaad2Binary(null);
    }

    private void readPathInfoEncryptionKey(JXPathContext settings) {
        byte[] keyBytes = JXPathUtils.getByteArray(settings, "pathInfoKey", null);
        if (keyBytes != null && keyBytes.length > 0) {
            myPathInfoKey = new SecretKeySpec(keyBytes, "DES");
        }
        if (myPathInfoKey == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No path info encryption key found, generating a new one.");
            }
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
                keyGenerator.init(56);
                myPathInfoKey = keyGenerator.generateKey();
            } catch (NoSuchAlgorithmException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not generate path info encryption key.", e);
                }
            }
        }
    }

    public void save() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving configuration.");
        }
        try {
            Document settings = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = settings.createElement("settings");
            settings.appendChild(root);
            root.appendChild(DOMUtils.createTextElement(settings, "version", MyTunesRss.VERSION));
            root.appendChild(DOMUtils.createIntElement(settings, "serverPort", myPort));
            root.appendChild(DOMUtils.createTextElement(settings, "serverName", myServerName));
            root.appendChild(DOMUtils.createBooleanElement(settings, "availableOnLocalNet", myAvailableOnLocalNet));
            root.appendChild(DOMUtils.createBooleanElement(settings, "checkUpdateOnStart", myCheckUpdateOnStart));
            root.appendChild(DOMUtils.createBooleanElement(settings, "autoStartServer", myAutoStartServer));
            root.appendChild(DOMUtils.createBooleanElement(settings, "updateDatabaseOnServerStart", myUpdateDatabaseOnServerStart));
            root.appendChild(DOMUtils.createTextElement(settings, "version", myVersion));
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
            root.appendChild(DOMUtils.createTextElement(settings, "myTunesRssComUser", myMyTunesRssComUser));
            if (myMyTunesRssComPasswordHash != null && myMyTunesRssComPasswordHash.length > 0) {
                root.appendChild(DOMUtils.createByteArrayElement(settings, "myTunesRssComPassword", myMyTunesRssComPasswordHash));
            }
            root.appendChild(DOMUtils.createTextElement(settings, "fileTypes", myFileTypes));
            root.appendChild(DOMUtils.createTextElement(settings, "artistDropWords", myArtistDropWords));
            root.appendChild(DOMUtils.createBooleanElement(settings, "quitConfirmation", myQuitConfirmation));
            root.appendChild(DOMUtils.createTextElement(settings, "webWelcomeMessage", myWebWelcomeMessage));
            if (myPathInfoKey != null) {
                root.appendChild(DOMUtils.createByteArrayElement(settings, "pathInfoKey", myPathInfoKey.getEncoded()));
            }
            root.appendChild(DOMUtils.createTextElement(settings, "lameBinary", myLameBinary));
            root.appendChild(DOMUtils.createTextElement(settings, "faad2Binary", myFaad2Binary));
            root.appendChild(DOMUtils.createTextElement(settings, "alacBinary", myAlacBinary));
            root.appendChild(DOMUtils.createIntElement(settings, "streamingCacheTimeout", myStreamingCacheTimeout));
            root.appendChild(DOMUtils.createIntElement(settings, "streamingCacheMaxFiles", myStreamingCacheMaxFiles));
            root.appendChild(DOMUtils.createBooleanElement(settings, "bandwidthLimit", myBandwidthLimit));
            root.appendChild(DOMUtils.createTextElement(settings, "bandwidthLimitFactor", myBandwidthLimitFactor.toString()));
            root.appendChild(DOMUtils.createBooleanElement(settings, "ignoreArtwork", myIgnoreArtwork));
            root.appendChild(DOMUtils.createBooleanElement(settings, "debugLogging", myDebugLogging));
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
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(getSettingsFile());
                DOMUtils.prettyPrint(settings, outputStream);
            } finally {
                IOUtils.close(outputStream);
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not write settings file.", e);
            }
        }
    }

    private void migrate() {
        setVersion("3.2");
    }
}