/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import de.codewave.vaadin.ComponentFactory;

public class LoginPanel extends Panel implements Button.ClickListener {

    private ComponentFactory myComponentFactory;
    private TextField myUsername;
    private TextField myPassword;

    public LoginPanel(ComponentFactory componentFactory) {
        super(MyTunesRssWebAdminUtils.getBundleString("loginPanel.caption"), componentFactory.createVerticalLayout(true, true));
        myComponentFactory = componentFactory;
        init();
    }

    protected void init() {
        myUsername = myComponentFactory.createTextField("loginPanel.username");
        addComponent(myUsername);
        myPassword = myComponentFactory.createPasswordTextField("loginPanel.password");
        addComponent(myPassword);
        addComponent(myComponentFactory.createButton("loginPanel.login", this));
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        MyTunesRssWebAdmin application = ((MyTunesRssWebAdmin) getApplication());
        if ("a".equals(myUsername.getValue()) && "a".equals(myPassword.getValue())) {
            application.setMainComponent(new StatusPanel(myComponentFactory));
        } else {
            application.showError("loginPanel.error.invalidLogin");
        }
    }
}
