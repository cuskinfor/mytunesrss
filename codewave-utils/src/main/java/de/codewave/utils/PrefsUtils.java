/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Utilities for application preferences.
 */
public class PrefsUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PrefsUtils.class);
    
    public static String MAC_CACHES_BASE;
    public static String MAC_PREFS_BASE;
    
    /**
     * Get the path to the preferences data directory for the specified application name. The path is "/User/{username}/Library/Preferences/{applicationName}"
     * on Mac OS X. It is {env.appdata}\{applicationName} (e.g. "C:\Documents and Settings\{username}\Application Data\{applicationName}") on Windows.
     * If the environment variable "appdata" is not found, it is {user.home}\.{applicationName} (e.g. "C:\Documents and
     * Settings\{username}\.{applicationName}"). The same applies to all other systems.
     *
     * @param applicationName The name of the application.
     * @return The path to the preferences data directory.
     * @throws java.io.IOException
     */
    public static String getPreferencesDataPath(String applicationName) throws IOException {
        if (SystemUtils.IS_OS_MAC_OSX && StringUtils.isNotBlank(MAC_PREFS_BASE) && new File(MAC_PREFS_BASE).isDirectory()) {
            return createIfNotExists(new File(MAC_PREFS_BASE, applicationName));   
        }
        return getDataPath(applicationName, "Preferences", true);
    }

    /**
     * Create a folder if is does not exist and return the absolute path name in any case.
     * 
     * @param dir A folder.
     *            
     * @return The absolute path of the folder.
     */
    private static String createIfNotExists(File dir) {
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                LOGGER.warn("Could not create preferences folder \"" + dir.getAbsolutePath() + "\".");
            }
        }
        return dir.getAbsolutePath();
    }

    public static String getPreferencesDataPathNoCreate(String applicationName) throws IOException {
        if (SystemUtils.IS_OS_MAC_OSX && StringUtils.isNotBlank(MAC_PREFS_BASE) && new File(MAC_PREFS_BASE).isDirectory()) {
            File newStyleDir = new File(MAC_PREFS_BASE, applicationName);
            if (newStyleDir.isDirectory()) {
                return newStyleDir.getAbsolutePath();
            }
        }
        return getDataPath(applicationName, "Preferences", false);
    }

    /**
     * Get the path to the application data directory for the specified application name. The path is "/User/{username}/Library/Caches/{applicationName}"
     * on Mac OS X. It is {env.appdata}\{applicationName} (e.g. "C:\Documents and Settings\{username}\Application Data\{applicationName}") on Windows.
     * If the environment variable "appdata" is not found, it is {user.home}\.{applicationName} (e.g. "C:\Documents and
     * Settings\{username}\.{applicationName}"). The same applies to all other systems.
     *
     * @param applicationName The name of the application.
     * @return The path to the cache data directory.
     * @throws java.io.IOException
     */
    public static String getCacheDataPath(String applicationName) throws IOException {
        if (SystemUtils.IS_OS_MAC_OSX && StringUtils.isNotBlank(MAC_CACHES_BASE) && new File(MAC_CACHES_BASE).isDirectory()) {
            return createIfNotExists(new File(MAC_CACHES_BASE, applicationName));
        }
        return getDataPath(applicationName, "Caches", true);
    }

    public static String getCacheDataPathNoCreate(String applicationName) throws IOException {
        if (SystemUtils.IS_OS_MAC_OSX && StringUtils.isNotBlank(MAC_CACHES_BASE) && new File(MAC_CACHES_BASE).isDirectory()) {
            File newStyleDir = new File(MAC_CACHES_BASE, applicationName);
            if (newStyleDir.isDirectory()) {
                return newStyleDir.getAbsolutePath();
            }
        }
        return getDataPath(applicationName, "Caches", false);
    }

    /**
     * Get the path to the preferences or cache data directory for the specified application name. The path is
     * "/User/{username}/Library/{subDirNameParameter}/{applicationName}" on Mac OS X. It is {env.appdata}\{applicationName} (e.g. "C:\Documents and
     * Settings\{username}\Application Data\{applicationName}") on Windows. If the environment variable "appdata" is not found, it is
     * {user.home}\.{applicationName} (e.g. "C:\Documents and Settings\{username}\.{applicationName}"). The same applies to all other systems.
     *
     * @param applicationName The name of the application.
     * @param osxSubDirName   Name of the subdirectory for OSX, i.e. "Preferences" or "Caches"
     * @param create          TRUE to create missing folders or FALSE to return non-existent paths as well.
     * @return The path to the data directory.
     */
    private static String getDataPath(String applicationName, String osxSubDirName, boolean create) {
        String pathname = System.getProperty("user.home");
        if (StringUtils.isNotEmpty(pathname)) {
            if (!pathname.endsWith("/") && !pathname.endsWith("\\")) {
                pathname += "/";
            }
        } else {
            pathname = "./";
        }
        if (SystemUtils.IS_OS_MAC_OSX) {
            pathname += "Library/" + osxSubDirName + "/" + applicationName;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String envAppData = System.getenv("appdata");
            if (StringUtils.isNotEmpty(envAppData)) {
                pathname = envAppData;
                if (!pathname.endsWith("/") && !pathname.endsWith("\\")) {
                    pathname += "/";
                }
                pathname += applicationName;
            } else {
                pathname += "." + applicationName;
            }
        } else {
            pathname += "." + applicationName;
        }
        File path = new File(pathname);
        if (!path.exists() && create) {
            if (!path.mkdirs()) {
                LOGGER.warn("Could not create data folder \"" + path.getAbsolutePath() + "\".");
            }
        }
        return pathname;
    }

}
