/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.vaadin.ComponentFactory;

public abstract class MyTunesRssConfigPanel extends Panel implements Button.ClickListener {
    protected static final Layout.MarginInfo FORM_PANEL_MARGIN_INFO = new Layout.MarginInfo(false, true, false, true);

    private Button mySave;
    private Button myReset;
    private Button myCancel;
    private ComponentFactory myComponentFactory;

    public MyTunesRssConfigPanel(String caption, GridLayout content, ComponentFactory componentFactory) {
        super(caption, content);
        getGridLayout().setWidth(100, Sizeable.UNITS_PERCENTAGE);
        for (int i = 0; i < getGridLayout().getColumns(); i++) {
            getGridLayout().setColumnExpandRatio(i, 1);
        }
        myComponentFactory = componentFactory;
        init(componentFactory);
        initFromConfig();
    }

    protected void addMainButtons(int columnn1, int row1, int column2, int row2) {
        mySave = myComponentFactory.createButton("save", this);
        myReset = myComponentFactory.createButton("reset", this);
        myCancel = myComponentFactory.createButton("cancel", this);
        Panel mainButtons = new Panel();
        mainButtons.addStyleName("light");
        mainButtons.setContent(myComponentFactory.createHorizontalLayout(false, true));
        mainButtons.addComponent(mySave);
        mainButtons.addComponent(myReset);
        mainButtons.addComponent(myCancel);
        getGridLayout().addComponent(mainButtons, columnn1, row1, column2, row2);
        getGridLayout().setComponentAlignment(mainButtons, Alignment.MIDDLE_RIGHT);
    }

    protected abstract void init(ComponentFactory componentFactory);

    protected abstract void writeToConfig();

    protected abstract void initFromConfig();

    protected abstract boolean isPanelValid();

    protected GridLayout getGridLayout() {
        return (GridLayout) getContent();
    }

    protected ComponentFactory getComponentFactory() {
        return myComponentFactory;
    }
    
    public void buttonClick(Button.ClickEvent clickEvent) {
        MyTunesRssWebAdmin application = ((MyTunesRssWebAdmin) getApplication());
        if (clickEvent.getButton() == mySave) {
            if (!isPanelValid()) {
                application.showError("error.formInvalid");
                return;
            }
            writeToConfig();
            application.setMainComponent(new StatusPanel(myComponentFactory));
        } else if (clickEvent.getButton() == myReset) {
            initFromConfig();
        } else if (clickEvent.getButton() == myCancel) {
            application.setMainComponent(new StatusPanel(myComponentFactory));
        }
    }

}
