/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.AddonsUtils;
import de.codewave.mytunesrss.ExternalSiteDefinition;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.vaadin.ComponentFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class AddonsConfigPanel extends MyTunesRssConfigPanel implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddonsConfigPanel.class);

    private Panel myThemesPanel;
    private Panel myLanguagesPanel;
    private Panel mySitesPanel;
    private Table myThemesTable;
    private Table myLanguagesTable;
    private Table mySitesTable;
    private Upload myUploadTheme;
    private Upload myUploadLanguage;
    private Button myAddSite;
    private File myUploadDir;

    public AddonsConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("addonsConfigPanel.caption"), componentFactory.createGridLayout(1, 4, true, true), componentFactory);
    }

    protected void init(Application application) {
        myThemesPanel = new Panel(getBundleString("addonsConfigPanel.caption.themes"), getComponentFactory().createVerticalLayout(true, true));
        myThemesTable = new Table();
        myThemesTable.addContainerProperty("name", String.class, null, getBundleString("addonsConfigPanel.themes.name"), null, null);
        myThemesTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myThemesPanel.addComponent(myThemesTable);
        myUploadTheme = new Upload(null, this);
        myUploadTheme.setButtonCaption(getBundleString("addonsConfigPanel.addTheme"));
        myUploadTheme.setImmediate(true);
        myUploadTheme.addListener((Upload.SucceededListener) this);
        myUploadTheme.addListener((Upload.FailedListener) this);
        myThemesPanel.addComponent(myUploadTheme);
        myLanguagesPanel = new Panel(getBundleString("addonsConfigPanel.caption.languages"), getComponentFactory().createVerticalLayout(true, true));
        myLanguagesTable = new Table();
        myLanguagesTable.addContainerProperty("code", String.class, null, getBundleString("addonsConfigPanel.languages.code"), null, null);
        myLanguagesTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myLanguagesPanel.addComponent(myLanguagesTable);
        myUploadLanguage = new Upload(null, this);
        myUploadLanguage.setButtonCaption(getBundleString("addonsConfigPanel.addLanguage"));
        myUploadLanguage.setImmediate(true);
        myUploadLanguage.addListener((Upload.SucceededListener) this);
        myUploadLanguage.addListener((Upload.FailedListener) this);
        myLanguagesPanel.addComponent(myUploadLanguage);
        mySitesPanel = new Panel(getBundleString("addonsConfigPanel.caption.sites"), getComponentFactory().createVerticalLayout(true, true));
        mySitesTable = new Table();
        mySitesTable.addContainerProperty("name", TextField.class, null, getBundleString("addonsConfigPanel.sites.name"), null, null);
        mySitesTable.addContainerProperty("type", Select.class, null, getBundleString("addonsConfigPanel.sites.type"), null, null);
        mySitesTable.addContainerProperty("url", TextField.class, null, getBundleString("addonsConfigPanel.sites.url"), null, null);
        mySitesTable.addContainerProperty("delete", Button.class, null, "", null, null);
        mySitesPanel.addComponent(mySitesTable);
        myAddSite = getComponentFactory().createButton("addonsConfigPanel.addSite", this);
        mySitesPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddSite));

        addComponent(myThemesPanel);
        addComponent(myLanguagesPanel);
        addComponent(mySitesPanel);

        addMainButtons(0, 3, 0, 3);

    }

    protected void initFromConfig(Application application) {
        refreshThemes();
        refreshLanguages();
        for (ExternalSiteDefinition site : MyTunesRss.CONFIG.getExternalSites()) {
            addSite(site);
        }
    }

    private void refreshLanguages() {
        myLanguagesTable.removeAllItems();
        List<AddonsUtils.LanguageDefinition> languages = new ArrayList<AddonsUtils.LanguageDefinition>(AddonsUtils.getLanguages(false));
        Collections.sort(languages);
        for (AddonsUtils.LanguageDefinition languageDefinition : languages) {
            myLanguagesTable.addItem(new Object[]{languageDefinition.getCode(), getComponentFactory().createButton("button.delete", this)}, languageDefinition.getCode());
        }
    }

    private void refreshThemes() {
        myThemesTable.removeAllItems();
        List<AddonsUtils.ThemeDefinition> themes = new ArrayList<AddonsUtils.ThemeDefinition>(AddonsUtils.getThemes(false));
        Collections.sort(themes);
        for (AddonsUtils.ThemeDefinition theme : themes) {
            myThemesTable.addItem(new Object[]{theme.getName(), getComponentFactory().createButton("button.delete", this)}, theme.getName());
        }
    }

    private void addSite(ExternalSiteDefinition site) {
        TextField name = new TextField(null, site.getName());
        name.setImmediate(true);
        name.addValidator(new SiteValidator("name", "type"));
        Select type = getComponentFactory().createSelect(null, Arrays.asList("album", "artist", "title"));
        type.setValue(site.getType());
        type.setImmediate(true);
        type.addValidator(new SiteValidator("type", "name"));
        TextField url = new TextField(null, site.getUrl());
        mySitesTable.addItem(new Object[]{name, type, url, getComponentFactory().createButton("button.delete", this)}, site);
        setTablePageLengths();
    }

    private void setTablePageLengths() {
        myThemesTable.setPageLength(Math.min(myThemesTable.getItemIds().size(), 10));
        myLanguagesTable.setPageLength(Math.min(myLanguagesTable.getItemIds().size(), 10));
        mySitesTable.setPageLength(Math.min(mySitesTable.getItemIds().size(), 10));
    }

    protected void writeToConfig() {
        for (ExternalSiteDefinition site : MyTunesRss.CONFIG.getExternalSites()) {
            MyTunesRss.CONFIG.removeExternalSite(site);
        }
        for (Object itemId : mySitesTable.getItemIds()) {
            MyTunesRss.CONFIG.addExternalSite(new ExternalSiteDefinition((String) getTableCellPropertyValue(mySitesTable, itemId, "type"), (String) getTableCellPropertyValue(mySitesTable, itemId, "name"), (String) getTableCellPropertyValue(mySitesTable, itemId, "url")));
        }
    }

    @Override
    protected boolean beforeSave() {
        return true;
    }

    @Override
    protected boolean beforeReset() {
        return true;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }

    public OutputStream receiveUpload(String filename, String mimeType) {
        try {
            myUploadDir = new File(MyTunesRssUtils.getCacheDataPath() + "/addon-upload");
            if (!myUploadDir.isDirectory()) {
                myUploadDir.mkdir();
            }
            return new FileOutputStream(new File(myUploadDir, filename));
        } catch (IOException e) {
            throw new RuntimeException("Could not receive upload.", e);
        }
    }

    public void uploadFailed(Upload.FailedEvent event) {
        FileUtils.deleteQuietly(myUploadDir);
        getApplication().showError("addonsConfigPanel.error.uploadFailed");
    }

    public void uploadSucceeded(Upload.SucceededEvent event) {
        if (event.getSource() == myUploadTheme) {
            AddonsUtils.AddFileResult result = AddonsUtils.addTheme(new File(myUploadDir, event.getFilename()));
            if (result == AddonsUtils.AddFileResult.InvalidFile) {
                getApplication().showError("addonsConfigPanel.error.invalidTheme");
            } else if (result == AddonsUtils.AddFileResult.ExtractFailed) {
                getApplication().showError("addonsConfigPanel.error.extractFailed");
            } else {
                getApplication().showInfo("addonsConfigPanel.info.themeOk");
                refreshThemes();
            }
        } else {
            AddonsUtils.AddFileResult result = AddonsUtils.addLanguage(new File(myUploadDir, event.getFilename()));
            if (result == AddonsUtils.AddFileResult.InvalidFile) {
                getApplication().showError("addonsConfigPanel.error.invalidLanguage");
            } else if (result == AddonsUtils.AddFileResult.ExtractFailed) {
                getApplication().showError("addonsConfigPanel.error.extractFailed");
            } else {
                getApplication().showInfo("addonsConfigPanel.info.languageOk");
                refreshLanguages();
            }
        }
        FileUtils.deleteQuietly(myUploadDir);
    }

    public class SiteValidator extends AbstractStringValidator {

        private String myProperty;
        private String myOtherProperty;

        public SiteValidator(String property, String otherProperty) {
            super(getBundleString("addonsConfigPanel.error.duplicateSite"));
            myProperty = property;
            myOtherProperty = otherProperty;
        }

        @Override
        protected boolean isValidString(String s) {
            Set<String> others = new HashSet<String>();
            for (Object itemId : mySitesTable.getItemIds()) {
                if (StringUtils.equals((String) getTableCellPropertyValue(mySitesTable, itemId, myProperty), s)) {
                    String other = (String) getTableCellPropertyValue(mySitesTable, itemId, myOtherProperty);
                    if (others.contains(other)) {
                        return false;
                    }
                    others.add(other);
                }
            }
            return true;
        }
    }
}