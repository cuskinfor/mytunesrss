/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.SmartTextField;

import java.util.Arrays;

public class LoginPanel extends Panel implements Button.ClickListener {

    private SmartTextField myPassword;

    public void attach() {
        setCaption(getApplication().getBundleString("loginPanel.caption"));
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        myPassword = getApplication().getComponentFactory().createPasswordTextField("loginPanel.password");
        addComponent(myPassword);
        addComponent(getApplication().getComponentFactory().createButton("loginPanel.login", this));
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (Arrays.equals(MyTunesRss.CONFIG.getAdminPasswordHash(), myPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST))) {
            getApplication().setMainComponent(getApplication().getStatusPanel());
        } else {
            getApplication().showError("loginPanel.error.invalidLogin");
        }
    }
}
