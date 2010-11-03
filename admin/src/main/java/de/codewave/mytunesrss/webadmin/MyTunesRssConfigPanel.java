/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.FetchExternalAddressRunnable;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssNotification;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.server.MyTunesRssSessionInfo;
import de.codewave.vaadin.ComponentFactory;
import org.apache.commons.lang.StringUtils;
import org.vaadin.henrik.refresher.Refresher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class MyTunesRssConfigPanel extends Panel implements Button.ClickListener, Refresher.RefreshListener {
    protected static final Layout.MarginInfo FORM_PANEL_MARGIN_INFO = new Layout.MarginInfo(false, true, false, true);

    private Button mySave;
    private Button myReset;
    private Button myCancel;
    protected AtomicLong myItemIdGenerator = new AtomicLong(0);
    private Refresher myRefresher;

    protected void init(String caption, GridLayout content) {
        setCaption(caption);
        setContent(content);
        getGridLayout().setWidth(100, Sizeable.UNITS_PERCENTAGE);
        for (int i = 0; i < getGridLayout().getColumns(); i++) {
            getGridLayout().setColumnExpandRatio(i, 1);
        }
    }

    protected void attach(int columnn1, int row1, int column2, int row2) {
        mySave = getApplication().getComponentFactory().createButton("button.save", this);
        myReset = getApplication().getComponentFactory().createButton("button.reset", this);
        myCancel = getApplication().getComponentFactory().createButton("button.cancel", this);
        Panel mainButtons = new Panel();
        mainButtons.addStyleName("light");
        mainButtons.setContent(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        mainButtons.addComponent(mySave);
        mainButtons.addComponent(myReset);
        mainButtons.addComponent(myCancel);
        getGridLayout().addComponent(mainButtons, columnn1, row1, column2, row2);
        getGridLayout().setComponentAlignment(mainButtons, Alignment.MIDDLE_RIGHT);
        myRefresher = new Refresher();
        addComponent(myRefresher);
        myRefresher.setRefreshInterval(MyTunesRssWebAdmin.ADMIN_REFRESHER_INTERVAL_MILLIS);
        myRefresher.addListener(this);
    }

    protected abstract void writeToConfig();

    protected abstract void initFromConfig();

    protected String getBundleString(String key, Object... parameters) {
        return getApplication().getBundleString(key, parameters);
    }

    protected void setError(AbstractComponent component, String messageKey, Object... parameters) {
        if (messageKey == null) {
            component.setComponentError(null);
        } else {
            component.setComponentError(new UserError(getBundleString(messageKey, parameters)));
        }
    }

    protected void setRequired(Field field) {
        getApplication().getComponentFactory().setRequired(field, "error.requiredField");
    }

    protected void setOptional(Field field) {
        getApplication().getComponentFactory().setOptional(field);
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    protected ComponentFactory getComponentFactory() {
        return getApplication().getComponentFactory();
    }

    protected ValidatorFactory getValidatorFactory() {
        return getApplication().getValidatorFactory();
    }

    protected GridLayout getGridLayout() {
        return (GridLayout) getContent();
    }

    protected boolean beforeCancel() {
        return true;
    }

    protected boolean beforeReset() {
        return true;
    }

    protected boolean beforeSave() {
        return true;
    }

    protected Object getTableCellItemValue(Table table, Object itemId, Object itemPropertyId) {
        return table.getItem(itemId).getItemProperty(itemPropertyId).getValue();
    }

    protected Object getTableCellPropertyValue(Table table, Object itemId, Object itemPropertyId) {
        return ((Property) getTableCellItemValue(table, itemId, itemPropertyId)).getValue();
    }

    protected Component getSaveFollowUpComponent() {
        return getApplication().getStatusPanel();
    }

    protected Component getCancelFollowUpComponent() {
        return getApplication().getStatusPanel();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == mySave) {
            if (beforeSave()) {
                writeToConfig();
                getApplication().setMainComponent(getSaveFollowUpComponent());
            }
        } else if (clickEvent.getButton() == myReset) {
            if (beforeReset()) {
                initFromConfig();
            }
        } else if (clickEvent.getButton() == myCancel) {
            if (beforeCancel()) {
                getApplication().setMainComponent(getCancelFollowUpComponent());
            }
        }
    }

    protected Field setValue(Field field, Object value) {
        field.setValue(value);
        return field;
    }


    protected Button createTableRowButton(String textKey, Button.ClickListener listener, Object itemId, Object action) {
        return new TableRowButton(getBundleString(textKey), listener, itemId, action);
    }

    public void refresh(Refresher source) {
        getApplication().pollNotifications();
    }

    protected Object findTableItemWithObject(Table table, Object component) {
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
