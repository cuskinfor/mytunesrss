/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Form;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.vaadin.VaadinUtils;

public class IphotoDatasourceOptionsPanel extends DatasourceOptionsPanel {

    private IphotoDatasourceConfig myConfig;
    private Form myMiscOptionsForm;
    private CheckBox myImportRolls;
    private CheckBox myImportAlbums;

    public IphotoDatasourceOptionsPanel(DatasourcesConfigPanel datasourcesConfigPanel, IphotoDatasourceConfig config) {
        super(datasourcesConfigPanel);
        myConfig = config;
    }

    @Override
    public void attach() {
        super.attach();
        init(getBundleString("datasourceOptionsPanel.caption", myConfig.getDefinition()), getComponentFactory().createGridLayout(1, 3, true, true));

        addComponent(myPathReplacementsPanel);
        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myImportRolls = getComponentFactory().createCheckBox("datasourceOptionsPanel.iphotoImportRolls");
        myImportAlbums = getComponentFactory().createCheckBox("datasourceOptionsPanel.iphotoImportAlbums");
        myMiscOptionsForm.addField(myImportRolls, myImportRolls);
        myMiscOptionsForm.addField(myImportAlbums, myImportAlbums);
        myMiscOptionsForm.addField(myPhotoThumbnailImportType, myPhotoThumbnailImportType);
        addComponent(getComponentFactory().surroundWithPanel(myMiscOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.misc")));

        addDefaultComponents(0, 2, 0, 2, false);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myConfig.clearPathReplacements();
        for (Object itemId : myPathReplacements.getItemIds()) {
            myConfig.addPathReplacement(new ReplacementRule((String) getTableCellPropertyValue(myPathReplacements, itemId, "search"), (String) getTableCellPropertyValue(myPathReplacements, itemId, "replace")));
        }
        myConfig.setImportAlbums(myImportAlbums.booleanValue());
        myConfig.setImportRolls(myImportRolls.booleanValue());
        myConfig.setPhotoThumbnailImportType(((DatasourceOptionsPanel.ImageImportTypeRepresentation) myPhotoThumbnailImportType.getValue()).getImageImportType());
        MyTunesRss.CONFIG.replaceDatasourceConfig(myConfig);
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected void initFromConfig() {
        myPathReplacements.removeAllItems();
        for (ReplacementRule replacement : myConfig.getPathReplacements()) {
            addPathReplacement(replacement);
        }
        myImportAlbums.setValue(myConfig.isImportAlbums());
        myImportRolls.setValue(myConfig.isImportRolls());
        myPhotoThumbnailImportType.setValue(IMPORT_TYPE_MAPPINGS.get(myConfig.getPhotoThumbnailImportType()));
        setTablePageLengths();
    }

    @Override
    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myPathReplacements, myMiscOptionsForm)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
            return false;
        }
        if (!myImportRolls.booleanValue() && !myImportAlbums.booleanValue()) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("datasourceOptionsPanel.error.importRollsOrAlbums");
            return false;
        }
        return true;
    }
}
