/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import org.apache.commons.lang.StringUtils;

public class LoginPanel extends Panel implements Button.ClickListener {

    private ComponentFactory myComponentFactory;
    private SmartTextField myUsername;
    private SmartTextField myPassword;

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
        if (StringUtils.isEmpty((String)myUsername.getValue()) && StringUtils.isEmpty((String)myPassword.getValue())) {
            application.setMainComponent(new StatusPanel(getApplication(), myComponentFactory));
        } else {
            application.showError("loginPanel.error.invalidLogin");
        }
    }
}
