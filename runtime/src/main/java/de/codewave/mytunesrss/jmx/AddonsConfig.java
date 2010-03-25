package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.AddonsUtils;
import de.codewave.mytunesrss.ExternalSiteDefinition;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.lang.StringUtils;

import javax.management.NotCompliantMBeanException;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class AddonsConfig extends MyTunesRssMBean implements AddonsConfigMBean {
    public AddonsConfig() throws NotCompliantMBeanException {
        super(AddonsConfigMBean.class);
    }

    public String addLanguage(String languagePath) {
        AddonsUtils.addLanguage(new File(StringUtils.trimToEmpty(languagePath)));
        onChange();
        return MyTunesRssUtils.getBundleString("ok");
    }

    public String addTheme(String themePath) {
        AddonsUtils.addTheme(new File(StringUtils.trimToEmpty(themePath)));
        onChange();
        return MyTunesRssUtils.getBundleString("ok");
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

    public String[] getExternalSites() {
        List<ExternalSiteDefinition> sites = MyTunesRss.CONFIG.getExternalSites();
        String[] result = new String[sites.size()];
        for (int i = 0; i < sites.size(); i++) {
            result[i] = MyTunesRssUtils.getBundleString("settings.editExternalSites.type." + sites.get(i).getType()) + " - " + sites.get(i).getName() + " -> " + sites.get(i).getUrl();
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

    public String addExternalSite(String type, String name, String url) {
        if (StringUtils.equals(type, "album") || StringUtils.equals(type, "artist") || StringUtils.equals(type, "title")) {
            if (StringUtils.isBlank(name)) {
                return MyTunesRssUtils.getBundleString("error.emptyExtSiteName");
            } else {
                if (StringUtils.contains(url, "{KEYWORD}")) {
                    ExternalSiteDefinition definition = new ExternalSiteDefinition(type, name, url);
                    if (MyTunesRss.CONFIG.hasExternalSite(definition, null)) {
                        return MyTunesRssUtils.getBundleString("error.duplicateExtSiteName");
                    } else {
                        MyTunesRss.CONFIG.addExternalSite(definition);
                        onChange();
                        return MyTunesRssUtils.getBundleString("ok");
                    }
                } else {
                    return MyTunesRssUtils.getBundleString("error.invalidExtSiteUrl");
                }
            }
        } else {
            return MyTunesRssUtils.getBundleString("error.invalidExtSiteType");
        }
    }

    public String removeExternalSite(String type, String name) {
        ExternalSiteDefinition definition = new ExternalSiteDefinition(type, name, null);
        if (MyTunesRss.CONFIG.hasExternalSite(definition, null)) {
            MyTunesRss.CONFIG.removeExternalSite(definition);
            onChange();
            return MyTunesRssUtils.getBundleString("ok");
        } else {
            return MyTunesRssUtils.getBundleString("error.noSuchExternalSite");
        }
    }

}