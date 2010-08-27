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

public class AddonsConfigPanel extends MyTunesRssConfigPanel implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

    private static final Logger LOG = LoggerFactory.getLogger(AddonsConfigPanel.class);

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
    private File myUploadDir;
    private Set<FlashPlayerConfig> myFlashPlayers;

    public void attach() {
        init(getApplication().getBundleString("addonsConfigPanel.caption"), getApplication().getComponentFactory().createGridLayout(1, 5, true, true));
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
        mySitesTable.addContainerProperty("name", TextField.class, null, getBundleString("addonsConfigPanel.sites.name"), null, null);
        mySitesTable.addContainerProperty("type", Select.class, null, getBundleString("addonsConfigPanel.sites.type"), null, null);
        mySitesTable.addContainerProperty("url", TextField.class, null, getBundleString("addonsConfigPanel.sites.url"), null, null);
        mySitesTable.addContainerProperty("delete", Button.class, null, "", null, null);
        mySitesPanel.addComponent(mySitesTable);
        myAddSite = getComponentFactory().createButton("addonsConfigPanel.addSite", this);
        mySitesPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddSite));
        myFlashPlayersPanel = new Panel(getBundleString("addonsConfigPanel.caption.flash"), getComponentFactory().createVerticalLayout(true, true));
        myFlashPlayersTable = new Table();
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

        attach(0, 4, 0, 4);

        initFromConfig();
    }

    protected void initFromConfig() {
        myFlashPlayers = MyTunesRss.CONFIG.getFlashPlayers();
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
        List<FlashPlayerConfig> flashPlayers = new ArrayList<FlashPlayerConfig>(myFlashPlayers);
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
        for (AddonsUtils.ThemeDefinition theme : themes) {
            myThemesTable.addItem(new Object[]{theme.getName(), createTableRowButton("button.delete", this, theme.getName(), "DeleteTheme")}, theme.getName());
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
        mySitesTable.addItem(new Object[]{name, type, url, createTableRowButton("button.delete", this, site, "DeleteSite")}, site);
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
        MyTunesRss.CONFIG.clearFlashPlayer();
        for (FlashPlayerConfig flashPlayer : myFlashPlayers) {
            MyTunesRss.CONFIG.addFlashPlayer(flashPlayer);
        }
        MyTunesRss.CONFIG.save();    }

    @Override
    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(mySitesTable)) {
            getApplication().showError("error.formInvalid");
            return false;
        }
        return true;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() instanceof TableRowButton) {
            final TableRowButton tableRowButton = (TableRowButton) clickEvent.getSource();
            if ("EditPlayer".equals(tableRowButton.getData())) {
                FlashPlayerEditPanel flashPlayerEditPanel = new FlashPlayerEditPanel(this, (FlashPlayerConfig) getFlashPlayerConfig((String) tableRowButton.getItemId()).clone());
                SinglePanelWindow flashPlayerEditWindow = new SinglePanelWindow(50, Sizeable.UNITS_EM, null, getBundleString("flashPlayerEditPanel.caption"), flashPlayerEditPanel);
                flashPlayerEditWindow.show(getWindow());
            } else {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                final String name = tableRowButton.getItem().getItemProperty("name").getValue().toString();
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("addonsConfigPanel.optionWindow" + tableRowButton.getData().toString() + ".caption"), getBundleString("addonsConfigPanel.optionWindow" + tableRowButton.getData().toString() + ".message", name), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            tableRowButton.deleteTableRow();
                            if (tableRowButton.getData().toString().equals("DeleteTheme")) {
                                AddonsUtils.deleteTheme(name);
                            } else if (tableRowButton.getData().toString().equals("DeleteLanguage")) {
                                AddonsUtils.deleteLanguage(name);
                            } else if (tableRowButton.getData().toString().equals("DeleteSite")) {
                                mySitesTable.removeItem(tableRowButton.getItemId());
                            } else if (tableRowButton.getData().toString().equals("DeletePlayer")) {
                                myFlashPlayersTable.removeItem(tableRowButton.getItemId());
                                FlashPlayerConfig config = new FlashPlayerConfig((String) tableRowButton.getItemId(), null, null);
                                myFlashPlayers.remove(config);
                                try {
                                    FileUtils.deleteQuietly(config.getBaseDir());
                                } catch (IOException e) {
                                    if (LOG.isErrorEnabled()) {
                                        LOG.error("Could not get flash player base directory.");
                                    }
                                }
                            }
                            setTablePageLengths();
                        }
                    }
                }.show(getApplication().getMainWindow());
            }
        } else if (clickEvent.getSource() == myAddSite) {
            addSite(new ExternalSiteDefinition("album", "new site", "http://"));
            setTablePageLengths();
        } else if (clickEvent.getSource() == myAddFlashPlayer) {
            FlashPlayerEditPanel flashPlayerEditPanel = new FlashPlayerEditPanel(this, new FlashPlayerConfig(UUID.randomUUID().toString(), "", ""));
            SinglePanelWindow flashPlayerEditWindow = new SinglePanelWindow(50, Sizeable.UNITS_EM, null, getBundleString("flashPlayerEditPanel.caption"), flashPlayerEditPanel);
            flashPlayerEditWindow.show(getWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private FlashPlayerConfig getFlashPlayerConfig(String itemId) {
        for (FlashPlayerConfig flashPlayerConfig : myFlashPlayers) {
            if (flashPlayerConfig.getId().equals(itemId)) {
                return flashPlayerConfig;
            }
        }
        return null;
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

    void addOrUpdatePlayer(FlashPlayerConfig flashPlayerConfig) {
        myFlashPlayers.remove(flashPlayerConfig);
        myFlashPlayers.add(flashPlayerConfig);
        refreshFlashPlayers();
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