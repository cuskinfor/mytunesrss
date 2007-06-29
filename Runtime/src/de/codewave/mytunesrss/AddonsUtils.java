/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

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

    public static String addTheme(File theme) {
        if (isThemeArchive(theme)) {
            File themeDir = null;
            CodewaveZipInputStream zipInputStream = null;
            try {
                zipInputStream = CodewaveZipInputStreamFactory.newInstance(new FileInputStream(theme));
                for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                    themeDir = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/themes/" + FilenameUtils.getBaseName(theme.getName()));
                    saveFile(themeDir, entry.getName(), (InputStream)zipInputStream);
                }
            } catch (IOException e) {
                if (themeDir != null && themeDir.exists()) {
                    try {
                        FileUtils.deleteDirectory(themeDir);
                    } catch (IOException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not delete directory.", e);
                        }
                    }
                }
                return MyTunesRssUtils.getBundleString("error.couldNotExtractTheme");
            } finally {
                if (zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    } catch (IOException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not close zip input stream.", e);
                        }
                    }
                }
            }
        } else {
            return MyTunesRssUtils.getBundleString("error.invalidThemeFile");
        }
        return null;
    }

    private static boolean isThemeArchive(File theme) {
        CodewaveZipInputStream zipInputStream = null;
        boolean stylesFound = false;
        boolean imagesFound = false;
        try {
            zipInputStream = CodewaveZipInputStreamFactory.newInstance(new FileInputStream(theme));
            for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                String entryName = entry.getName();
                if ("styles".equals(entryName) || entryName.startsWith("styles/") || entryName.startsWith("styles\\")) {
                    stylesFound = true;
                } else if ("images".equals(entryName) || entryName.startsWith("images/") || entryName.startsWith("images\\")) {
                    imagesFound = true;
                }
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not verify language archive.", e);
            }
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not close zip input stream.", e);
                    }
                }
            }
        }
        return imagesFound && stylesFound;
    }

    private static void saveFile(File baseDir, String fileName, InputStream inputStream) throws IOException {
        if (isAccepted(fileName)) {
            String dirName = "";
            if (fileName.contains("/")) {
                dirName = fileName.substring(0, fileName.lastIndexOf("/"));
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            } else if (fileName.contains("\\")) {
                dirName = fileName.substring(0, fileName.lastIndexOf("\\"));
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            }
            File uploadDir = new File(baseDir, dirName);
            if (!uploadDir.exists()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating upload directory \"" + uploadDir + "\".");
                }
                uploadDir.mkdirs();
            }
            if (uploadDir.isDirectory()) {
                FileOutputStream targetStream = new FileOutputStream(new File(uploadDir, fileName));
                IOUtils.copy(inputStream, targetStream);
                targetStream.close();
            }
        }
    }

    private static boolean isAccepted(String fileName) {
        return !(StringUtils.isEmpty(fileName) || fileName.endsWith("/") || fileName.endsWith("\\")) && !fileName.contains("__MACOSX/");
    }

    public static String addLanguage(File language) {
        if (isLanguageArchive(language)) {
            CodewaveZipInputStream zipInputStream = null;
            File languageDir = null;
            try {
                zipInputStream = CodewaveZipInputStreamFactory.newInstance(new FileInputStream(language));
                for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                    languageDir = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/languages");
                    if (entry.getName().startsWith("MyTunesRssWeb_") && entry.getName().endsWith(".properties")) {
                        saveFile(languageDir, entry.getName(), (InputStream)zipInputStream);
                    }
                }
            } catch (IOException e) {
                if (languageDir != null && languageDir.exists()) {
                    try {
                        FileUtils.deleteDirectory(languageDir);
                    } catch (IOException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not delete directory.", e);
                        }
                    }
                }
                return MyTunesRssUtils.getBundleString("error.couldNotExtractLanguage");
            } finally {
                if (zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    } catch (IOException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not close zip input stream.", e);
                        }
                    }
                }
            }
        } else {
            return MyTunesRssUtils.getBundleString("error.invalidLanguageFile");
        }
        return null;
    }

    private static boolean isLanguageArchive(File language) {
        CodewaveZipInputStream zipInputStream = null;
        try {
            zipInputStream = CodewaveZipInputStreamFactory.newInstance(new FileInputStream(language));
            for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                if (entry.getName().startsWith("MyTunesRssWeb_") && entry.getName().endsWith(".properties")) {
                    return true;
                }
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not verify language archive.", e);
            }
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not close zip input stream.", e);
                    }
                }
            }
        }
        return false;
    }

    public static String deleteTheme(String themeName) {
        try {
            File themeDir = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/themes/" + themeName);
            if (themeDir.isDirectory()) {
                FileUtils.deleteDirectory(themeDir);
            } else {
                return MyTunesRssUtils.getBundleString("error.deleteThemeNoDir");
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not delete theme \"" + themeName + "\".", e);
            }
            return MyTunesRssUtils.getBundleString("error.couldNotRemoveTheme");
        }
        return null;
    }

    public static String deleteLanguage(String languageCode) {
        try {
            File languageFile = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/languages/MyTunesRssWeb_" +
                languageCode + ".properties");
            if (languageFile.isFile()) {
                languageFile.delete();
            } else {
                return MyTunesRssUtils.getBundleString("error.deleteLanguageNoFile");
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not delete language \"" + languageCode + "\".", e);
            }
            return MyTunesRssUtils.getBundleString("error.couldNotRemoveLanguage");
        }
        return null;
    }

    public static File getBestLanguageFile(Locale locale) {
        String[] codes = locale.toString().split("_");
        List<String> fileNames = new ArrayList<String>();
        if (codes.length == 3) {
            fileNames.add("MyTunesRssWeb_" + codes[0] + "_" + codes[1] + "_" + codes[2] + ".properties");
        }
        if (codes.length >= 2) {
            fileNames.add("MyTunesRssWeb_" + codes[0] + "_" + codes[1] + ".properties");
        }
        fileNames.add("MyTunesRssWeb_" + codes[0] + ".properties");
        for (String fileName : fileNames) {
            try {
                File languageFile = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/languages/" + fileName);
                if (languageFile.isFile()) {
                    return languageFile;
                }
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Language file \"" + fileName + "\" not found.");
                }
            }
        }
        return null;
    }
}