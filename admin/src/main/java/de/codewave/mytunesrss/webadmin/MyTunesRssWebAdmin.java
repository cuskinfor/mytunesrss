/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.ResourceBundleManager;
import de.codewave.utils.swing.components.PasswordHashField;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.component.MessageWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MyTunesRssWebAdmin extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssWebAdmin.class);

    public static final ResourceBundleManager RESOURCE_BUNDLE_MANAGER = new ResourceBundleManager(MyTunesRssWebAdmin.class.getClassLoader());

    public static final int ADMIN_REFRESHER_INTERVAL_MILLIS = 5000;

    public static String getBundleString(ResourceBundle bundle, String key, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return bundle.getString(key);
        }
        return MessageFormat.format(bundle.getString(key), parameters);
    }

    private ResourceBundle myBundle;

    private ComponentFactory myComponentFactory;

    private ValidatorFactory myValidatorFactory;


    public void init() {
        myBundle = RESOURCE_BUNDLE_MANAGER.getBundle("de.codewave.mytunesrss.webadmin.MyTunesRssAdmin", getLocale());
        myComponentFactory = new ComponentFactory(myBundle);
        myValidatorFactory = new ValidatorFactory(myBundle);
        setTheme("mytunesrss");
        setMainWindow(new MainWindow(getBundleString("mainWindowTitle", MyTunesRss.VERSION), getNewWindowPanel()));
    }

    private Panel getNewWindowPanel() {
        Panel panel;
        if (MyTunesRss.CONFIG.isShowInitialWizard()) {
            panel = new WizardPanel();
        } else if (MyTunesRss.CONFIG.isAdminPassword()) {
            panel = new LoginPanel();
        } else {
            panel = new StatusPanel();
        }
        return panel;
    }

    @Override
    public Window getWindow(String name) {
        Window window = super.getWindow(name);
        if (window == null) {
            window = new MainWindow(getBundleString("mainWindowTitle", MyTunesRss.VERSION), getNewWindowPanel());
            window.setName(name);
            addWindow(window);
        }
        return window;
    }

    public String getBundleString(String key, Object... parameters) {
        return getBundleString(myBundle, key, parameters);
    }

    public ComponentFactory getComponentFactory() {
        return myComponentFactory;
    }

    public ValidatorFactory getValidatorFactory() {
        return myValidatorFactory;
    }

    @Override
    public void terminalError(Terminal.ErrorEvent event) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("Unhandled exception.", event.getThrowable());
        }
        MyTunesRss.UNHANDLED_EXCEPTION.set(true);
    }

    @Override
    public String getLogoutURL() {
        return "/";
    }
}
