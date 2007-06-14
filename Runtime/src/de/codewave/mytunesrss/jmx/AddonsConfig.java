package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;

import javax.management.*;
import java.io.*;
import java.util.*;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       Cologne Systems GmbH<br> <b>Creation Date:</b>
 * 01.03.2007
 *
 * @author Michael Descher
 * @version 1.0
 */
public class AddonsConfig extends MyTunesRssMBean implements AddonsConfigMBean {
    public AddonsConfig() throws NotCompliantMBeanException {
        super(AddonsConfigMBean.class);
    }

    public String addLanguage(String languagePath) {
        String error = AddonsUtils.addLanguage(new File(languagePath));
        return error != null ? error : MyTunesRss.BUNDLE.getString("ok");
    }

    public String addTheme(String themePath) {
        String error = AddonsUtils.addTheme(new File(themePath));
        return error != null ? error : MyTunesRss.BUNDLE.getString("ok");
    }

    public String[] getLanguages() {
        Collection<String> collection = AddonsUtils.getLanguages();
        return collection.toArray(new String[collection.size()]);
    }

    public String[] getThemes() {
        Collection<String> collection = AddonsUtils.getThemes();
        return collection.toArray(new String[collection.size()]);
    }

    public String removeLanguage(String languageCode) {
        String error = AddonsUtils.deleteLanguage(languageCode);
        return error != null ? error : MyTunesRss.BUNDLE.getString("ok");
    }

    public String removeTheme(String themeName) {
        String error = AddonsUtils.deleteTheme(themeName);
        return error != null ? error : MyTunesRss.BUNDLE.getString("ok");
    }
}