/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import de.codewave.mytunesrss.AddonsUtils;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class EditLanguagePanel extends MyTunesRssConfigPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditLanguagePanel.class);

    private Table myEditorTable;
    private Locale myEditLang;
    private AddonsConfigPanel myAddonsConfigPanel;

    public EditLanguagePanel(AddonsConfigPanel addonsConfigPanel, Locale editLang) {
        myAddonsConfigPanel = addonsConfigPanel;
        myEditLang = editLang;
    }

    public void attach() {
        super.attach();
        init(getApplication().getBundleString("editLanguagePanel.caption"), getApplication().getComponentFactory().createGridLayout(1, 2, true, true));
        myEditorTable = new Table();
        myEditorTable.setCacheRate(500);
        myEditorTable.addContainerProperty("key", String.class, null, getBundleString("editLanguagePanel.key"), null, null);
        Locale appLocale = getApplication().getLocale();
        myEditorTable.addContainerProperty("reference", String.class, null, appLocale.getDisplayName(appLocale), null, null);
        myEditorTable.addContainerProperty("edit", TextField.class, null, myEditLang.getDisplayName(appLocale), null, null);
        myEditorTable.setWidth(100, UNITS_PERCENTAGE);
        myEditorTable.setColumnExpandRatio("key", 1);
        myEditorTable.setColumnExpandRatio("reference", 1);
        myEditorTable.setColumnExpandRatio("edit", 1);
        addComponent(myEditorTable);

        addDefaultComponents(0, 1, 0, 1, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myEditorTable.removeAllItems();
        Properties refProps = new Properties();
        Properties editProps = new Properties();
        FileInputStream refInputStream = null;
        FileInputStream editInputStream = null;
        try {
            refInputStream = new FileInputStream(AddonsUtils.getBuiltinLanguageFile(getApplication().getLocale()));
            editInputStream = new FileInputStream(AddonsUtils.getUserLanguageFile(myEditLang));
            refProps.load(refInputStream);
            editProps.load(editInputStream);
            List<String> keys = new ArrayList(refProps.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                SmartTextField textField = getComponentFactory().createTextField(null);
                textField.setValue(editProps.getProperty(key));
                myEditorTable.addItem(new Object[]{key, refProps.getProperty(key), textField}, key);
            }
        } catch (Exception e) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("editLanguagePanel.error.couldNotReadFile");
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read reference or edit properties.", e);
            }
            cancelPanel();
        } finally {
            IOUtils.closeQuietly(refInputStream);
            IOUtils.closeQuietly(editInputStream);
        }
        setTablePageLengths();
    }

    private void setTablePageLengths() {
        myEditorTable.setPageLength(Math.min(myEditorTable.getItemIds().size(), 40));
    }

    protected void writeToConfig() {
        Properties props = new Properties();
        for (Object id : myEditorTable.getItemIds()) {
            props.setProperty((String)id, (String) ((TextField) myEditorTable.getItem(id).getItemProperty("edit").getValue()).getValue());
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(AddonsUtils.getUserLanguageFile(myEditLang));
            props.store(outputStream, "MyTunesRSS user interface language for \"" + myEditLang + "\"");
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not write language file.", e);
            }
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("editLanguagePanel.error.couldNotWriteFile");
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    protected boolean beforeSave() {
        for (Object id : myEditorTable.getItemIds()) {
            if (StringUtils.isBlank((String) ((TextField) myEditorTable.getItem(id).getItemProperty("edit").getValue()).getValue())) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("editLanguagePanel.error.emptyTranslation");
                return false;
            }
        }
        if (AddonsUtils.getUserLanguageFile(myEditLang) == null) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("editLanguagePanel.error.couldNotWriteFile");
            return false;
        }
        return true;
    }

    @Override
    protected Component getSaveFollowUpComponent() {
        return myAddonsConfigPanel;
    }

    @Override
    protected Component getCancelFollowUpComponent() {
        return myAddonsConfigPanel;
    }
}