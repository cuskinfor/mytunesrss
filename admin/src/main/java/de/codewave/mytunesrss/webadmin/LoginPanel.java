/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.SmartPasswordField;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;

import java.util.Arrays;

public class LoginPanel extends Panel implements Button.ClickListener {

    private SmartPasswordField myPassword;

    public void attach() {
        super.attach();
        setCaption(getApplication().getBundleString("loginPanel.caption"));
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        myPassword = getApplication().getComponentFactory().createPasswordTextField("loginPanel.password");
        addComponent(myPassword);
        Button loginButton = getApplication().getComponentFactory().createButton("loginPanel.login", this);
        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        loginButton.addStyleName("primary");
        addComponent(loginButton);
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (Arrays.equals(MyTunesRss.CONFIG.getAdminPasswordHash(), myPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST.get()))) {
            getApplication().setUser("USER"); // we just need any non-NULL objects here
            ((WebApplicationContext)getApplication().getContext()).getHttpSession().setAttribute("MyTunesRSSWebAdmin", Boolean.TRUE);
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new StatusPanel());
        } else {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("loginPanel.error.invalidLogin");
        }
    }
}
