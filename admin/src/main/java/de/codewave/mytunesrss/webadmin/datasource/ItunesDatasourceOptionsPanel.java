/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.ItunesDatasourceConfig;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.mytunesrss.webadmin.MyTunesRssConfigPanel;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.*;

public class ItunesDatasourceOptionsPanel extends DatasourceOptionsPanel {

    private Form myMiscOptionsForm;
    private CheckBox myDeleteMissingFiles;
    private Table myIgnoreItunesPlaylists;
    private ItunesDatasourceConfig myConfig;

    public ItunesDatasourceOptionsPanel(DatasourcesConfigPanel datasourcesConfigPanel, ItunesDatasourceConfig config) {
        super(datasourcesConfigPanel);
        myConfig = config;
    }

    @Override
    public void attach() {
        super.attach();
        init(getBundleString("datasourceOptionsPanel.caption", myConfig.getDefinition()), getComponentFactory().createGridLayout(1, 5, true, true));

        addComponent(myPathReplacementsPanel);
        Panel ignorePlaylistsPanel = new Panel(getBundleString("datasourceOptionsPanel.caption.ignoreItunesPlaylists"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(ignorePlaylistsPanel);
        myIgnoreItunesPlaylists = new Table();
        myIgnoreItunesPlaylists.setCacheRate(50);
        myIgnoreItunesPlaylists.addContainerProperty("check", CheckBox.class, null, "", null, null);
        myIgnoreItunesPlaylists.addContainerProperty("type", ItunesPlaylistType.class, null, getBundleString("datasourceOptionsPanel.ignoreItunesPlaylistType"), null, null);
        myIgnoreItunesPlaylists.setEditable(false);
        List<ItunesPlaylistType> types = new ArrayList<ItunesPlaylistType>(Arrays.asList(ItunesPlaylistType.values()));
        types.remove(ItunesPlaylistType.Master); // "Master" type is always ignored
        Collections.sort(types, new Comparator<ItunesPlaylistType>() {
            public int compare(ItunesPlaylistType o1, ItunesPlaylistType o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (ItunesPlaylistType type : types) {
            myIgnoreItunesPlaylists.addItem(new Object[] {new CheckBox(), type}, type.name());
        }
        ignorePlaylistsPanel.addComponent(myIgnoreItunesPlaylists);

        addComponent(myImageMappingsPanel);

        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myDeleteMissingFiles = getComponentFactory().createCheckBox("datasourceOptionsPanel.itunesDeleteMissingFiles");
        myMiscOptionsForm.addField(myDeleteMissingFiles, myDeleteMissingFiles);
        myMiscOptionsForm.addField(myArtistDropWords, myArtistDropWords);
        myMiscOptionsForm.addField(myDisabledMp4Codecs, myDisabledMp4Codecs);
        myMiscOptionsForm.addField(myTrackImageImportType, myTrackImageImportType);
        addComponent(getComponentFactory().surroundWithPanel(myMiscOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.misc")));

        addDefaultComponents(0, 4, 0, 4, false);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myConfig.setDeleteMissingFiles(myDeleteMissingFiles.booleanValue());
        myConfig.setArtistDropWords(myArtistDropWords.getStringValue(""));
        myConfig.setDisabledMp4Codecs(myDisabledMp4Codecs.getStringValue(""));
        myConfig.clearPathReplacements();
        for (Object itemId : myPathReplacements.getItemIds()) {
            myConfig.addPathReplacement(new ReplacementRule((String) getTableCellPropertyValue(myPathReplacements, itemId, "search"), (String) getTableCellPropertyValue(myPathReplacements, itemId, "replace")));
        }
        myConfig.clearIgnorePlaylists();
        for (Object itemId : myIgnoreItunesPlaylists.getItemIds()) {
            boolean checked = (Boolean)getTableCellPropertyValue(myIgnoreItunesPlaylists, itemId, "check");
            if (checked) {
                ItunesPlaylistType type = ((ItunesPlaylistType) getTableCellItemValue(myIgnoreItunesPlaylists, itemId, "type"));
                myConfig.addIgnorePlaylist(type);
            }
        }
        List<ReplacementRule> mappings = new ArrayList<ReplacementRule>();
        for (Object itemId : myTrackImageMappingsTable.getItemIds()) {
            mappings.add(new ReplacementRule((String) getTableCellPropertyValue(myTrackImageMappingsTable, itemId, "search"), (String) getTableCellPropertyValue(myTrackImageMappingsTable, itemId, "replace")));
        }
        myConfig.setTrackImageMappings(mappings);
        myConfig.setTrackImageImportType(((ImageImportTypeRepresentation) myTrackImageImportType.getValue()).getImageImportType());
    }

    @Override
    protected void initFromConfig() {
        myDeleteMissingFiles.setValue(myConfig.isDeleteMissingFiles());
        myArtistDropWords.setValue(myConfig.getArtistDropWords());
        myDisabledMp4Codecs.setValue(myConfig.getDisabledMp4Codecs());
        myTrackImageImportType.setValue(IMPORT_TYPE_MAPPINGS.get(myConfig.getTrackImageImportType()));
        myPathReplacements.removeAllItems();
        for (ReplacementRule replacement : myConfig.getPathReplacements()) {
            addPathReplacement(replacement);
        }
        Set<ItunesPlaylistType> itunesPlaylists = myConfig.getIgnorePlaylists();
        for (Object itemId : myIgnoreItunesPlaylists.getItemIds()) {
            ItunesPlaylistType type = ((ItunesPlaylistType) getTableCellItemValue(myIgnoreItunesPlaylists, itemId, "type"));
            if (itunesPlaylists.contains(type)) {
                ((Property) getTableCellItemValue(myIgnoreItunesPlaylists, itemId, "check")).setValue(true);
            }
        }
        myTrackImageMappingsTable.removeAllItems();
        for (ReplacementRule mapping : myConfig.getTrackImageMappings()) {
            addTrackImageMapping(mapping);
        }
        setTablePageLengths();
    }

    protected void setTablePageLengths() {
        super.setTablePageLengths();
        myIgnoreItunesPlaylists.setPageLength(Math.min(myIgnoreItunesPlaylists.getItemIds().size(), 10));
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myPathReplacements, myIgnoreItunesPlaylists, myMiscOptionsForm, myTrackImageMappingsTable)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
            return false;
        }
        return true;
    }
}