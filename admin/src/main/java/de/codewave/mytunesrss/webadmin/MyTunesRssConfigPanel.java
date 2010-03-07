/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.vaadin.ComponentFactory;

import java.util.concurrent.atomic.AtomicLong;

public abstract class MyTunesRssConfigPanel extends Panel implements Button.ClickListener {
    protected static final Layout.MarginInfo FORM_PANEL_MARGIN_INFO = new Layout.MarginInfo(false, true, false, true);

    private Button mySave;
    private Button myReset;
    private Button myCancel;
    private ComponentFactory myComponentFactory;
    protected AtomicLong myItemIdGenerator = new AtomicLong(0);

    public MyTunesRssConfigPanel(Application application, String caption, GridLayout content, ComponentFactory componentFactory) {
        super(caption, content);
        getGridLayout().setWidth(100, Sizeable.UNITS_PERCENTAGE);
        for (int i = 0; i < getGridLayout().getColumns(); i++) {
            getGridLayout().setColumnExpandRatio(i, 1);
        }
        myComponentFactory = componentFactory;
        init(application);
        initFromConfig(application);
    }

    protected void addMainButtons(int columnn1, int row1, int column2, int row2) {
        mySave = myComponentFactory.createButton("button.save", this);
        myReset = myComponentFactory.createButton("button.reset", this);
        myCancel = myComponentFactory.createButton("button.cancel", this);
        Panel mainButtons = new Panel();
        mainButtons.addStyleName("light");
        mainButtons.setContent(myComponentFactory.createHorizontalLayout(false, true));
        mainButtons.addComponent(mySave);
        mainButtons.addComponent(myReset);
        mainButtons.addComponent(myCancel);
        getGridLayout().addComponent(mainButtons, columnn1, row1, column2, row2);
        getGridLayout().setComponentAlignment(mainButtons, Alignment.MIDDLE_RIGHT);
    }

    protected abstract void init(Application application);

    protected abstract void writeToConfig();

    protected abstract void initFromConfig(Application application);

    protected static String getBundleString(String key, Object... parameters) {
        return MyTunesRssWebAdminUtils.getBundleString(key, parameters);
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    protected GridLayout getGridLayout() {
        return (GridLayout) getContent();
    }

    protected ComponentFactory getComponentFactory() {
        return myComponentFactory;
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

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == mySave) {
            if (beforeSave()) {
                writeToConfig();
                getApplication().setMainComponent(new StatusPanel(getApplication(), myComponentFactory));
            }
        } else if (clickEvent.getButton() == myReset) {
            if (beforeReset()) {
                initFromConfig(getApplication());
            }
        } else if (clickEvent.getButton() == myCancel) {
            if (beforeCancel()) {
                getApplication().setMainComponent(new StatusPanel(getApplication(), myComponentFactory));
            }
        }
    }

}
