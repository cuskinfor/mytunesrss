/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.io.*;
import de.codewave.utils.xml.*;
import de.codewave.utils.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.*;
import java.awt.*;

/**
 * de.codewave.mytunesrss.MyTunesRssConfig
 */
public class MyTunesRssConfig {
    private static final Log LOG = LogFactory.getLog(MyTunesRssConfig.class);
    private static final String PREF_ROOT = "/de/codewave/mytunesrss30beta";

    private int myPort = 8080;
    private String myServerName = "MyTunesRSS";
    private boolean myAvailableOnLocalNet = true;
    private String myLibraryXml = "";
    private String myBaseDir = "";
    private boolean myCheckUpdateOnStart = true;
    private boolean myAutoStartServer = false;
    private boolean myAutoUpdateDatabase = false;
    private int myAutoUpdateDatabaseInterval = 10;
    private String myVersion;
    private boolean myIgnoreTimestamps = false;
    private int myFileSystemAlbumNameFolder = 1;
    private int myFileSystemArtistNameFolder = 2;
    private boolean mySaveEnabled = true;
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

    public String getLibraryXml() {
        return myLibraryXml;
    }

    public void setLibraryXml(String libraryXml) {
        myLibraryXml = StringUtils.trim(libraryXml);
    }

    public String getBaseDir() {
        return myBaseDir;
    }

