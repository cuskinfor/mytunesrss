/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.AddonsConfigMBean
 */
public interface AddonsConfigMBean {
    String[] getThemes();

    String[] getLanguages();

    String addTheme(String themePath);

    String addLanguage(String languagePath);

    String removeTheme(String themeName);

    String removeLanguage(String languageCode);
}