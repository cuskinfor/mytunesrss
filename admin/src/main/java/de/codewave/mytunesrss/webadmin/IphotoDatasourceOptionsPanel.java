/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.IphotoDatasourceConfig;
import de.codewave.mytunesrss.ReplacementRule;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

public class IphotoDatasourceOptionsPanel extends MyTunesRssConfigPanel {

    private Table myPathReplacements;
    private Button myAddPathReplacement;
    private IphotoDatasourceConfig myConfig;
    private Form myMiscOptionsForm;
    private CheckBox myImportRolls;
    private CheckBox myImportAlbums;

    public IphotoDatasourceOptionsPanel(IphotoDatasourceConfig config) {
        myConfig = config;
    }

    @Override
    public void attach() {
        super.attach();
        init(null, getComponentFactory().createGridLayout(1, 3, true, true));

        Panel replacementsPanel = new Panel(getBundleString("datasourceOptionsPanel.caption.iphoto.replacements"), getComponentFactory().createVerticalLayout(true, true));
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
        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myImportRolls = getComponentFactory().createCheckBox("datasourceOptionsPanel.iphotoImportRolls");
        myImportAlbums = getComponentFactory().createCheckBox("datasourceOptionsPanel.iphotoImportAlbums");
        myMiscOptionsForm.addField(myImportRolls, myImportRolls);
        myMiscOptionsForm.addField(myImportAlbums, myImportAlbums);
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
    }

    @Override
    protected void initFromConfig() {
        myPathReplacements.removeAllItems();
        for (ReplacementRule replacement : myConfig.getPathReplacements()) {
            addPathReplacement(replacement);
        }
        setTablePageLengths();
        myImportAlbums.setValue(myConfig.isImportAlbums());
        myImportRolls.setValue(myConfig.isImportRolls());
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
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myPathReplacements)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        } else if (!myImportRolls.booleanValue() && !myImportAlbums.booleanValue()) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("datasourcesConfigPanel.error.importRollsOrAlbums");
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
        } else {
            super.buttonClick(clickEvent);
        }
    }
}