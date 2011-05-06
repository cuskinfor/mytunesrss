/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.CodewaveZipInputStream;
import java.util.zip.CodewaveZipInputStreamFactory;
import java.util.zip.ZipEntry;

/**
 * de.codewave.mytunesrss.AddonsUtils
 */
public class AddonsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AddonsUtils.class);

    public static Collection<ThemeDefinition> getThemes(boolean builtinThemes) {
        Set<ThemeDefinition> themeSet = new HashSet<ThemeDefinition>();
        try {
            File themesDir = new File(MyTunesRssUtils.getBuiltinAddonsPath() + "/themes");
            if (!themesDir.exists() && builtinThemes) {
                themesDir.mkdirs();
            }
            if (builtinThemes) {
                themeSet.addAll(getThemesFromDir(themesDir));
            }
            themesDir = new File(MyTunesRssUtils.getPreferencesDataPath() + "/themes");
            if (!themesDir.exists()) {
                themesDir.mkdirs();
            }
            themeSet.addAll(getThemesFromDir(themesDir));
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Problem reading existing themes.", e);
            }
        }
        List<ThemeDefinition> themes = new ArrayList<ThemeDefinition>(themeSet);
        Collections.sort(themes, new Comparator<ThemeDefinition>() {
            public int compare(ThemeDefinition o1, ThemeDefinition o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return themes;
    }

    private static Collection<ThemeDefinition> getThemesFromDir(File themesDir) throws IOException {
        Collection<ThemeDefinition> themes = new HashSet<ThemeDefinition>();
        if (themesDir.isDirectory()) {
            for (String theme : themesDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return new File(dir, name + "/images").isDirectory() && new File(dir, name + "/styles").isDirectory();
                }
            })) {
                File readme = new File(themesDir + "/" + theme, "readme.txt");
                if (readme.isFile()) {
                    themes.add(new ThemeDefinition(theme, FileUtils.readFileToString(readme, "UTF-8")));
                } else {
                    themes.add(new ThemeDefinition(theme, null));
                }
            }
        }
        return themes;
    }

    public static Collection<LanguageDefinition> getLanguages(boolean builtinLanguages) {
        Set<LanguageDefinition> languageSet = new HashSet<LanguageDefinition>();
        try {
            File languagesDir = new File(MyTunesRssUtils.getBuiltinAddonsPath() + "/languages");
            if (!languagesDir.exists() && builtinLanguages) {
                languagesDir.mkdirs();
            }
            if (builtinLanguages) {
                languageSet.add(new LanguageDefinition("de", null));
                languageSet.add(new LanguageDefinition("en", null));
                languageSet.addAll(getLanguagesFromDir(languagesDir));
            }
            languagesDir = new File(MyTunesRssUtils.getPreferencesDataPath() + "/languages");
            if (!languagesDir.exists()) {
                languagesDir.mkdirs();
            }
            languageSet.addAll(getLanguagesFromDir(languagesDir));
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Problem reading existing themes and languages.", e);
            }
        }
        List<LanguageDefinition> languages = new ArrayList<LanguageDefinition>(languageSet);
        Collections.sort(languages, new Comparator<LanguageDefinition>() {
            public int compare(LanguageDefinition o1, LanguageDefinition o2) {
                return o1.getCode().compareTo(o2.getCode());
            }
        });
        return languages;
    }

    private static Collection<LanguageDefinition> getLanguagesFromDir(File languagesDir) throws IOException {
        Collection<LanguageDefinition> languages = new HashSet<LanguageDefinition>();
        if (languagesDir.isDirectory()) {
            for (String language : languagesDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith("MyTunesRssWeb_") && name.endsWith(".properties");
                }
            })) {
                String languageCode = getLanguageCode(language);
                if (languageCode != null) {
                    File readme = new File(languagesDir, language + ".readme.txt");
                    if (readme.isFile()) {
                        languages.add(new LanguageDefinition(languageCode, FileUtils.readFileToString(readme, "UTF-8")));
                    } else {
                        languages.add(new LanguageDefinition(languageCode, null));
                    }
                }
            }
        }
        return languages;
    }

    private static String getLanguageCode(String language) {
        int underscoreIndex = language.indexOf('_');
        if (underscoreIndex > -1 && underscoreIndex + 1 < language.length()) {
            return language.substring("MyTunesRssWeb_".length(), language.length() - ".properties".length());
        }
        return null;
    }

    public static AddFileResult addTheme(String themeName, File theme) {
        if (isThemeArchive(theme)) {
            File themeDir = null;
            CodewaveZipInputStream zipInputStream = null;
            try {
                zipInputStream = CodewaveZipInputStreamFactory.newInstance(new FileInputStream(theme));
                for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                    themeDir = new File(MyTunesRssUtils.getPreferencesDataPath() + "/themes/" + themeName);
                    saveFile(themeDir, entry.getName(), (InputStream) zipInputStream);
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
                return AddFileResult.ExtractFailed;
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
            return AddFileResult.InvalidFile;
        }
        return AddFileResult.Ok;
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

    public static AddFileResult addLanguage(File language) {
        if (isLanguageArchive(language)) {
            CodewaveZipInputStream zipInputStream = null;
            File languageDir = null;
            try {
                zipInputStream = CodewaveZipInputStreamFactory.newInstance(new FileInputStream(language));
                String languageCode = null;
                byte[] readme = null;
                for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                    languageDir = new File(MyTunesRssUtils.getPreferencesDataPath() + "/languages");
                    if (entry.getName().startsWith("MyTunesRssWeb_") && entry.getName().endsWith(".properties")) {
                        saveFile(languageDir, entry.getName(), (InputStream) zipInputStream);
                        languageCode = getLanguageCode(entry.getName());
                    }
                    if (entry.getName().equals("readme.txt")) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        IOUtils.copy((InputStream) zipInputStream, baos);
                        readme = baos.toByteArray();
                    }
                }
                if (languageCode != null && readme != null) {
                    saveFile(languageDir, "MyTunesRssWeb_" + languageCode + ".properties.readme.txt", new ByteArrayInputStream(readme));
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
                return AddFileResult.ExtractFailed;
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
            return AddFileResult.InvalidFile;
        }
        return AddFileResult.Ok;
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
        if (StringUtils.equals(MyTunesRss.CONFIG.getDefaultUserInterfaceTheme(), themeName)) {
            MyTunesRss.CONFIG.setDefaultUserInterfaceTheme(null);
        }
        try {
            File themeDir = new File(MyTunesRssUtils.getPreferencesDataPath() + "/themes/" + themeName);
            if (themeDir.isDirectory()) {
                FileUtils.deleteDirectory(themeDir);
            } else {
                return MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.deleteThemeNoDir");
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not delete theme \"" + themeName + "\".", e);
            }
            return MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.couldNotRemoveTheme");
        }
        return null;
    }

    public static String deleteLanguage(String languageCode) {
        try {
            File languageFile = new File(
                    MyTunesRssUtils.getPreferencesDataPath() + "/languages/MyTunesRssWeb_" + languageCode + ".properties");
            if (languageFile.isFile()) {
                languageFile.delete();
                File readmeFile = new File(
                        MyTunesRssUtils.getPreferencesDataPath() + "/languages/MyTunesRssWeb_" + languageCode +
                                ".properties.readme.txt");
                if (readmeFile.isFile()) {
                    readmeFile.delete();
                }
            } else {
                return MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.deleteLanguageNoFile");
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not delete language \"" + languageCode + "\".", e);
            }
            return MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.couldNotRemoveLanguage");
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
                File languageFile = new File(MyTunesRssUtils.getPreferencesDataPath() + "/languages/" + fileName);
                if (languageFile.isFile()) {
                    return languageFile;
                }
                languageFile = new File(MyTunesRssUtils.getBuiltinAddonsPath() + "/languages/" + fileName);
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

    public static class LanguageDefinition implements Comparable<LanguageDefinition> {
        private String myCode;
        private String myInfo;

        public LanguageDefinition(String code, String info) {
            myCode = code;
            myInfo = info;
        }

        public String getCode() {
            return myCode;
        }

        public void setCode(String code) {
            myCode = code;
        }

        public String getInfo() {
            return myInfo;
        }

        public void setInfo(String info) {
            myInfo = info;
        }

        @Override
        public String toString() {
            return myCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof LanguageDefinition)) {
                return false;
            }
            return StringUtils.equals(myCode, ((LanguageDefinition) obj).myCode);
        }

        @Override
        public int hashCode() {
            return myCode != null ? myCode.hashCode() : 0;
        }

        public int compareTo(LanguageDefinition o) {
            return myCode.compareTo(o.getCode());
        }
    }

    public static class ThemeDefinition implements Comparable<ThemeDefinition> {
        private String myName;
        private String myInfo;

        public ThemeDefinition(String name, String info) {
            myName = name;
            myInfo = info;
        }

        public String getName() {
            return myName;
        }

        public void setName(String name) {
            myName = name;
        }

        public String getInfo() {
            return myInfo;
        }

        public void setInfo(String info) {
            myInfo = info;
        }

        @Override
        public String toString() {
            return myName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof ThemeDefinition)) {
                return false;
            }
            return StringUtils.equals(myName, ((ThemeDefinition) obj).myName);
        }

        @Override
        public int hashCode() {
            return myName != null ? myName.hashCode() : 0;
        }

        public int compareTo(ThemeDefinition o) {
            return myName.compareTo(o.getName());
        }
    }

    public static enum AddFileResult {
        ExtractFailed(), InvalidFile(), Ok();
    }
}