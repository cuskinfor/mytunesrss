/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;

import java.util.Arrays;

public class LoginPanel extends Panel implements Button.ClickListener {

    private ComponentFactory myComponentFactory;
    private SmartTextField myPassword;

    public LoginPanel(ComponentFactory componentFactory) {
        super(MyTunesRssWebAdminUtils.getBundleString("loginPanel.caption"), componentFactory.createVerticalLayout(true, true));
        myComponentFactory = componentFactory;
        init();
    }

    protected void init() {
        myPassword = myComponentFactory.createPasswordTextField("loginPanel.password");
        addComponent(myPassword);
        addComponent(myComponentFactory.createButton("loginPanel.login", this));
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        MyTunesRssWebAdmin application = ((MyTunesRssWebAdmin) getApplication());
        if (Arrays.equals(MyTunesRss.CONFIG.getAdminPasswordHash(), myPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST))) {
            application.setMainComponent(new StatusPanel(getApplication(), myComponentFactory));
        } else {
            application.showError("loginPanel.error.invalidLogin");
        }
    }
}
