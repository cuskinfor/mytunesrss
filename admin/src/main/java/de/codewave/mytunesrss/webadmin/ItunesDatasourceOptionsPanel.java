/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.ItunesDatasourceConfig;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.*;

public class ItunesDatasourceOptionsPanel extends MyTunesRssConfigPanel {

    public class ImageImportTypeRepresentation {

        private ImageImportType myImageImportType;

        public ImageImportTypeRepresentation(ImageImportType imageImportType) {
            myImageImportType = imageImportType;
        }

        public ImageImportType getImageImportType() {
            return myImageImportType;
        }

        @Override
        public String toString() {
            return getBundleString("dataimportConfigPanel.importType." + myImageImportType.name());
        }
    }

    private final Map<ImageImportType, ImageImportTypeRepresentation> IMPORT_TYPE_MAPPINGS = new HashMap<ImageImportType, ImageImportTypeRepresentation>();

    private Form myMiscOptionsForm;
    private CheckBox myDeleteMissingFiles;
    private Table myPathReplacements;
    private Button myAddPathReplacement;
    private Table myIgnoreItunesPlaylists;
    private SmartTextField myArtistDropWords;
    private SmartTextField myDisabledMp4Codecs;
    private ItunesDatasourceConfig myConfig;
    private Table myTrackImageMappingsTable;
    private Button myAddTrackImageMapping;
    private Select myTrackImageImportType;

