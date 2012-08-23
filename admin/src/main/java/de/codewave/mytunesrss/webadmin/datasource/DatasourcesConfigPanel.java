/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.config.*;
import de.codewave.mytunesrss.webadmin.EditUserConfigPanel;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.mytunesrss.webadmin.MyTunesRssConfigPanel;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.component.SinglePanelWindow;
import de.codewave.vaadin.validation.FileValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class DatasourcesConfigPanel extends MyTunesRssConfigPanel {

    public static final Pattern XML_FILE_PATTERN = Pattern.compile("^.*\\.xml$", Pattern.CASE_INSENSITIVE);
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourcesConfigPanel.class);

    private Table myDatasources;
    private Button myAddLocalDatasource;
    private Map<Long, DatasourceConfig> myConfigs = new HashMap<Long, DatasourceConfig>();

    public void attach() {
        super.attach();
        init(getBundleString("datasourcesConfigPanel.caption"), getComponentFactory().createGridLayout(1, 4, true, true));
        Panel sourcesPanel = new Panel(getBundleString("datasourcesConfigPanel.caption.sources"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(sourcesPanel);
        myDatasources = new Table();
        myDatasources.setCacheRate(50);
        myDatasources.addContainerProperty("icon", Embedded.class, null, "", null, null);
        myDatasources.addContainerProperty("path", String.class, null, getBundleString("datasourcesConfigPanel.sourcePath"), null, null);
        myDatasources.addContainerProperty("edit", Button.class, null, "", null, null);
        myDatasources.addContainerProperty("delete", Button.class, null, "", null, null);
        myDatasources.addContainerProperty("options", Button.class, null, "", null, null);
        myDatasources.setSortContainerPropertyId("path");
        myDatasources.setEditable(false);
        sourcesPanel.addComponent(myDatasources);
        myAddLocalDatasource = getComponentFactory().createButton("datasourcesConfigPanel.addLocalDatasource", this);
        sourcesPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddLocalDatasource));

        addDefaultComponents(0, 3, 0, 3, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myDatasources.removeAllItems();
        myConfigs.clear();
        for (DatasourceConfig datasource : MyTunesRss.CONFIG.getDatasources()) {
            addDatasource(datasource);
        }
        myDatasources.sort();
        setTablePageLengths();
    }

    private void addDatasource(DatasourceConfig datasource) {
        Button editButton = getComponentFactory().createButton("button.edit", this);
        Button deleteButton = getComponentFactory().createButton("button.delete", this);
        Button optionsButton = getComponentFactory().createButton("button.options", this);
        long id = myItemIdGenerator.getAndIncrement();
        myDatasources.addItem(new Object[]{new Embedded("", getDatasourceImage(datasource.getType())), datasource.getDefinition(), editButton, deleteButton, optionsButton}, id);
        myConfigs.put(id, DatasourceConfig.copy(datasource));
    }

    private Resource getDatasourceImage(DatasourceType type) {
        if (type == DatasourceType.Itunes) {
            return new ThemeResource("img/itunes.png");
        } else if (type == DatasourceType.Iphoto) {
            return new ThemeResource("img/iphoto.png");
        } else if (type == DatasourceType.Aperture) {
            return new ThemeResource("img/aperture.png");
        } else {
            return new ThemeResource("img/folder.gif");
        }
    }

    protected void writeToConfig() {
        final Set<String> removedDatasourceIds = new HashSet<String>(MyTunesRssUtils.toDatasourceIds(MyTunesRss.CONFIG.getDatasources()));
        removedDatasourceIds.removeAll(MyTunesRssUtils.toDatasourceIds(myConfigs.values()));
        MyTunesRss.CONFIG.setDatasources(new ArrayList<DatasourceConfig>(myConfigs.values()));
        MyTunesRss.CONFIG.save();
        // cleanup database in backgoround
        MyTunesRss.EXECUTOR_SERVICE.execute(new Runnable() {
            public void run() {
                DataStoreSession session = MyTunesRss.STORE.getTransaction();
                try {
                    MyTunesRssUtils.refreshDatasourcePlaylists(session);
                    if (!removedDatasourceIds.isEmpty()) {
                        MyTunesRssUtils.removeDataForSources(session, removedDatasourceIds);
                    }
                    session.commit();
                } catch (SQLException e) {
                    LOGGER.warn("Could not remove obsolete data.", e);
                } finally {
                    session.rollback();
                }
            }
        });
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
                }.show(getWindow());
            } else {
                DatasourceConfig datasourceConfig = myConfigs.get(findTableItemWithObject(myDatasources, clickEvent.getSource()));
                switch (datasourceConfig.getType()) {
                    case Itunes:
                        ItunesDatasourceOptionsPanel itunesOptionsPanel = new ItunesDatasourceOptionsPanel(this, (ItunesDatasourceConfig) datasourceConfig);
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(itunesOptionsPanel);
                        break;
                    case Iphoto:
                        IphotoDatasourceOptionsPanel iphotoOptionsPanel = new IphotoDatasourceOptionsPanel(this, (IphotoDatasourceConfig) datasourceConfig);
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(iphotoOptionsPanel);
                        break;
                    case Aperture:
                        ApertureDatasourceOptionsPanel apertureOptionsPanel = new ApertureDatasourceOptionsPanel(this, (ApertureDatasourceConfig) datasourceConfig);
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(apertureOptionsPanel);
                        break;
                    case Watchfolder:
                        WatchfolderDatasourceOptionsPanel watchfolderOptionsPanel = new WatchfolderDatasourceOptionsPanel(this, (WatchfolderDatasourceConfig) datasourceConfig);
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(watchfolderOptionsPanel);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown datasource type!");
                }
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private void addOrEditLocalDataSource(final Object itemId, File file) {
        new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigPanel.caption.selectLocalDatasource"), file, ServerSideFileChooser.PATTERN_ALL, XML_FILE_PATTERN, false, getApplication().getServerSideFileChooserLabels()) {

            @Override
            protected void onFileSelected(File file) {
                DatasourceConfig newConfig = DatasourceConfig.create(UUID.randomUUID().toString(), file.getAbsolutePath());
                if (newConfig != null) {
                    if (itemId != null) {
                        myDatasources.getItem(itemId).getItemProperty("path").setValue(file.getAbsolutePath());
                        myDatasources.getItem(itemId).getItemProperty("icon").setValue(new Embedded("", getDatasourceImage(newConfig.getType())));
                        DatasourceConfig oldConfig = myConfigs.get(itemId);
                        if (oldConfig.getType() == newConfig.getType()) {
                            // same local type
                            oldConfig.setDefinition(file.getAbsolutePath());
                        } else {
                            // local type has changed
                            newConfig.setId(oldConfig.getId()); // keep data source ID
                            myConfigs.put((Long) itemId, newConfig);
                        }
                    } else {
                        addDatasource(newConfig);
                        setTablePageLengths();
                    }
                } else {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.invalidDatasourcePath");
                }
                getParent().removeWindow(this);
            }
        }.show(getWindow());
    }
}