/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ItunesDatasourceConfig;
import de.codewave.mytunesrss.PathReplacement;
import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.*;

public class ItunesDatasourceOptionsPanel extends MyTunesRssConfigPanel {

    private Form myMiscOptionsForm;
    private CheckBox myDeleteMissingFiles;
    private Table myPathReplacements;
    private Button myAddPathReplacement;
    private Table myIgnoreItunesPlaylists;
    private ItunesDatasourceConfig myConfig;

    public ItunesDatasourceOptionsPanel(ItunesDatasourceConfig config) {
        myConfig = config;
    }

    @Override
    public void attach() {
        init(null, getComponentFactory().createGridLayout(1, 4, true, true));

        Panel replacementsPanel = new Panel(getBundleString("datasourceOptionsPanel.caption.replacements"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(replacementsPanel);
        myPathReplacements = new Table();
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

        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myDeleteMissingFiles = getComponentFactory().createCheckBox("datasourceOptionsPanel.itunesDeleteMissingFiles");
        myMiscOptionsForm.addField(myDeleteMissingFiles, myDeleteMissingFiles);
        addComponent(getComponentFactory().surroundWithPanel(myMiscOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.itunesMisc")));

        attach(0, 3, 0, 3);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myConfig.setDeleteMissingFiles(myDeleteMissingFiles.booleanValue());
        myConfig.clearPathReplacements();
        for (Object itemId : myPathReplacements.getItemIds()) {
            myConfig.addPathReplacement(new PathReplacement((String) getTableCellPropertyValue(myPathReplacements, itemId, "search"), (String) getTableCellPropertyValue(myPathReplacements, itemId, "replace")));
        }
        myConfig.clearIgnorePlaylists();
        for (Object itemId : myIgnoreItunesPlaylists.getItemIds()) {
            boolean checked = (Boolean)getTableCellPropertyValue(myIgnoreItunesPlaylists, itemId, "check");
            if (checked) {
                ItunesPlaylistType type = ((ItunesPlaylistType) getTableCellItemValue(myIgnoreItunesPlaylists, itemId, "type"));
                myConfig.addIgnorePlaylist(type);
            }
        }
    }

    @Override
    protected void initFromConfig() {
        myDeleteMissingFiles.setValue(myConfig.isDeleteMissingFiles());
        myPathReplacements.removeAllItems();
        for (PathReplacement replacement : myConfig.getPathReplacements()) {
            addPathReplacement(replacement);
        }
        Set<ItunesPlaylistType> itunesPlaylists = myConfig.getIgnorePlaylists();
        for (Object itemId : myIgnoreItunesPlaylists.getItemIds()) {
            ItunesPlaylistType type = ((ItunesPlaylistType) getTableCellItemValue(myIgnoreItunesPlaylists, itemId, "type"));
            if (itunesPlaylists.contains(type)) {
                ((Property) getTableCellItemValue(myIgnoreItunesPlaylists, itemId, "check")).setValue(true);
            }
        }
        setTablePageLengths();
    }

    private void addPathReplacement(PathReplacement replacement) {
        SmartTextField searchTextField = new SmartTextField();
        searchTextField.setValue(replacement.getSearchPattern());
        searchTextField.addValidator(new ValidRegExpValidator("datasourcesConfigPanel.error.invalidSearchExpression"));
        searchTextField.setImmediate(true);
        myPathReplacements.addItem(new Object[]{searchTextField, new SmartTextField(null, replacement.getReplacement()), getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
    }

    private void setTablePageLengths() {
        myPathReplacements.setPageLength(Math.min(myPathReplacements.getItemIds().size(), 5));
        myIgnoreItunesPlaylists.setPageLength(Math.min(myIgnoreItunesPlaylists.getItemIds().size(), 10));
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myPathReplacements, myIgnoreItunesPlaylists, myMiscOptionsForm)) {
            getApplication().showError("error.formInvalid");
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
        getWindow().getParent().getWindow().removeWindow(getWindow());
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddPathReplacement) {
            addPathReplacement(new PathReplacement("^.*$", "\\0"));
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
            }.show(getApplication().getMainWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private Object findTableItemWithObject(Table table, Object component) {
        for (Object itemId : table.getItemIds()) {
            for (Object itemPropertyId : table.getItem(itemId).getItemPropertyIds()) {
                if (component == table.getItem(itemId).getItemProperty(itemPropertyId).getValue()) {
                    return itemId;
                }
            }
        }
        return null;
    }
}