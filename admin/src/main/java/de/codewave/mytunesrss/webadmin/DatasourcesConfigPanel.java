/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.PathReplacement;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.component.TextFieldWindow;
import de.codewave.vaadin.validation.FileValidator;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatasourcesConfigPanel extends MyTunesRssConfigPanel {

    private Table myDatasources;
    private Button myAddLocalDatasource;
    private Button myAddRemoteDatasource;
    private TextField myAlbumFallback;
    private TextField myArtistFallback;
    private Table myPathReplacements;
    private Button myAddPathReplacement;
    private TextField myUploadDir;
    private Button mySelectUploadDir;
    private CheckBox myUploadCreateUserDir;
    private Form myFallbackForm;
    private Form myUploadForm;

    public DatasourcesConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("datasourcesConfigPanel.caption"), componentFactory.createGridLayout(1, 5, true, true), componentFactory);
    }

    protected void init(Application application) {
        Panel sourcesPanel = new Panel(getBundleString("datasourcesConfigPanel.caption.sources"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(sourcesPanel);
        myDatasources = new Table();
        myDatasources.addContainerProperty("icon", Embedded.class, null, "", null, null);
        myDatasources.addContainerProperty("path", String.class, null, getBundleString("datasourcesConfigPanel.sourcePath"), null, null);
        myDatasources.addContainerProperty("delete", Button.class, null, "", null, null);
        myDatasources.setEditable(false);
        sourcesPanel.addComponent(myDatasources);
        myAddLocalDatasource = getComponentFactory().createButton("datasourcesConfigPanel.addLocalDatasource", this);
        myAddRemoteDatasource = getComponentFactory().createButton("datasourcesConfigPanel.addRemoteDatasource", this);
        sourcesPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddLocalDatasource, myAddRemoteDatasource));

        myFallbackForm = getComponentFactory().createForm(null, true);
        myAlbumFallback = getComponentFactory().createTextField("datasourcesConfigPanel.albumFallback");
        myArtistFallback = getComponentFactory().createTextField("datasourcesConfigPanel.artistFallback");
        myFallbackForm.addField(myAlbumFallback, myAlbumFallback);
        myFallbackForm.addField(myArtistFallback, myArtistFallback);
        addComponent(getComponentFactory().surroundWithPanel(myFallbackForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourcesConfigPanel.caption.fallbacks")));

        Panel replacementsPanel = new Panel(getBundleString("datasourcesConfigPanel.caption.replacements"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(replacementsPanel);
        myPathReplacements = new Table();
        myPathReplacements.addContainerProperty("search", String.class, null, getBundleString("datasourcesConfigPanel.replaceSearch"), null, null);
        myPathReplacements.addContainerProperty("replace", String.class, null, getBundleString("datasourcesConfigPanel.replaceReplace"), null, null);
        myPathReplacements.addContainerProperty("delete", Button.class, null, "", null, null);
        myPathReplacements.setEditable(true);
        replacementsPanel.addComponent(myPathReplacements);
        myAddPathReplacement = getComponentFactory().createButton("datasourcesConfigPanel.addReplacement", this);
        replacementsPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddPathReplacement));

        myUploadForm = getComponentFactory().createForm(null, true);
        myUploadDir = getComponentFactory().createTextField("datasourcesConfigPanel.uploadDir", new FileValidator(getBundleString("datasourcesConfigPanel.error.invalidUploadDir"), false, true, null));
        myUploadDir.setImmediate(true);
        mySelectUploadDir = getComponentFactory().createButton("datasourcesConfigPanel.selectUploadDir", this);
        myUploadCreateUserDir = getComponentFactory().createCheckBox("datasourcesConfigPanel.uploadCreateUserDir");
        myUploadForm.addField(myUploadDir, myUploadDir);
        myUploadForm.addField(mySelectUploadDir, mySelectUploadDir);
        myUploadForm.addField(myUploadCreateUserDir, myUploadCreateUserDir);
        addComponent(getComponentFactory().surroundWithPanel(myUploadForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourcesConfigPanel.caption.upload")));

        addMainButtons(0, 4, 0, 4);
    }

    protected void initFromConfig(Application application) {
        for (String datasource : MyTunesRss.CONFIG.getDatasources()) {
            addDatasource(application, datasource);
        }
        myAlbumFallback.setValue(MyTunesRss.CONFIG.getAlbumFallback());
        myArtistFallback.setValue(MyTunesRss.CONFIG.getArtistFallback());
        for (PathReplacement replacement : MyTunesRss.CONFIG.getPathReplacements()) {
            addPathReplacement(replacement);
        }
        myUploadDir.setValue(MyTunesRss.CONFIG.getUploadDir());
        myUploadCreateUserDir.setValue(MyTunesRss.CONFIG.isUploadCreateUserDir());
        setTablePageLengths();
    }

    private void addDatasource(Application application, String datasource) {
        myDatasources.addItem(new Object[]{new Embedded("", getDatasourceImage(application, datasource)), datasource, getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
    }

    private void addPathReplacement(PathReplacement replacement) {
        TextField searchTextField = new TextField();
        searchTextField.setValue(replacement.getSearchPattern());
        searchTextField.addValidator(new ValidRegExpValidator("datasourcesConfigPanel.error.invalidSearchExpression"));
        searchTextField.setImmediate(true);
        myPathReplacements.addItem(new Object[]{searchTextField, replacement.getReplacement(), getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
    }

    private Resource getDatasourceImage(Application application, String datasource) {
        if (MyTunesRssUtils.isValidRemoteUrl(datasource)) {
            return new ClassResource("http.gif", application);
        } else if (StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(datasource), "xml")) {
            return new ClassResource("itunes.gif", application);
        } else {
            return new ClassResource("folder.gif", application);
        }
    }

    protected void writeToConfig() {
        List<String> datasources = new ArrayList<String>();
        for (Object itemId : myDatasources.getItemIds()) {
            datasources.add((String) myDatasources.getItem(itemId).getItemProperty("path").getValue());
        }
        MyTunesRss.CONFIG.setDatasources(datasources.toArray(new String[datasources.size()]));
        MyTunesRss.CONFIG.setAlbumFallback((String) myAlbumFallback.getValue());
        MyTunesRss.CONFIG.setArtistFallback((String) myArtistFallback.getValue());
        MyTunesRss.CONFIG.clearPathReplacements();
        for (Object itemId : myDatasources.getItemIds()) {
            MyTunesRss.CONFIG.addPathReplacement(new PathReplacement((String) myPathReplacements.getItem(itemId).getItemProperty("search").getValue(), (String) myPathReplacements.getItem(itemId).getItemProperty("replace").getValue()));
        }
        MyTunesRss.CONFIG.setUploadDir((String) myUploadDir.getValue());
        MyTunesRss.CONFIG.setUploadCreateUserDir(myUploadCreateUserDir.booleanValue());
    }

    private void setTablePageLengths() {
        myDatasources.setPageLength(Math.min(myDatasources.getItemIds().size(), 10));
        myPathReplacements.setPageLength(Math.min(myPathReplacements.getItemIds().size(), 10));
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddLocalDatasource) {
            // todo
        } else if (clickEvent.getSource() == myAddRemoteDatasource) {
            new TextFieldWindow(50, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigPanel.addRemoteDatasource.caption"), getBundleString("datasourcesConfigPanel.addRemoteDatasource.message"), getBundleString("datasourcesConfigPanel.addRemoteDatasource.ok"), getBundleString("button.cancel")) {
                @Override
                protected void onOk(String text) {
                    if (MyTunesRssUtils.isValidRemoteUrl(text)) {
                        addDatasource(getApplication(), text);
                        getApplication().getMainWindow().removeWindow(this);
                    } else {
                        DatasourcesConfigPanel.this.getApplication().showWarning("datasourcesConfigPanel.warning.illegalRemoteUrl");
                    }
                }
            }.show(getApplication().getMainWindow());
        } else if (clickEvent.getSource() == myAddPathReplacement) {
            addPathReplacement(new PathReplacement("^.*$", "\\0"));
            setTablePageLengths();
        } else if (findTableItemWithObject(myDatasources, clickEvent.getSource()) != null) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigDialog.optionWindowDeleteDatasource.caption"), getBundleString("datasourcesConfigDialog.optionWindowDeleteDatasource.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myDatasources.removeItem(findTableItemWithObject(myDatasources, clickEvent.getSource()));
                        setTablePageLengths();
                    }
                }
            }.show(getApplication().getMainWindow());
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
        } else if (clickEvent.getSource() == mySelectUploadDir) {
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigPanel.caption.selectUploadDir"), new File((String) myUploadDir.getValue()), true, false, null, true) {
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

    protected boolean beforeSave() {
        return false;
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