    public ItunesDatasourceOptionsPanel(ItunesDatasourceConfig config) {
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.Auto, new ImageImportTypeRepresentation(ImageImportType.Auto));
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.Never, new ImageImportTypeRepresentation(ImageImportType.Never));
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.OnDemand, new ImageImportTypeRepresentation(ImageImportType.OnDemand));
        myConfig = config;
    }

    @Override
    public void attach() {
        super.attach();
        init(null, getComponentFactory().createGridLayout(1, 5, true, true));

        Panel replacementsPanel = new Panel(getBundleString("datasourceOptionsPanel.caption.itunes.replacements"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(replacementsPanel);
        myPathReplacements = new Table();
        myPathReplacements.setCacheRate(50);
        myPathReplacements.addContainerProperty("search", TextField.class, null, getBundleString("datasourceOptionsPanel.replaceSearch"), null, null);
        myPathReplacements.addContainerProperty("replace", TextField.class, null, getBundleString("datasourceOptionsPanel.replaceReplace"), null, null);
        myPathReplacements.addContainerProperty("delete", Button.class, null, "", null, null);
        myPathReplacements.setEditable(false);
        replacementsPanel.addComponent(myPathReplacements);
        myAddPathReplacement = getComponentFactory().createButton("datasourceOptionsPanel.addReplacement", this);
        replacementsPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddPathReplacement));
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

        Panel imageMappingsPanel = new Panel(getBundleString("datasourceOptionsPanel.trackImageMapping.caption"), getComponentFactory().createVerticalLayout(true, true));
        myTrackImageMappingsTable = new Table();
        myTrackImageMappingsTable.setCacheRate(50);
        myTrackImageMappingsTable.addContainerProperty("search", TextField.class, null, getBundleString("datasourceOptionsPanel.imageMappingSearch"), null, null);
        myTrackImageMappingsTable.addContainerProperty("replace", TextField.class, null, getBundleString("datasourceOptionsPanel.imageMappingReplace"), null, null);
        myTrackImageMappingsTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myTrackImageMappingsTable.setEditable(false);
        imageMappingsPanel.addComponent(myTrackImageMappingsTable);
        myAddTrackImageMapping = getComponentFactory().createButton("datasourceOptionsPanel.addImageMapping", this);
        imageMappingsPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddTrackImageMapping));
        addComponent(imageMappingsPanel);

        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myDeleteMissingFiles = getComponentFactory().createCheckBox("datasourceOptionsPanel.itunesDeleteMissingFiles");
        myArtistDropWords = getComponentFactory().createTextField("datasourceOptionsPanel.artistDropWords");
        myDisabledMp4Codecs = getComponentFactory().createTextField("datasourceOptionsPanel.disabledMp4Codecs");
        myTrackImageImportType = getComponentFactory().createSelect("dataimportConfigPanel.trackImageImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.Never)));
        myMiscOptionsForm.addField(myDeleteMissingFiles, myDeleteMissingFiles);
        myMiscOptionsForm.addField(myArtistDropWords, myArtistDropWords);
        myMiscOptionsForm.addField(myDisabledMp4Codecs, myDisabledMp4Codecs);
        myMiscOptionsForm.addField(myTrackImageImportType, myTrackImageImportType);
        addComponent(getComponentFactory().surroundWithPanel(myMiscOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.misc")));

        addDefaultComponents(0, 4, 0, 4, false);

        initFromConfig();
    }

    private void addTrackImageMapping(ReplacementRule replacement) {
        SmartTextField searchTextField = new SmartTextField();
        searchTextField.setValue(replacement.getSearchPattern());
        searchTextField.addValidator(new ValidRegExpValidator("datasourceOptionsPanel.error.invalidSearchExpression"));
        searchTextField.setImmediate(true);
        myTrackImageMappingsTable.addItem(new Object[]{searchTextField, new SmartTextField(null, replacement.getReplacement()), getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
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

    private void addPathReplacement(ReplacementRule replacement) {
        SmartTextField searchTextField = new SmartTextField();
        searchTextField.setValue(replacement.getSearchPattern());
        searchTextField.addValidator(new ValidRegExpValidator("datasourcesConfigPanel.error.invalidSearchExpression"));
        searchTextField.setImmediate(true);
        myPathReplacements.addItem(new Object[]{searchTextField, new SmartTextField(null, replacement.getReplacement()), getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
    }

    private void setTablePageLengths() {
        myPathReplacements.setPageLength(Math.min(myPathReplacements.getItemIds().size(), 5));
        myIgnoreItunesPlaylists.setPageLength(Math.min(myIgnoreItunesPlaylists.getItemIds().size(), 10));
        myTrackImageMappingsTable.setPageLength(Math.min(myTrackImageMappingsTable.getItemIds().size(), 5));
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myPathReplacements, myIgnoreItunesPlaylists, myMiscOptionsForm, myTrackImageMappingsTable)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        } else {
            writeToConfig();
            closeWindow();
        }
        return false; // make sure the default operation is not used
    }

    @Override
    protected boolean beforeCancel() {
        closeWindow();
        return false; // make sure the default operation is not used
    }

    private void closeWindow() {
        getWindow().getParent().removeWindow(getWindow());
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddPathReplacement) {
            addPathReplacement(new ReplacementRule("^.*$", "\\0"));
            setTablePageLengths();
        } else if (findTableItemWithObject(myPathReplacements, clickEvent.getSource()) != null) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigDialog.optionWindowDeletePathReplacement.caption"), getBundleString("datasourcesConfigDialog.optionWindowDeletePathReplacement.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myPathReplacements.removeItem(findTableItemWithObject(myPathReplacements, clickEvent.getSource()));
                        setTablePageLengths();
                    }
                }
            }.show(VaadinUtils.getApplicationWindow(this));
        } else if (clickEvent.getSource() == myAddTrackImageMapping) {
            addTrackImageMapping(new ReplacementRule("^.*$", "\\0"));
            setTablePageLengths();
        } else if (findTableItemWithObject(myTrackImageMappingsTable, clickEvent.getSource()) != null) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourceOptionsPanel.optionWindowDeleteTrackImageMapping.caption"), getBundleString("datasourceOptionsPanel.optionWindowDeleteTrackImageMapping.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myTrackImageMappingsTable.removeItem(findTableItemWithObject(myTrackImageMappingsTable, clickEvent.getSource()));
                        setTablePageLengths();
                    }
                }
            }.show(VaadinUtils.getApplicationWindow(this));
        } else {
            super.buttonClick(clickEvent);
        }
    }
}