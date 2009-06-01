package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.AddonsUtils;
import de.codewave.mytunesrss.MyTunesRssUtils;

import javax.management.NotCompliantMBeanException;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

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
        String error = AddonsUtils.addLanguage(new File(StringUtils.trimToEmpty(languagePath)));
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String addTheme(String themePath) {
        String error = AddonsUtils.addTheme(new File(StringUtils.trimToEmpty(themePath)));
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String[] getLanguages() {
        Collection<AddonsUtils.LanguageDefinition> collection = AddonsUtils.getLanguages(false);
        String[] result = new String[collection.size()];
        Iterator<AddonsUtils.LanguageDefinition> iter = collection.iterator();
        for (int i = 0; i < collection.size(); i++) {
            result[i] = iter.next().getCode();
        }
        return result;
    }

    public String[] getThemes() {
        Collection<AddonsUtils.ThemeDefinition> collection = AddonsUtils.getThemes(false);
        String[] result = new String[collection.size()];
        Iterator<AddonsUtils.ThemeDefinition> iter = collection.iterator();
        for (int i = 0; i < collection.size(); i++) {
            result[i] = iter.next().getName();
        }
        return result;
    }

    public String removeLanguage(String languageCode) {
        String error = AddonsUtils.deleteLanguage(StringUtils.trimToEmpty(languageCode));
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }

    public String removeTheme(String themeName) {
        String error = AddonsUtils.deleteTheme(StringUtils.trimToEmpty(themeName));
        onChange();
        return error != null ? error : MyTunesRssUtils.getBundleString("ok");
    }
}