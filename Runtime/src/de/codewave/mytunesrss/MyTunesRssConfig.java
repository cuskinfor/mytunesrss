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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.MyTunesRssConfig
 */
public class MyTunesRssConfig {
    private static final Log LOG = LogFactory.getLog(MyTunesRssConfig.class);

    private int myPort = 8080;
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

    private String findItunesLibraryXml() {
        String userHome = System.getProperty("user.home");
        if (StringUtils.isNotEmpty(userHome)) {
            if (!userHome.endsWith("/") && !userHome.endsWith("\\")) {
                userHome += "/";
            }
            Collection<File> files = IOUtils.find(new File(userHome), "iTunes Music Library.xml");
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
        setVersion(Preferences.userRoot().node("/de/codewave/mytunesrss").get("version", ""));
        migrate();
        setPort(Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("serverPort", getPort()));
        try {
            if (!Arrays.asList(Preferences.userRoot().node("/de/codewave/mytunesrss").keys()).contains("iTunesLibrary")) {
                MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.searchingItunesXml"), null, false, new MyTunesRssTask() {
                    public void execute() throws Exception {
                        setLibraryXml(findItunesLibraryXml());
                    }
                });
            }
        } catch (BackingStoreException e) {
            // intentionally left blank
        }
        setLibraryXml(Preferences.userRoot().node("/de/codewave/mytunesrss").get("iTunesLibrary", getLibraryXml()));
        setCheckUpdateOnStart(Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("checkUpdateOnStart", isCheckUpdateOnStart()));
        setAutoStartServer(Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("autoStartServer", isAutoStartServer()));
        setAutoUpdateDatabase(Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("autoUpdateDatabase", isAutoUpdateDatabase()));
        setAutoUpdateDatabaseInterval(Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("autoUpdateDatabaseInterval",
                                                                                                    getAutoUpdateDatabaseInterval()));
        setIgnoreTimestamps(Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("ignoreTimestamps", isIgnoreTimestamps()));
        Preferences userNode = Preferences.userRoot().node("/de/codewave/mytunesrss/user");
        if (userNode != null) {
            try {
                for (String userName : userNode.childrenNames()) {
                    User user = new User(userName);
                    user.setPasswordHash(userNode.node(userName).getByteArray("password", null));
                    user.setRss(userNode.node(userName).getBoolean("featureRss", true));
                    user.setM3u(userNode.node(userName).getBoolean("featureM3u", true));
                    user.setDownload(userNode.node(userName).getBoolean("featureDownload", true));
                    addUser(user);
                }
            } catch (BackingStoreException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not read users.", e);
                }
            }
        }
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
        setAutoStartServer(JXPathUtils.getBooleanValue(context, "/mytunesrss/server/@autostart", isAutoStartServer()));
        // data sources
        setLibraryXml(JXPathUtils.getStringValue(context, "/mytunesrss/datasource/itunesxml", getLibraryXml()));
        setBaseDir(JXPathUtils.getStringValue(context, "/mytunesrss/datasource/basedir", getBaseDir()));
        setFileSystemAlbumNameFolder(JXPathUtils.getIntValue(context, "/mytunesrss/datasource/basedir/@album", getFileSystemAlbumNameFolder()));
        setFileSystemArtistNameFolder(JXPathUtils.getIntValue(context, "/mytunesrss/datasource/basedir/@artist", getFileSystemArtistNameFolder()));
        // misc
        setCheckUpdateOnStart(JXPathUtils.getBooleanValue(context, "/mytunesrss/updatecheck", isCheckUpdateOnStart()));
        setVersion(MyTunesRss.VERSION);
        // database
        setAutoUpdateDatabase(JXPathUtils.getBooleanValue(context, "/mytunesrss/database/@autoupdate", isAutoUpdateDatabase()));
        setAutoUpdateDatabaseInterval(JXPathUtils.getIntValue(context, "/mytunesrss/database/@updateinterval", getAutoUpdateDatabaseInterval()));
        setIgnoreTimestamps(JXPathUtils.getBooleanValue(context, "/mytunesrss/database/@ignoretimestamps", isIgnoreTimestamps()));
        // users
        for (Iterator<JXPathContext> iterator = JXPathUtils.getContextIterator(context, "/mytunesrss/user"); iterator.hasNext();) {
            JXPathContext userContext = iterator.next();
            User user = new User(JXPathUtils.getStringValue(userContext, "@name", null));
            user.setPasswordHash(Base64Utils.decode(JXPathUtils.getStringValue(userContext, "@password", null)));
            user.setRss(JXPathUtils.getBooleanValue(userContext, "features/@rss", true));
            user.setM3u(JXPathUtils.getBooleanValue(userContext, "features/@m3u", true));
            user.setDownload(JXPathUtils.getBooleanValue(userContext, "features/@download", true));
            addUser(user);
        }
    }

    public void save() {
        if (mySaveEnabled) {
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("version", MyTunesRss.VERSION);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("serverPort", myPort);
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("iTunesLibrary", myLibraryXml);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("checkUpdateOnStart", myCheckUpdateOnStart);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("autoStartServer", myAutoStartServer);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("autoUpdateDatabase", myAutoUpdateDatabase);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("autoUpdateDatabaseInterval", myAutoUpdateDatabaseInterval);
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("version", myVersion);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("ignoreTimestamps", myIgnoreTimestamps);
            Preferences userNode = Preferences.userRoot().node("/de/codewave/mytunesrss/user");
            try {
                // remove obsolete users
                for (String username : userNode.childrenNames()) {
                    if (!myUsers.contains(new User(username))) {
                        userNode.node(username).removeNode();
                    }
                }
                // create and update existing users
                for (User user : myUsers) {
                    userNode.node(user.getName()).putByteArray("password", user.getPasswordHash());
                    userNode.node(user.getName()).putBoolean("featureRss", user.isRss());
                    userNode.node(user.getName()).putBoolean("featureM3u", user.isM3u());
                    userNode.node(user.getName()).putBoolean("featureDownload", user.isDownload());
                }
            } catch (BackingStoreException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not read users.", e);
                }
            }
        }
    }

    private void checkPrefsVersion() {
        String version = Preferences.userRoot().node("/de/codewave/mytunesrss").get("version", "");
        if ("".equals(version)) {
            try {
                Preferences.userRoot().node("/de/codewave/mytunesrss").removeNode();
            } catch (BackingStoreException e) {
                // intentionally left blank
            }
        }
    }

    private void migrate() {
        if (getVersion().compareTo("2.1") < 0) {
            // migrate to 2.1
            int interval = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("autoUpdateDatabaseInterval", 600) / 60;
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("autoUpdateDatabaseInterval", interval);
            setVersion("2.1");
        }
        if (getVersion().compareTo("2.3") < 0) {
            // migrate to 2.3
            String password = Preferences.userRoot().node("/de/codewave/mytunesrss").get("serverPassword", "");
            if (StringUtils.isNotEmpty(password)) {
                try {
                    byte[] hash = MyTunesRss.MESSAGE_DIGEST.digest(password.getBytes("UTF-8"));
                    Preferences.userRoot().node("/de/codewave/mytunesrss/user/default").putByteArray("password", hash);
                    Preferences.userRoot().node("/de/codewave/mytunesrss").remove("serverPassword");
                    setVersion("2.3");
                } catch (UnsupportedEncodingException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not create password hash.", e);
                    }
                }
            }
        }
    }
}