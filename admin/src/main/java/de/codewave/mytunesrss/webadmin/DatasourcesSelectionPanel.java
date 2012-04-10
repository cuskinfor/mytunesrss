/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.vaadin.VaadinUtils;

import java.util.Collection;
import java.util.HashSet;

public abstract class DatasourcesSelectionPanel extends MyTunesRssConfigPanel {

    private Table myDataSources;
    private Button mySelectAllButton;
    private Button myDeselectAllButton;
    private Button myContinueButton;
    private Button myCancelButton;

    @Override
    public void attach() {
        super.attach();
        init(null, getComponentFactory().createGridLayout(1, 2, true, true));

        myDataSources = new Table();
        myDataSources.setCacheRate(50);
        myDataSources.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        myDataSources.addContainerProperty("selected", CheckBox.class, null, null, null, null);
        myDataSources.addContainerProperty("name", String.class, null, getBundleString("datasourceSelection.datasource.name"), null, null);
        myDataSources.setColumnExpandRatio("name", 1);
        myDataSources.setEditable(false);
        myDataSources.setSortContainerPropertyId("name");
        for (DatasourceConfig datasourceConfig : MyTunesRss.CONFIG.getDatasources()) {
            CheckBox checkbox = new CheckBox();
            checkbox.setValue(true);
            myDataSources.addItem(new Object[]{checkbox, datasourceConfig.getDefinition()}, datasourceConfig);
        }
        myDataSources.setPageLength(Math.min(myDataSources.getItemIds().size(), 10));
        addComponent(myDataSources);

        myCancelButton = getApplication().getComponentFactory().createButton("button.cancel", this);
        myContinueButton = getApplication().getComponentFactory().createButton("button.continue", this);
        mySelectAllButton = getApplication().getComponentFactory().createButton("datasourceSelection.selectAll", this);
        myDeselectAllButton = getApplication().getComponentFactory().createButton("datasourceSelection.deselectAll", this);
        Panel mainButtons = new Panel();
        mainButtons.addStyleName("light");
        mainButtons.setContent(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        mainButtons.addComponent(mySelectAllButton);
        mainButtons.addComponent(myDeselectAllButton);
        mainButtons.addComponent(myCancelButton);
        mainButtons.addComponent(myContinueButton);
        getGridLayout().addComponent(mainButtons, 0, 1, 0, 1);
        getGridLayout().setComponentAlignment(mainButtons, Alignment.MIDDLE_RIGHT);
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).checkUnhandledException();
    }

    private void closeWindow() {
        getWindow().getParent().removeWindow(getWindow());
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myContinueButton) {
            Collection<DatasourceConfig> datasources = getSelectedDatasources();
            if (datasources.isEmpty()) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("datasourceSelection.error.noDatasource");
            } else {
                onContinue(datasources);
                closeWindow();
            }
        } else if (clickEvent.getSource() == myCancelButton) {
            closeWindow();
        } else if (clickEvent.getSource() == mySelectAllButton) {
            for (Object itemId : myDataSources.getItemIds()) {
                ((Property) getTableCellItemValue(myDataSources, itemId, "selected")).setValue(true);
            }
        } else if (clickEvent.getSource() == myDeselectAllButton) {
            for (Object itemId : myDataSources.getItemIds()) {
                ((Property) getTableCellItemValue(myDataSources, itemId, "selected")).setValue(false);
            }
        }
    }

    protected abstract void onContinue(Collection<DatasourceConfig> datasources);

    public Collection<DatasourceConfig> getSelectedDatasources() {
        Collection<DatasourceConfig> selectedDatasources = new HashSet<DatasourceConfig>();
        for (Object itemId : myDataSources.getItemIds()) {
            DatasourceConfig config = (DatasourceConfig) itemId;
            if ((Boolean) getTableCellPropertyValue(myDataSources, itemId, "selected")) {
                selectedDatasources.add(config);
            }
        }
        return selectedDatasources;
    }

    @Override
    protected void writeToConfig() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    protected void initFromConfig() {
        throw new UnsupportedOperationException("Not supported");
    }
}