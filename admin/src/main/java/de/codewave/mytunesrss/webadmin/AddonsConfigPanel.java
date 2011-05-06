/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.*;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.SinglePanelWindow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AddonsConfigPanel extends MyTunesRssConfigPanel implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

    private static final Logger LOG = LoggerFactory.getLogger(AddonsConfigPanel.class);

    private static final String PREFIX = "upload_addon_";

    private static final Object DEFAULT_UI_THEME_ID = UUID.randomUUID();

    private Panel myThemesPanel;
    private Panel myLanguagesPanel;
    private Panel mySitesPanel;
    private Panel myFlashPlayersPanel;
    private Table myThemesTable;
    private Table myLanguagesTable;
    private Table mySitesTable;
    private Table myFlashPlayersTable;
    private Upload myUploadTheme;
    private Upload myUploadLanguage;
    private Button myAddSite;
    private Button myAddFlashPlayer;

    public void attach() {
        super.attach();
        init(getApplication().getBundleString("addonsConfigPanel.caption"), getApplication().getComponentFactory().createGridLayout(1, 5, true, true));
        myThemesPanel = new Panel(getBundleString("addonsConfigPanel.caption.themes"), getComponentFactory().createVerticalLayout(true, true));
        myThemesTable = new Table();
        myThemesTable.setCacheRate(50);
        myThemesTable.addContainerProperty("defmarker", Boolean.class, null, getBundleString("addonsConfigPanel.themes.defmarker"), null, null);
        myThemesTable.addContainerProperty("name", String.class, null, getBundleString("addonsConfigPanel.themes.name"), null, null);
        myThemesTable.addContainerProperty("default", Button.class, null, "", null, null);
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
        myLanguagesTable.setCacheRate(50);
        myLanguagesTable.addContainerProperty("name", String.class, null, getBundleString("addonsConfigPanel.languages.code"), null, null);
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
        mySitesTable.setCacheRate(50);
        mySitesTable.addContainerProperty("name", TextField.class, null, getBundleString("addonsConfigPanel.sites.name"), null, null);
        mySitesTable.addContainerProperty("type", Select.class, null, getBundleString("addonsConfigPanel.sites.type"), null, null);
        mySitesTable.addContainerProperty("url", TextField.class, null, getBundleString("addonsConfigPanel.sites.url"), null, null);
        mySitesTable.addContainerProperty("delete", Button.class, null, "", null, null);
        mySitesPanel.addComponent(mySitesTable);
        myAddSite = getComponentFactory().createButton("addonsConfigPanel.addSite", this);
        mySitesPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddSite));
        myFlashPlayersPanel = new Panel(getBundleString("addonsConfigPanel.caption.flash"), getComponentFactory().createVerticalLayout(true, true));
        myFlashPlayersTable = new Table();
        myFlashPlayersTable.setCacheRate(50);
        myFlashPlayersTable.addContainerProperty("name", String.class, null, getBundleString("addonsConfigPanel.flash.name"), null, null);
        myFlashPlayersTable.addContainerProperty("edit", Button.class, null, "", null, null);
        myFlashPlayersTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myFlashPlayersPanel.addComponent(myFlashPlayersTable);
        myAddFlashPlayer = getComponentFactory().createButton("addonsConfigPanel.addFlashPlayer", this);
        myFlashPlayersPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddFlashPlayer));
        addComponent(myThemesPanel);
        addComponent(myLanguagesPanel);
        addComponent(mySitesPanel);
        addComponent(myFlashPlayersPanel);

        addDefaultComponents(0, 4, 0, 4, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        refreshThemes();
        refreshLanguages();
        refreshExternalSites();
        refreshFlashPlayers();
        setTablePageLengths();
    }

    private void refreshExternalSites() {
        mySitesTable.removeAllItems();
        for (ExternalSiteDefinition site : MyTunesRss.CONFIG.getExternalSites()) {
            addSite(site);
        }
    }

    private void refreshFlashPlayers() {
        myFlashPlayersTable.removeAllItems();
        List<FlashPlayerConfig> flashPlayers = new ArrayList<FlashPlayerConfig>(MyTunesRss.CONFIG.getFlashPlayers());
        Collections.sort(flashPlayers);
        for (FlashPlayerConfig flashPlayer : flashPlayers) {
            myFlashPlayersTable.addItem(new Object[]{flashPlayer.getName(), createTableRowButton("button.edit", this, flashPlayer.getId(), "EditPlayer"), createTableRowButton("button.delete", this, flashPlayer.getId(), "DeletePlayer")}, flashPlayer.getId());
        }
    }

    private void refreshLanguages() {
        myLanguagesTable.removeAllItems();
        List<AddonsUtils.LanguageDefinition> languages = new ArrayList<AddonsUtils.LanguageDefinition>(AddonsUtils.getLanguages(false));
        Collections.sort(languages);
        for (AddonsUtils.LanguageDefinition languageDefinition : languages) {
            myLanguagesTable.addItem(new Object[]{languageDefinition.getCode(), createTableRowButton("button.delete", this, languageDefinition.getCode(), "DeleteLanguage")}, languageDefinition.getCode());
        }
    }

    private void refreshThemes() {
        myThemesTable.removeAllItems();
        List<AddonsUtils.ThemeDefinition> themes = new ArrayList<AddonsUtils.ThemeDefinition>(AddonsUtils.getThemes(false));
        Collections.sort(themes);
        Button disabledDeleteButton = createTableRowButton("button.delete", this, DEFAULT_UI_THEME_ID, "DeleteTheme");
        disabledDeleteButton.setEnabled(false);
        myThemesTable.addItem(new Object[]{StringUtils.isEmpty(MyTunesRss.CONFIG.getDefaultUserInterfaceTheme()), getBundleString("addonsConfigPanel.themes.defname"), createTableRowButton("button.default", this, DEFAULT_UI_THEME_ID, "DefaultTheme"), disabledDeleteButton}, DEFAULT_UI_THEME_ID);
        for (AddonsUtils.ThemeDefinition theme : themes) {
            myThemesTable.addItem(new Object[]{StringUtils.equals(MyTunesRss.CONFIG.getDefaultUserInterfaceTheme(), theme.getName()), theme.getName(), createTableRowButton("button.default", this, theme.getName(), "DefaultTheme"), createTableRowButton("button.delete", this, theme.getName(), "DeleteTheme")}, theme.getName());
        }
    }

    private void addSite(ExternalSiteDefinition site) {
        SmartTextField name = new SmartTextField(null, site.getName());
        name.setImmediate(true);
        name.addValidator(new SiteValidator("name", "type"));
        Select type = getComponentFactory().createSelect(null, Arrays.asList("album", "artist", "title"));
        type.setValue(site.getType());
        type.setImmediate(true);
        type.addValidator(new SiteValidator("type", "name"));
        SmartTextField url = new SmartTextField(null, site.getUrl());
        url.setImmediate(true);
        url.addValidator(new AbstractStringValidator(getBundleString("addonsConfigPanel.error.invalidSiteUrl")) {
            @Override
            protected boolean isValidString(String s) {
                if (s.contains("{KEYWORD}")) {
                    try {
                        new URL(s.replace("{KEYWORD}", "dummy"));
                        return true;
                    } catch (MalformedURLException e) {
                        // ignore, will return false in the end
                    }
                }
                return false;
            }
        });
        Object itemId = UUID.randomUUID().toString();
        mySitesTable.addItem(new Object[]{name, type, url, createTableRowButton("button.delete", this, itemId, "DeleteSite")}, itemId);
        setTablePageLengths();
    }

    private void setTablePageLengths() {
        myThemesTable.setPageLength(Math.min(myThemesTable.getItemIds().size(), 10));
        myLanguagesTable.setPageLength(Math.min(myLanguagesTable.getItemIds().size(), 10));
        mySitesTable.setPageLength(Math.min(mySitesTable.getItemIds().size(), 10));
        myFlashPlayersTable.setPageLength(Math.min(myFlashPlayersTable.getItemIds().size(), 10));
    }

    protected void writeToConfig() {
        for (ExternalSiteDefinition site : MyTunesRss.CONFIG.getExternalSites()) {
            MyTunesRss.CONFIG.removeExternalSite(site);
        }
        for (Object itemId : mySitesTable.getItemIds()) {
            MyTunesRss.CONFIG.addExternalSite(new ExternalSiteDefinition((String) getTableCellPropertyValue(mySitesTable, itemId, "type"), (String) getTableCellPropertyValue(mySitesTable, itemId, "name"), (String) getTableCellPropertyValue(mySitesTable, itemId, "url")));
        }
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(mySitesTable)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
            return false;
        }
        return true;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() instanceof TableRowButton) {
            final TableRowButton tableRowButton = (TableRowButton) clickEvent.getSource();
            if ("EditPlayer".equals(tableRowButton.getData())) {
                FlashPlayerEditPanel flashPlayerEditPanel = new FlashPlayerEditPanel(this, MyTunesRss.CONFIG.getFlashPlayer((String) tableRowButton.getItemId()));
                SinglePanelWindow flashPlayerEditWindow = new SinglePanelWindow(50, Sizeable.UNITS_EM, null, getBundleString("flashPlayerEditPanel.caption"), flashPlayerEditPanel);
                flashPlayerEditWindow.show(getWindow());
            } else if ("DefaultTheme".equals(tableRowButton.getData())) {
                String name = tableRowButton.getItemId() == DEFAULT_UI_THEME_ID ? null : tableRowButton.getItem().getItemProperty("name").getValue().toString();
                MyTunesRss.CONFIG.setDefaultUserInterfaceTheme(name);
                refreshThemes();
            } else {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                final String name = tableRowButton.getItem().getItemProperty("name").getValue().toString();
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("addonsConfigPanel.optionWindow" + tableRowButton.getData().toString() + ".caption"), getBundleString("addonsConfigPanel.optionWindow" + tableRowButton.getData().toString() + ".message", name), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            if ("DeleteTheme".equals(tableRowButton.getData().toString())) {
                                AddonsUtils.deleteTheme(name);
                            } else if ("DeleteLanguage".equals(tableRowButton.getData().toString())) {
                                AddonsUtils.deleteLanguage(name);
                            } else if ("DeletePlayer".equals(tableRowButton.getData().toString())) {
                                FlashPlayerConfig removedConfig = MyTunesRss.CONFIG.removeFlashPlayer((String) tableRowButton.getItemId());
                                if (removedConfig != null) {
                                    try {
                                        FileUtils.deleteQuietly(removedConfig.getBaseDir());
                                    } catch (IOException e) {
                                        if (LOG.isErrorEnabled()) {
                                            LOG.error("Could not get flash player base directory.");
                                        }
                                    }
                                }
                            }
                            tableRowButton.deleteTableRow();
                            setTablePageLengths();
                        }
                    }
                }.show(getWindow());
            }
        } else if (clickEvent.getSource() == myAddSite) {
            addSite(new ExternalSiteDefinition("album", "new site", "http://"));
            setTablePageLengths();
        } else if (clickEvent.getSource() == myAddFlashPlayer) {
            FlashPlayerEditPanel flashPlayerEditPanel = new FlashPlayerEditPanel(this, new FlashPlayerConfig(UUID.randomUUID().toString(), "", FlashPlayerConfig.DEFAULT_HTML, PlaylistFileType.Xspf, 640, 480, TimeUnit.SECONDS));
            SinglePanelWindow flashPlayerEditWindow = new SinglePanelWindow(50, Sizeable.UNITS_EM, null, getBundleString("flashPlayerEditPanel.caption"), flashPlayerEditPanel);
            flashPlayerEditWindow.show(getWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }

    public OutputStream receiveUpload(String filename, String mimeType) {
        try {
            return new FileOutputStream(new File(getUploadDir(), PREFIX + filename));
        } catch (IOException e) {
            throw new RuntimeException("Could not receive upload.", e);
        }
    }

    public void uploadFailed(Upload.FailedEvent event) {
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("addonsConfigPanel.error.uploadFailed");
        FileUtils.deleteQuietly(new File(getUploadDir(), PREFIX + event.getFilename()));
    }

    public void uploadSucceeded(Upload.SucceededEvent event) {
        if (event.getSource() == myUploadTheme) {
            AddonsUtils.AddFileResult result = AddonsUtils.addTheme(FilenameUtils.getBaseName(event.getFilename()), new File(getUploadDir(), PREFIX + event.getFilename()));
            if (result == AddonsUtils.AddFileResult.InvalidFile) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("addonsConfigPanel.error.invalidTheme");
            } else if (result == AddonsUtils.AddFileResult.ExtractFailed) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("addonsConfigPanel.error.extractFailed");
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("addonsConfigPanel.info.themeOk");
                refreshThemes();
            }
        } else {
            AddonsUtils.AddFileResult result = AddonsUtils.addLanguage(new File(getUploadDir(), PREFIX + event.getFilename()));
            if (result == AddonsUtils.AddFileResult.InvalidFile) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("addonsConfigPanel.error.invalidLanguage");
            } else if (result == AddonsUtils.AddFileResult.ExtractFailed) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("addonsConfigPanel.error.extractFailed");
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("addonsConfigPanel.info.languageOk");
                refreshLanguages();
            }
        }
        FileUtils.deleteQuietly(new File(getUploadDir(), PREFIX + event.getFilename()));
        setTablePageLengths();
    }

    void saveFlashPlayer(FlashPlayerConfig flashPlayerConfig) {
        if (MyTunesRss.CONFIG.getFlashPlayer(flashPlayerConfig.getName()) == null) {
            MyTunesRss.CONFIG.addFlashPlayer(flashPlayerConfig);
        }
        refreshFlashPlayers();
        setTablePageLengths();
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

    private String getUploadDir() {
        try {
            return MyTunesRssUtils.getCacheDataPath() + "/" + MyTunesRss.CACHEDIR_TEMP;
        } catch (IOException e) {
            throw new RuntimeException("Could not get cache path.");
        }
    }
}