/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.*;
import org.apache.commons.io.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.AddonsUtils
 */
public class AddonsUtils {
    private static final Log LOG = LogFactory.getLog(AddonsUtils.class);

    public static Collection<String> getThemes() {
        List<String> themes = new ArrayList<String>();
        try {
            File themesDir = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/themes");
            if (!themesDir.exists()) {
                themesDir.mkdirs();
            }
            if (themesDir.isDirectory()) {
                for (String theme : themesDir.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return new File(dir, name + "/images").isDirectory() && new File(dir, name + "/styles").isDirectory();
                    }
                })) {
                    themes.add(theme);
                }
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Problem reading existing themes.", e);
            }
        }
        Collections.sort(themes);
        return themes;
    }

    public static Collection<String> getLanguages() {
        List<String> languages = new ArrayList<String>();
        try {
            File languagesDir = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/languages");
            if (!languagesDir.exists()) {
                languagesDir.mkdirs();
            }
            if (languagesDir.isDirectory()) {
                for (String language : languagesDir.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.startsWith("MyTunesRssWeb_") && name.endsWith(".properties");
                    }
                })) {
                    int underscoreIndex = language.indexOf('_');
                    if (underscoreIndex > -1 && underscoreIndex + 1 < language.length()) {
                        languages.add(language.substring("MyTunesRssWeb_".length(), language.length() - ".properties".length()));
                    }
                }
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Problem reading existing themes and languages.", e);
            }
        }
        Collections.sort(languages);
        return languages;
    }

    public static void addTheme(File theme) {
        // todo: implement method
        throw new UnsupportedOperationException("method addTheme of class MyTunesRssConfig is not yet implemented!");
    }

    public static void addLanguage(File language) {
        // todo: implement method
        throw new UnsupportedOperationException("method addLanguage of class MyTunesRssConfig is not yet implemented!");
    }

    public static String deleteTheme(String theme) {
        try {
            FileUtils.deleteDirectory(new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/themes/" + theme));
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not delete theme \"" + theme + "\".", e);
            }
            return MyTunesRss.BUNDLE.getString("error.couldNotRemoveTheme");
        }
        return null;
    }

    public static String deleteLanguage(String language) {
        try {
            new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/languages/" + language).delete();
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not delete language \"" + language + "\".", e);
            }
            return MyTunesRss.BUNDLE.getString("error.couldNotRemoveLanguage");
        }
        return null;
    }
}