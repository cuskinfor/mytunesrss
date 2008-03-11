package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.AddonsUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;

import javax.management.NotCompliantMBeanException;
import java.io.File;
import java.util.Collection;

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
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String addTheme(String themePath) {
        String error = AddonsUtils.addTheme(new File(themePath));
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
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
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String removeTheme(String themeName) {
        String error = AddonsUtils.deleteTheme(themeName);
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String getWebWelcomeMessage() {
        return MyTunesRss.CONFIG.getWebWelcomeMessage();
    }

    public void setWebWelcomeMessage(String message) {
        MyTunesRss.CONFIG.setWebWelcomeMessage(message);
        onChange();
    }
}