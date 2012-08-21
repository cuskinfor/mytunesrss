/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.ApertureDatasourceConfig;
import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.mytunesrss.webadmin.MyTunesRssConfigPanel;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApertureDatasourceOptionsPanel extends DatasourceOptionsPanel {

    private Form myMiscOptionsForm;
    private ApertureDatasourceConfig myConfig;

    public ApertureDatasourceOptionsPanel(DatasourcesConfigPanel datasourcesConfigPanel, ApertureDatasourceConfig config) {
        super(datasourcesConfigPanel, config);
        myConfig = config;
    }

    @Override
    public void attach() {
        super.attach();
        init(getBundleString("datasourceOptionsPanel.caption", myConfig.getDefinition()), getComponentFactory().createGridLayout(1, 4, true, true));

        addComponent(myFileTypesPanel);
        addComponent(myPathReplacementsPanel);
        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myMiscOptionsForm.addField(myPhotoThumbnailImportType, myPhotoThumbnailImportType);
        addComponent(getComponentFactory().surroundWithPanel(myMiscOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.misc")));

        addDefaultComponents(0, 3, 0, 3, false);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myConfig.clearPathReplacements();
        for (Object itemId : myPathReplacements.getItemIds()) {
            myConfig.addPathReplacement(new ReplacementRule((String) getTableCellPropertyValue(myPathReplacements, itemId, "search"), (String) getTableCellPropertyValue(myPathReplacements, itemId, "replace")));
        }
        myConfig.setPhotoThumbnailImportType(((ImageImportTypeRepresentation) myPhotoThumbnailImportType.getValue()).getImageImportType());
        myConfig.setFileTypes(getFileTypesAsList());
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected void initFromConfig() {
        myPathReplacements.removeAllItems();
        myPhotoThumbnailImportType.setValue(IMPORT_TYPE_MAPPINGS.get(myConfig.getPhotoThumbnailImportType()));
        for (ReplacementRule replacement : myConfig.getPathReplacements()) {
            addPathReplacement(replacement);
        }
        setFileTypes(myConfig.getFileTypes());
        setTablePageLengths();
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myPathReplacements, myMiscOptionsForm, myFileTypes)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
            return false;
        }
        return true;
    }
}