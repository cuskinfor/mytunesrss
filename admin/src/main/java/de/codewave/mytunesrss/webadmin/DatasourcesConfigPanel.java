/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.*;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.*;
import de.codewave.vaadin.validation.FileValidator;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DatasourcesConfigPanel extends MyTunesRssConfigPanel {

    private static final Pattern XML_FILE_PATTERN = Pattern.compile("^.*\\.xml$", Pattern.CASE_INSENSITIVE);

    private Table myDatasources;
    private Button myAddLocalDatasource;
    private SmartTextField myUploadDir;
    private Button mySelectUploadDir;
    private CheckBox myUploadCreateUserDir;
    private Form myUploadForm;
    private Map<Long, DatasourceConfig> myConfigs = new HashMap<Long, DatasourceConfig>();

    public void attach() {
        init(getBundleString("datasourcesConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));
        Panel sourcesPanel = new Panel(getBundleString("datasourcesConfigPanel.caption.sources"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(sourcesPanel);
        myDatasources = new Table();
        myDatasources.addContainerProperty("icon", Embedded.class, null, "", null, null);
        myDatasources.addContainerProperty("path", String.class, null, getBundleString("datasourcesConfigPanel.sourcePath"), null, null);
        myDatasources.addContainerProperty("edit", Button.class, null, "", null, null);
        myDatasources.addContainerProperty("delete", Button.class, null, "", null, null);
        myDatasources.addContainerProperty("options", Button.class, null, "", null, null);
        myDatasources.setEditable(false);
        sourcesPanel.addComponent(myDatasources);
        myAddLocalDatasource = getComponentFactory().createButton("datasourcesConfigPanel.addLocalDatasource", this);
        sourcesPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddLocalDatasource));

        myUploadForm = getComponentFactory().createForm(null, true);
        myUploadDir = getComponentFactory().createTextField("datasourcesConfigPanel.uploadDir", new FileValidator(getBundleString("datasourcesConfigPanel.error.invalidUploadDir"), FileValidator.PATTERN_ALL, null));
        myUploadDir.setImmediate(true);
        mySelectUploadDir = getComponentFactory().createButton("datasourcesConfigPanel.selectUploadDir", this);
        myUploadCreateUserDir = getComponentFactory().createCheckBox("datasourcesConfigPanel.uploadCreateUserDir");
        myUploadForm.addField(myUploadDir, myUploadDir);
        myUploadForm.addField(mySelectUploadDir, mySelectUploadDir);
        myUploadForm.addField(myUploadCreateUserDir, myUploadCreateUserDir);
        addComponent(getComponentFactory().surroundWithPanel(myUploadForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourcesConfigPanel.caption.upload")));

        attach(0, 4, 0, 4);

        initFromConfig();
    }

    protected void initFromConfig() {
        myDatasources.removeAllItems();
        myConfigs.clear();
        for (DatasourceConfig datasource : MyTunesRss.CONFIG.getDatasources()) {
            addDatasource(getApplication(), datasource);
        }
        myUploadDir.setValue(MyTunesRss.CONFIG.getUploadDir());
        myUploadCreateUserDir.setValue(MyTunesRss.CONFIG.isUploadCreateUserDir());
        setTablePageLengths();
    }

    private void addDatasource(Application application, DatasourceConfig datasource) {
        Button editButton = getComponentFactory().createButton("button.edit", this);
        Button deleteButton = getComponentFactory().createButton("button.delete", this);
        Button optionsButton = getComponentFactory().createButton("button.options", this);
        long id = myItemIdGenerator.getAndIncrement();
        myDatasources.addItem(new Object[]{new Embedded("", getDatasourceImage(application, datasource.getType())), datasource.getDefinition(), editButton, deleteButton, optionsButton}, id);
        myConfigs.put(id, DatasourceConfig.copy(datasource));
    }

    private Resource getDatasourceImage(Application application, DatasourceType type) {
        if (type == DatasourceType.Itunes) {
            return new ClassResource("itunes.gif", application);
        } else {
            return new ClassResource("folder.gif", application);
        }
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setDatasources(new ArrayList<DatasourceConfig>(myConfigs.values()));
        MyTunesRss.CONFIG.setUploadDir(myUploadDir.getStringValue(null));
        MyTunesRss.CONFIG.setUploadCreateUserDir(myUploadCreateUserDir.booleanValue());
        MyTunesRss.CONFIG.save();
    }

    private void setTablePageLengths() {
        myDatasources.setPageLength(Math.min(myDatasources.getItemIds().size(), 10));
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddLocalDatasource) {
            addOrEditLocalDataSource(null, null);
        } else if (findTableItemWithObject(myDatasources, clickEvent.getSource()) != null) {
            Item item = myDatasources.getItem(findTableItemWithObject(myDatasources, clickEvent.getSource()));
            if (item.getItemProperty("edit").getValue() == clickEvent.getSource()) {
                addOrEditLocalDataSource(findTableItemWithObject(myDatasources, clickEvent.getSource()), new File((String) item.getItemProperty("path").getValue()));
            } else if (item.getItemProperty("delete").getValue() == clickEvent.getSource()) {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigDialog.optionWindowDeleteDatasource.caption"), getBundleString("datasourcesConfigDialog.optionWindowDeleteDatasource.message"), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            Object id = findTableItemWithObject(myDatasources, clickEvent.getSource());
                            myDatasources.removeItem(id);
                            myConfigs.remove(id);
                            setTablePageLengths();
                        }
                    }
                }.show(getApplication().getMainWindow());
            } else {
                DatasourceConfig datasourceConfig = myConfigs.get(findTableItemWithObject(myDatasources, clickEvent.getSource()));
                switch (datasourceConfig.getType()) {
                    case Itunes:
                        ItunesDatasourceOptionsPanel itunesOptionsPanel = new ItunesDatasourceOptionsPanel((ItunesDatasourceConfig) datasourceConfig);
                        SinglePanelWindow itunesOptionsWindow = new SinglePanelWindow(50, Sizeable.UNITS_EM, null, getBundleString("datasourceOptionsPanel.caption"), itunesOptionsPanel);
                        itunesOptionsWindow.show(getWindow());
                        break;
                    case Watchfolder:
                        WatchfolderDatasourceOptionsPanel watchfolderOptionsPanel = new WatchfolderDatasourceOptionsPanel((WatchfolderDatasourceConfig) datasourceConfig);
                        SinglePanelWindow watchfolderOptionsWindow = new SinglePanelWindow(50, Sizeable.UNITS_EM, null, getBundleString("datasourceOptionsPanel.caption"), watchfolderOptionsPanel);
                        watchfolderOptionsWindow.show(getWindow());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown datasource type!");
                }
            }
        } else if (clickEvent.getSource() == mySelectUploadDir) {
            File dir = StringUtils.isNotBlank((String)myUploadDir.getValue()) ? new File((String) myUploadDir.getValue()) : null;
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigPanel.caption.selectUploadDir"), dir, ServerSideFileChooser.PATTERN_ALL, null, true, "Roots") { // TODO i18n
                @Override
                protected void onFileSelected(File file) {
                    myUploadDir.setValue(file.getAbsolutePath());
                    getApplication().getMainWindow().removeWindow(this);
                }
            }.show(getApplication().getMainWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private void addOrEditLocalDataSource(final Object itemId, File file) {
        new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigPanel.caption.selectLocalDatasource"), file, ServerSideFileChooser.PATTERN_ALL, XML_FILE_PATTERN, false, "Roots") { // TODO i18n
            @Override
            protected void onFileSelected(File file) {
                if (itemId != null) {
                    myDatasources.getItem(itemId).getItemProperty("path").setValue(file.getAbsolutePath());
                    DatasourceConfig newConfig = DatasourceConfig.create(file.getAbsolutePath());
                    DatasourceConfig oldConfig = myConfigs.get(itemId);
                    if (oldConfig.getType() == newConfig.getType()) {
                        // same local type
                        oldConfig.setDefinition(file.getAbsolutePath());
                    } else {
                        // local type has changed
                        myConfigs.put((Long) itemId, newConfig);
                    }
                } else {
                    addDatasource(getApplication(), DatasourceConfig.create(file.getAbsolutePath()));
                    setTablePageLengths();
                }
                getApplication().getMainWindow().removeWindow(this);
            }
        }.show(getApplication().getMainWindow());
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myUploadForm)) {
            getApplication().showError("error.formInvalid");
            return false;
        }
        return true;
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