/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.io.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.awt.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.MyTunesRssConfig
 */
public class MyTunesRssConfig {
    private static final Log LOG = LogFactory.getLog(MyTunesRssConfig.class);
    public static final String PREF_ROOT = "/de/codewave/mytunesrss3";

    private int myPort = 8080;
    private String myServerName = "MyTunesRSS";
    private boolean myAvailableOnLocalNet = true;
    private List<String> myDatasources = new ArrayList<String>();
    private boolean myCheckUpdateOnStart = true;
    private boolean myAutoStartServer;
    private boolean myAutoUpdateDatabase;
    private int myAutoUpdateDatabaseInterval = 10;
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

    public boolean isAutoUpdateDatabase() {
        return myAutoUpdateDatabase;
    }

    public void setAutoUpdateDatabase(boolean autoUpdateDatabase) {
        myAutoUpdateDatabase = autoUpdateDatabase;
    }

    public int getAutoUpdateDatabaseInterval() {
        return myAutoUpdateDatabaseInterval;
    }

    public void setAutoUpdateDatabaseInterval(int autoUpdateDatabaseInterval) {
        myAutoUpdateDatabaseInterval = autoUpdateDatabaseInterval;
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

    public void loadFromPrefs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading configuration.");
        }
        checkPrefsVersion();
        setVersion(Preferences.userRoot().node(PREF_ROOT).get("version", ""));
        migrate();
        setPort(Preferences.userRoot().node(PREF_ROOT).getInt("serverPort", getPort()));
        setServerName(Preferences.userRoot().node(PREF_ROOT).get("serverName", getServerName()));
        setAvailableOnLocalNet(Preferences.userRoot().node(PREF_ROOT).getBoolean("availableOnLocalNet", isAvailableOnLocalNet()));
        setCheckUpdateOnStart(Preferences.userRoot().node(PREF_ROOT).getBoolean("checkUpdateOnStart", isCheckUpdateOnStart()));
        setAutoStartServer(Preferences.userRoot().node(PREF_ROOT).getBoolean("autoStartServer", isAutoStartServer()));
        setAutoUpdateDatabase(Preferences.userRoot().node(PREF_ROOT).getBoolean("autoUpdateDatabase", isAutoUpdateDatabase()));
        setUpdateDatabaseOnServerStart(Preferences.userRoot().node(PREF_ROOT).getBoolean("updateDatabaseOnServerStart",
                                                                                         isUpdateDatabaseOnServerStart()));
        setAutoUpdateDatabaseInterval(Preferences.userRoot().node(PREF_ROOT).getInt("autoUpdateDatabaseInterval", getAutoUpdateDatabaseInterval()));
        setIgnoreTimestamps(Preferences.userRoot().node(PREF_ROOT).getBoolean("ignoreTimestamps", isIgnoreTimestamps()));
        String[] baseDirs = new String[Preferences.userRoot().node(PREF_ROOT).getInt("baseDirCount", 0)];
        for (int i = 0; i < baseDirs.length; i++) {
            baseDirs[i] = Preferences.userRoot().node(PREF_ROOT + "/basedir").get(Integer.toString(i), "");
            if (!MyTunesRss.REGISTRATION.isRegistered() && i + 1 == MyTunesRssRegistration.UNREGISTERED_MAX_WATCHFOLDERS) {
                break;
            }
        }
        setDatasources(baseDirs);
        setFileSystemArtistNameFolder(Preferences.userRoot().node(PREF_ROOT).getInt("artistFolder", getFileSystemArtistNameFolder()));
        setFileSystemAlbumNameFolder(Preferences.userRoot().node(PREF_ROOT).getInt("albumFolder", getFileSystemAlbumNameFolder()));
        setItunesDeleteMissingFiles(Preferences.userRoot().node(PREF_ROOT).getBoolean("iTunesDeleteMissingFiles", isItunesDeleteMissingFiles()));
        setUploadDir(Preferences.userRoot().node(PREF_ROOT).get("uploadDir", getUploadDir()));
        setUploadCreateUserDir(Preferences.userRoot().node(PREF_ROOT).getBoolean("uploadCreateUserDir", isUploadCreateUserDir()));
        setLocalTempArchive(Preferences.userRoot().node(PREF_ROOT).getBoolean("localTempArchive", isLocalTempArchive()));
        Preferences userNode = Preferences.userRoot().node(PREF_ROOT + "/user");
        if (userNode != null) {
            try {
                for (String userName : userNode.childrenNames()) {
                    User user = new User(userName);
                    user.loadFromPreferences(userNode.node(userName));
                    addUser(user);
                    if (!MyTunesRss.REGISTRATION.isRegistered() && getUsers().size() == MyTunesRssRegistration.UNREGISTERED_MAX_USERS) {
                        break;
                    }
                }
            } catch (BackingStoreException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not read users.", e);
                }
            }
        }
        setSupportName(Preferences.userRoot().node(PREF_ROOT).get("supportName", getSupportName()));
        setSupportEmail(Preferences.userRoot().node(PREF_ROOT).get("supportEmail", getSupportEmail()));
        setProxyServer(Preferences.userRoot().node(PREF_ROOT).getBoolean("proxyServer", isProxyServer()));
        setProxyHost(Preferences.userRoot().node(PREF_ROOT).get("proxyHost", getProxyHost()));
        setProxyPort(Preferences.userRoot().node(PREF_ROOT).getInt("proxyPort", getProxyPort()));
        setMyTunesRssComUser(Preferences.userRoot().node(PREF_ROOT).get("myTunesRssComUser", getMyTunesRssComUser()));
        setMyTunesRssComPasswordHash(Preferences.userRoot().node(PREF_ROOT).getByteArray("myTunesRssComPassword", getMyTunesRssComPasswordHash()));
        setFileTypes(Preferences.userRoot().node(PREF_ROOT).get("fileTypes", getFileTypes()));
        setArtistDropWords(Preferences.userRoot().node(PREF_ROOT).get("artistDropWords", getArtistDropWords()));
        setQuitConfirmation(Preferences.userRoot().node(PREF_ROOT).getBoolean("quitConfirmation", isQuitConfirmation()));
        setWebWelcomeMessage(Preferences.userRoot().node(PREF_ROOT).get("webWelcomeMessage", getWebWelcomeMessage()));
        readPathInfoEncryptionKey();
        setLameBinary(Preferences.userRoot().node(PREF_ROOT).get("lameBinary", getLameBinary()));
        setFaad2Binary(Preferences.userRoot().node(PREF_ROOT).get("faad2Binary", getFaad2Binary()));
        setAlacBinary(Preferences.userRoot().node(PREF_ROOT).get("alacBinary", getAlacBinary()));
        setStreamingCacheTimeout(Preferences.userRoot().node(PREF_ROOT).getInt("streamingCacheTimeout", getStreamingCacheTimeout()));
        setStreamingCacheMaxFiles(Preferences.userRoot().node(PREF_ROOT).getInt("streamingCacheMaxFiles", getStreamingCacheMaxFiles()));
        setBandwidthLimit(Preferences.userRoot().node(PREF_ROOT).getBoolean("bandwidthLimit", false));
        setBandwidthLimitFactor(new BigDecimal(Preferences.userRoot().node(PREF_ROOT).get("bandwidthLimitFactor", "0")));
        setIgnoreArtwork(Preferences.userRoot().node(PREF_ROOT).getBoolean("ignoreArtwork", false));
        setDebugLogging(loadDebugLogging());
        if (!MyTunesRss.REGISTRATION.isRegistered()) {
            adjustSettingsToUnregisteredState();
        }
    }

    public static boolean loadDebugLogging() {
        return Preferences.userRoot().node(PREF_ROOT).getBoolean("debugLogging", false);
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

    private void readPathInfoEncryptionKey() {
        String pathInfoKey = Preferences.userRoot().node(PREF_ROOT).get("pathInfoKey", null);
        if (StringUtils.isNotEmpty(pathInfoKey)) {
            byte[] keyBytes = new byte[0];
            try {
                keyBytes = Base64.decodeBase64(pathInfoKey.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                keyBytes = Base64.decodeBase64(pathInfoKey.getBytes());
            }
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
        Preferences.userRoot().node(PREF_ROOT).put("version", MyTunesRss.VERSION);
        Preferences.userRoot().node(PREF_ROOT).putInt("serverPort", myPort);
        Preferences.userRoot().node(PREF_ROOT).put("serverName", myServerName);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("availableOnLocalNet", myAvailableOnLocalNet);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("checkUpdateOnStart", myCheckUpdateOnStart);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("autoStartServer", myAutoStartServer);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("updateDatabaseOnServerStart", myUpdateDatabaseOnServerStart);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("autoUpdateDatabase", myAutoUpdateDatabase);
        Preferences.userRoot().node(PREF_ROOT).putInt("autoUpdateDatabaseInterval", myAutoUpdateDatabaseInterval);
        Preferences.userRoot().node(PREF_ROOT).put("version", myVersion);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("ignoreTimestamps", myIgnoreTimestamps);
        Preferences.userRoot().node(PREF_ROOT).putInt("baseDirCount", myDatasources.size());
        try {
            Preferences.userRoot().node(PREF_ROOT + "/basedir").removeNode();
            for (int i = 0; i < myDatasources.size(); i++) {
                Preferences.userRoot().node(PREF_ROOT + "/basedir").put(Integer.toString(i), myDatasources.get(i));
            }
        } catch (BackingStoreException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not write base directories.", e);
            }

        }
        Preferences.userRoot().node(PREF_ROOT).putInt("artistFolder", myFileSystemArtistNameFolder);
        Preferences.userRoot().node(PREF_ROOT).putInt("albumFolder", myFileSystemAlbumNameFolder);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("iTunesDeleteMissingFiles", myItunesDeleteMissingFiles);
        Preferences.userRoot().node(PREF_ROOT).put("uploadDir", myUploadDir);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("uploadCreateUserDir", myUploadCreateUserDir);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("localTempArchive", myLocalTempArchive);
        Preferences userNode = Preferences.userRoot().node(PREF_ROOT + "/user");
        try {
            // remove obsolete users
            for (String username : userNode.childrenNames()) {
                if (!myUsers.contains(new User(username))) {
                    userNode.node(username).removeNode();
                }
            }
            // create and update existing users
            for (User user : myUsers) {
                user.saveToPreferences(userNode.node(user.getName()));
            }
        } catch (BackingStoreException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not write users.", e);
            }
        }
        Preferences.userRoot().node(PREF_ROOT).put("supportName", mySupportName);
        Preferences.userRoot().node(PREF_ROOT).put("supportEmail", mySupportEmail);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("proxyServer", myProxyServer);
        Preferences.userRoot().node(PREF_ROOT).put("proxyHost", myProxyHost);
        Preferences.userRoot().node(PREF_ROOT).putInt("proxyPort", myProxyPort);
        Preferences.userRoot().node(PREF_ROOT).put("myTunesRssComUser", myMyTunesRssComUser);
        if (myMyTunesRssComPasswordHash != null && myMyTunesRssComPasswordHash.length > 0) {
            Preferences.userRoot().node(PREF_ROOT).putByteArray("myTunesRssComPassword", myMyTunesRssComPasswordHash);
        } else {
            Preferences.userRoot().node(PREF_ROOT).remove("myTunesRssComPassword");
        }
        Preferences.userRoot().node(PREF_ROOT).put("fileTypes", myFileTypes);
        Preferences.userRoot().node(PREF_ROOT).put("artistDropWords", myArtistDropWords);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("quitConfirmation", myQuitConfirmation);
        Preferences.userRoot().node(PREF_ROOT).put("webWelcomeMessage", myWebWelcomeMessage);
        if (myPathInfoKey != null) {
            try {
                Preferences.userRoot().node(PREF_ROOT).put("pathInfoKey", new String(Base64.encodeBase64(myPathInfoKey.getEncoded()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Preferences.userRoot().node(PREF_ROOT).put("pathInfoKey", new String(Base64.encodeBase64(myPathInfoKey.getEncoded())));
            }
        }
        Preferences.userRoot().node(PREF_ROOT).put("lameBinary", myLameBinary);
        Preferences.userRoot().node(PREF_ROOT).put("faad2Binary", myFaad2Binary);
        Preferences.userRoot().node(PREF_ROOT).put("alacBinary", myAlacBinary);
        Preferences.userRoot().node(PREF_ROOT).putInt("streamingCacheTimeout", myStreamingCacheTimeout);
        Preferences.userRoot().node(PREF_ROOT).putInt("streamingCacheMaxFiles", myStreamingCacheMaxFiles);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("bandwidthLimit", myBandwidthLimit);
        Preferences.userRoot().node(PREF_ROOT).put("bandwidthLimitFactor", myBandwidthLimitFactor.toString());
        Preferences.userRoot().node(PREF_ROOT).putBoolean("ignoreArtwork", myIgnoreArtwork);
        Preferences.userRoot().node(PREF_ROOT).putBoolean("debugLogging", myDebugLogging);
    }

    private void checkPrefsVersion() {
        String version = Preferences.userRoot().node(PREF_ROOT).get("version", "");
        if ("".equals(version)) {
            try {
                Preferences.userRoot().node(PREF_ROOT).removeNode();
            } catch (BackingStoreException e) {
                // intentionally left blank
            }
        }
    }

    private void migrate() {
        setVersion("3.1");
    }

    public Point loadWindowPosition() {
        Preferences preferences = Preferences.userRoot().node(PREF_ROOT);
        return new Point(preferences.getInt("window_x", Integer.MAX_VALUE), preferences.getInt("window_y", Integer.MAX_VALUE));
    }

    public void saveWindowPosition(Point point) {
        Preferences preferences = Preferences.userRoot().node(PREF_ROOT);
        preferences.putInt("window_x", point.x);
        preferences.putInt("window_y", point.y);
    }
}