    public void setBaseDir(String baseDir) {
        myBaseDir = StringUtils.trim(baseDir);
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

    public Collection<User> getUsers() {
        return new HashSet<User>(myUsers);
    }

    public User getUser(String name) {
        for (User user : myUsers) {
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

  private String findItunesLibraryXml(Trigger trigger) {
        String userHome = System.getProperty("user.home");
        if (StringUtils.isNotEmpty(userHome)) {
            if (!userHome.endsWith("/") && !userHome.endsWith("\\")) {
                userHome += "/";
            }
            Collection<File> files = IOUtils.find(new File(userHome), "iTunes Music Library.xml", trigger);
            if (!files.isEmpty()) {
                for (File file : files) {
                    File parentFile = file.getParentFile();
                    if (parentFile != null && parentFile.getName().equals("iTunes")) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        return "";
    }

    public void loadFromPrefs() {
        checkPrefsVersion();
        setVersion(Preferences.userRoot().node(PREF_ROOT).get("version", ""));
        migrate();
        setPort(Preferences.userRoot().node(PREF_ROOT).getInt("serverPort", getPort()));
        try {
            if (!Arrays.asList(Preferences.userRoot().node(PREF_ROOT).keys()).contains("iTunesLibrary")) {
                MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.searchingItunesXml"), MyTunesRss.BUNDLE.getString("cancel"), false, new MyTunesRssTask() {
                    private Trigger myCancelTrigger = new Trigger();

                    public void execute() throws Exception {
                        setLibraryXml(findItunesLibraryXml(myCancelTrigger));
                    }

                    @Override
                    protected void cancel() {
                        myCancelTrigger.trigger();
                    }
                });
            }
        } catch (BackingStoreException e) {
            // intentionally left blank
        }
        setServerName(Preferences.userRoot().node(PREF_ROOT).get("serverName", getServerName()));
        setAvailableOnLocalNet(Preferences.userRoot().node(PREF_ROOT).getBoolean("availableOnLocalNet", isAvailableOnLocalNet()));
        setLibraryXml(Preferences.userRoot().node(PREF_ROOT).get("iTunesLibrary", getLibraryXml()));
        setCheckUpdateOnStart(Preferences.userRoot().node(PREF_ROOT).getBoolean("checkUpdateOnStart", isCheckUpdateOnStart()));
        setAutoStartServer(Preferences.userRoot().node(PREF_ROOT).getBoolean("autoStartServer", isAutoStartServer()));
        setAutoUpdateDatabase(Preferences.userRoot().node(PREF_ROOT).getBoolean("autoUpdateDatabase", isAutoUpdateDatabase()));
        setUpdateDatabaseOnServerStart(Preferences.userRoot().node(PREF_ROOT).getBoolean("updateDatabaseOnServerStart", isUpdateDatabaseOnServerStart()));
        setAutoUpdateDatabaseInterval(Preferences.userRoot().node(PREF_ROOT).getInt("autoUpdateDatabaseInterval",
                                                                                                    getAutoUpdateDatabaseInterval()));
        setIgnoreTimestamps(Preferences.userRoot().node(PREF_ROOT).getBoolean("ignoreTimestamps", isIgnoreTimestamps()));
        setBaseDir(Preferences.userRoot().node(PREF_ROOT).get("baseDir", getBaseDir()));
        setFileSystemArtistNameFolder(Preferences.userRoot().node(PREF_ROOT).getInt("artistFolder", getFileSystemArtistNameFolder()));
        setFileSystemAlbumNameFolder(Preferences.userRoot().node(PREF_ROOT).getInt("albumFolder", getFileSystemAlbumNameFolder()));
        setItunesDeleteMissingFiles(Preferences.userRoot().node(PREF_ROOT).getBoolean("iTunesDeleteMissingFiles", isItunesDeleteMissingFiles()));
        setUploadDir(Preferences.userRoot().node(PREF_ROOT).get("uploadDir", getUploadDir()));
        setUploadCreateUserDir(Preferences.userRoot().node(PREF_ROOT).getBoolean("uploadCreateUserDir", isUploadCreateUserDir()));
        Preferences userNode = Preferences.userRoot().node(PREF_ROOT + "/user");
        if (userNode != null) {
            try {
                for (String userName : userNode.childrenNames()) {
                    User user = new User(userName);
                    user.setActive(userNode.node(userName).getBoolean("active", true));
                    user.setPasswordHash(userNode.node(userName).getByteArray("password", null));
                    user.setRss(userNode.node(userName).getBoolean("featureRss", true));
                    user.setM3u(userNode.node(userName).getBoolean("featureM3u", true));
                    user.setDownload(userNode.node(userName).getBoolean("featureDownload", true));
                    user.setUpload(userNode.node(userName).getBoolean("featureUpload", false));
                    user.setResetTime(userNode.node(userName).getLong("resetTime", System.currentTimeMillis()));
                    user.setQuotaResetTime(userNode.node(userName).getLong("quotaResetTime", System.currentTimeMillis()));
                    user.setDownBytes(userNode.node(userName).getLong("downBytes", 0));
                    user.setDownFiles(userNode.node(userName).getInt("downFiles", 0));
                    user.setQuotaDownBytes(userNode.node(userName).getLong("quotaDownBytes", 0));
                    user.setQuotaDownFiles(userNode.node(userName).getInt("quotaDownFiles", 0));
                    user.setBytesQuota(userNode.node(userName).getLong("bytesQuota", 0));
                    user.setFileQuota(userNode.node(userName).getInt("filesQuota", 0));
                    user.setQuotaType(User.QuotaType.valueOf(userNode.node(userName).get("quotaType", User.QuotaType.None.name())));
                    user.setMaximumZipEntries(userNode.node(userName).getInt("maximumZipEntries", 0));
                    addUser(user);
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
    }

    public void loadFromXml(URL xmlUrl) {
        JXPathContext context = JXPathUtils.getContext(xmlUrl);
        // preferences
        if (JXPathUtils.getBooleanValue(context, "/mytunesrss/preferences/@load", false)) {
            loadFromPrefs();
        }
        mySaveEnabled = JXPathUtils.getBooleanValue(context, "/mytunesrss/preferences/@save", false);
        // server
        setPort(JXPathUtils.getIntValue(context, "/mytunesrss/server/@port", getPort()));
        setServerName(JXPathUtils.getStringValue(context, "/mytunesrss/server/@name", getServerName()));
        setAvailableOnLocalNet((JXPathUtils.getBooleanValue(context, "/mytunesrss/server/@availableonlocalnet", isAvailableOnLocalNet())));
        setAutoStartServer(JXPathUtils.getBooleanValue(context, "/mytunesrss/server/@autostart", isAutoStartServer()));
        // data sources
        setLibraryXml(JXPathUtils.getStringValue(context, "/mytunesrss/datasource/itunesxml", getLibraryXml()));
        setBaseDir(JXPathUtils.getStringValue(context, "/mytunesrss/datasource/basedir", getBaseDir()));
        setFileSystemAlbumNameFolder(JXPathUtils.getIntValue(context, "/mytunesrss/datasource/basedir/@album", getFileSystemAlbumNameFolder()));
        setFileSystemArtistNameFolder(JXPathUtils.getIntValue(context, "/mytunesrss/datasource/basedir/@artist", getFileSystemArtistNameFolder()));
        setFileTypes(JXPathUtils.getStringValue(context, "/mytunesrss/datasource/filetypes", getFileTypes()));
        setArtistDropWords(JXPathUtils.getStringValue(context, "/mytunesrss/datasource/artistdropwords", getArtistDropWords()));
        setItunesDeleteMissingFiles(JXPathUtils.getBooleanValue(context, "/mytunesrss/datasource/itunesxml/@deletemissing", isItunesDeleteMissingFiles()));
        setUploadDir(JXPathUtils.getStringValue(context, "/mytunesrss/upload/basedir", getUploadDir()));
        setUploadCreateUserDir(JXPathUtils.getBooleanValue(context, "/mytunesrss/upload/@userdir", isUploadCreateUserDir()));
        // misc
        setCheckUpdateOnStart(JXPathUtils.getBooleanValue(context, "/mytunesrss/updatecheck", isCheckUpdateOnStart()));
        setVersion(MyTunesRss.VERSION);
        // database
        setUpdateDatabaseOnServerStart(JXPathUtils.getBooleanValue(context, "/mytunesrss/database/@updateonstart", isUpdateDatabaseOnServerStart()));
        setAutoUpdateDatabase(JXPathUtils.getBooleanValue(context, "/mytunesrss/database/@autoupdate", isAutoUpdateDatabase()));
        setAutoUpdateDatabaseInterval(JXPathUtils.getIntValue(context, "/mytunesrss/database/@updateinterval", getAutoUpdateDatabaseInterval()));
        setIgnoreTimestamps(JXPathUtils.getBooleanValue(context, "/mytunesrss/database/@ignoretimestamps", isIgnoreTimestamps()));
        // users
        for (Iterator<JXPathContext> iterator = JXPathUtils.getContextIterator(context, "/mytunesrss/user"); iterator.hasNext();) {
            JXPathContext userContext = iterator.next();
            User user = new User(JXPathUtils.getStringValue(userContext, "@name", null));
            user.setActive(JXPathUtils.getBooleanValue(userContext, "@active", true));
            user.setPasswordHash(MyTunesRssBase64Utils.decode(JXPathUtils.getStringValue(userContext, "@password", null)));
            user.setRss(JXPathUtils.getBooleanValue(userContext, "features/@rss", true));
            user.setM3u(JXPathUtils.getBooleanValue(userContext, "features/@m3u", true));
            user.setDownload(JXPathUtils.getBooleanValue(userContext, "features/@download", true));
            user.setUpload(JXPathUtils.getBooleanValue(userContext, "features/@upload", false));
            user.setMaximumZipEntries(JXPathUtils.getIntValue(userContext, "maximumZipEntries", 0));
            addUser(user);
            if (!MyTunesRss.REGISTRATION.isRegistered()) {
                break;
            }
        }
        // http proxy
        setProxyServer(context.getValue("/mytunesrss/proxy") != null);
        setProxyHost(JXPathUtils.getStringValue(context, "/mytunesrss/proxy/host", getProxyHost()));
        setProxyPort(JXPathUtils.getIntValue(context, "/mytunesrss/proxy/port", getProxyPort()));
        setMyTunesRssComUser(JXPathUtils.getStringValue(context, "/mytunesrss/mytunesrsscom/@username", ""));
        setMyTunesRssComPasswordHash(MyTunesRssBase64Utils.decode(JXPathUtils.getStringValue(context, "/mytunesrss/mytunesrsscom/@password", null)));
    }

    public void save() {
        if (mySaveEnabled) {
            Preferences.userRoot().node(PREF_ROOT).put("version", MyTunesRss.VERSION);
            Preferences.userRoot().node(PREF_ROOT).putInt("serverPort", myPort);
            Preferences.userRoot().node(PREF_ROOT).put("serverName", myServerName);
            Preferences.userRoot().node(PREF_ROOT).putBoolean("availableOnLocalNet", myAvailableOnLocalNet);
            Preferences.userRoot().node(PREF_ROOT).put("iTunesLibrary", myLibraryXml);
            Preferences.userRoot().node(PREF_ROOT).putBoolean("checkUpdateOnStart", myCheckUpdateOnStart);
            Preferences.userRoot().node(PREF_ROOT).putBoolean("autoStartServer", myAutoStartServer);
            Preferences.userRoot().node(PREF_ROOT).putBoolean("updateDatabaseOnServerStart", myUpdateDatabaseOnServerStart);
            Preferences.userRoot().node(PREF_ROOT).putBoolean("autoUpdateDatabase", myAutoUpdateDatabase);
            Preferences.userRoot().node(PREF_ROOT).putInt("autoUpdateDatabaseInterval", myAutoUpdateDatabaseInterval);
            Preferences.userRoot().node(PREF_ROOT).put("version", myVersion);
            Preferences.userRoot().node(PREF_ROOT).putBoolean("ignoreTimestamps", myIgnoreTimestamps);
            Preferences.userRoot().node(PREF_ROOT).put("baseDir", myBaseDir);
            Preferences.userRoot().node(PREF_ROOT).putInt("artistFolder", myFileSystemArtistNameFolder);
            Preferences.userRoot().node(PREF_ROOT).putInt("albumFolder", myFileSystemAlbumNameFolder);
            Preferences.userRoot().node(PREF_ROOT).putBoolean("iTunesDeleteMissingFiles", myItunesDeleteMissingFiles);
            Preferences.userRoot().node(PREF_ROOT).put("uploadDir", myUploadDir);
            Preferences.userRoot().node(PREF_ROOT).putBoolean("uploadCreateUserDir", myUploadCreateUserDir);
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
                    if (user.getPasswordHash() != null && user.getPasswordHash().length > 0) {
                        userNode.node(user.getName()).putByteArray("password", user.getPasswordHash());
                    } else {
                        userNode.node(user.getName()).remove("password");
                    }
                    userNode.node(user.getName()).putBoolean("active", user.isActive());
                    userNode.node(user.getName()).putBoolean("featureRss", user.isRss());
                    userNode.node(user.getName()).putBoolean("featureM3u", user.isM3u());
                    userNode.node(user.getName()).putBoolean("featureDownload", user.isDownload());
                    userNode.node(user.getName()).putBoolean("featureUpload", user.isUpload());
                    userNode.node(user.getName()).putLong("resetTime", user.getResetTime());
                    userNode.node(user.getName()).putLong("quotaResetTime", user.getQuotaResetTime());
                    userNode.node(user.getName()).putLong("downBytes", user.getDownBytes());
                    userNode.node(user.getName()).putInt("downFiles", user.getDownFiles());
                    userNode.node(user.getName()).putLong("quotaDownBytes", user.getQuotaDownBytes());
                    userNode.node(user.getName()).putInt("quotaDownFiles", user.getQuotaDownFiles());
                    userNode.node(user.getName()).putLong("bytesQuota", user.getBytesQuota());
                    userNode.node(user.getName()).putInt("filesQuota", user.getFileQuota());
                    userNode.node(user.getName()).put("quotaType", user.getQuotaType().name());
                    userNode.node(user.getName()).putInt("maximumZipEntries", user.getMaximumZipEntries());
                }
            } catch (BackingStoreException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not read users.", e);
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
        }
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
        if (getVersion().compareTo("2.1") < 0) {
            // migrate to 2.1
            int interval = Preferences.userRoot().node(PREF_ROOT).getInt("autoUpdateDatabaseInterval", 600) / 60;
            Preferences.userRoot().node(PREF_ROOT).putInt("autoUpdateDatabaseInterval", interval);
            setVersion("2.1");
        }
        if (getVersion().compareTo("2.3") < 0) {
            // migrate to 2.3
            String password = Preferences.userRoot().node(PREF_ROOT).get("serverPassword", "");
            if (StringUtils.isNotEmpty(password)) {
                try {
                    byte[] hash = MyTunesRss.MESSAGE_DIGEST.digest(password.getBytes("UTF-8"));
                    Preferences.userRoot().node(PREF_ROOT + "/user/default").putByteArray("password", hash);
                    Preferences.userRoot().node(PREF_ROOT + "/user/default").putBoolean("featureRss", true);
                    Preferences.userRoot().node(PREF_ROOT + "/user/default").putBoolean("featureM3u", true);
                    Preferences.userRoot().node(PREF_ROOT + "/user/default").putBoolean("featureDownload", true);
                    Preferences.userRoot().node(PREF_ROOT).remove("serverPassword");
                    setVersion("2.3");
                } catch (UnsupportedEncodingException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not create password hash.", e);
                    }
                }
            }
        }
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