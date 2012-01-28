/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.addons.AddonsUtils;
import de.codewave.mytunesrss.addons.LanguageDefinition;
import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.*;

public abstract class DownloadLanguagePanel extends MyTunesRssConfigPanel {

    private List<LanguageDefinition> myLanguages;
    private Table myLanguageTable;

    public DownloadLanguagePanel(Collection<LanguageDefinition> languages) {
        myLanguages = new ArrayList<LanguageDefinition>(languages);
    }

    @Override
    public void attach() {
        super.attach();
        init(null, getComponentFactory().createGridLayout(1, 1, true, true));
        myLanguageTable = new Table();
        myLanguageTable.setCacheRate(50);
        myLanguageTable.addContainerProperty("name", String.class, null, getBundleString("addonsConfigPanel.languages.name"), null, null);
        myLanguageTable.addContainerProperty("version", String.class, null, getBundleString("addonsConfigPanel.languages.version"), null, null);
        myLanguageTable.addContainerProperty("author", String.class, null, getBundleString("addonsConfigPanel.languages.author"), null, null);
        myLanguageTable.addContainerProperty("download", Button.class, null, "", null, null);
        myLanguageTable.setEditable(false);
        addComponent(myLanguageTable);
        final Locale locale = getApplication().getLocale();
        Collections.sort(myLanguages, new Comparator<LanguageDefinition>() {
            public int compare(LanguageDefinition languageDefinition1, LanguageDefinition languageDefinition2) {
                return new Locale(languageDefinition1.getCode()).getDisplayName(locale).compareTo(new Locale(languageDefinition2.getCode()).getDisplayName(locale));
            }
        });
        for (LanguageDefinition language : myLanguages) {
            myLanguageTable.addItem(new Object[]{
                    new Locale(language.getCode()).getDisplayName(locale),
                    language.getVersion(),
                    language.getNick(),
                    createTableRowButton("button.download", this, language.getId(), "DownloadLangiage")}, language.getId());
        }
        setTablePageLengths();
    }

    private void setTablePageLengths() {
        myLanguageTable.setPageLength(Math.min(myLanguageTable.getItemIds().size(), 20));
    }

    @Override
    protected void writeToConfig() {
        // intentionally left blank, nothing to write
    }

    @Override
    protected void initFromConfig() {
        // intentionally left blank, nothing to init
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() instanceof TableRowButton) {
            TableRowButton button = (TableRowButton) (clickEvent.getSource());
            Integer id = (Integer) button.getItemId();
            boolean result = AddonsUtils.downloadLanguage(id);
            debug("download result = " + result);
            if (result) {
                myLanguageTable.removeItem(button.getItemId());
                setTablePageLengths();
                onSuccessfulDownload();
                if (myLanguageTable.getItemIds().isEmpty()) {
                    getWindow().getParent().removeWindow(getWindow());
                }
            }
        }
    }

    public abstract void onSuccessfulDownload();
}