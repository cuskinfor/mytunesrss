/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.vaadin.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConfigPanel extends MyTunesRssConfigPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserConfigPanel.class);

    private Panel myUserTreePanel;
    private Tree myUserTree;

    public UserConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("userConfigPanel.caption"), componentFactory.createGridLayout(1, 2, true, true), componentFactory);
    }

    protected void init(Application application) {
        myUserTreePanel = new Panel(getBundleString("userConfigPanel.caption.themes"), getComponentFactory().createVerticalLayout(true, true));
        myUserTree = new Tree();
        myUserTreePanel.addComponent(myUserTree);
        addComponent(myUserTreePanel);

        addMainButtons(0, 1, 0, 1);

    }

    protected void initFromConfig(Application application) {
        myUserTree.removeAllItems();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            myUserTree.addItem(user);
            myUserTree.setChildrenAllowed(user, false);
            if (user.getParent() != null) {
                myUserTree.setParent(user, user.getParent());
                myUserTree.setChildrenAllowed(user.getParent(), true);
            }
        }
    }

    protected void writeToConfig() {

    }

    @Override
    protected boolean beforeSave() {
        return true;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }
}