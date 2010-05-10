/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.component.MessageWindow;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class MyTunesRssWebAdmin extends Application {

    public static String getBundleString(ResourceBundle bundle, String key, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return bundle.getString(key);
        }
        return MessageFormat.format(bundle.getString(key), parameters);
    }

    private ResourceBundle myBundle;

    private ComponentFactory myComponentFactory;

    private ValidatorFactory myValidatorFactory;

    private StatusPanel myStatusPanel;

    public void init() {
        myBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.webadmin.MyTunesRssAdmin", getLocale());
        myComponentFactory = new ComponentFactory(myBundle);
        myValidatorFactory = new ValidatorFactory(myBundle);
        myStatusPanel = new StatusPanel();
        MyTunesRssEventManager.getInstance().addListener(myStatusPanel);
        setTheme("mytunesrss");
        Window main = new Window(getBundleString("mainWindowTitle", MyTunesRss.VERSION));
        main.getContent().setWidth(100, Sizeable.UNITS_PERCENTAGE);
        setMainWindow(main);
        main.addComponent(new LoginPanel());
    }

    public String getBundleString(String key, Object... parameters) {
        return getBundleString(myBundle, key, parameters);
    }

    public void setMainComponent(Component component) {
        Window mainWindow = getMainWindow();
        mainWindow.removeAllComponents();
        mainWindow.addComponent(component);
    }

    public ComponentFactory getComponentFactory() {
        return myComponentFactory;
    }

    public ValidatorFactory getValidatorFactory() {
        return myValidatorFactory;
    }

    public void showError(String messageKey, Object... parameters) {
        new MessageWindow(50, Sizeable.UNITS_EM, null, null, getBundleString(messageKey, parameters), new Button(getBundleString("button.ok"))) {
            @Override
            protected void onClick(Button button) {
                // intentionally left blank
            }
        }.show(getMainWindow());
    }

    public void showWarning(String messageKey, Object... parameters) {
        new MessageWindow(50, Sizeable.UNITS_EM, null, null, getBundleString(messageKey, parameters), new Button(getBundleString("button.ok"))) {
            @Override
            protected void onClick(Button button) {
                // intentionally left blank
            }
        }.show(getMainWindow());
    }

    public void showInfo(String messageKey, Object... parameters) {
        new MessageWindow(50, Sizeable.UNITS_EM, null, null, getBundleString(messageKey, parameters), new Button(getBundleString("button.ok"))) {
            @Override
            protected void onClick(Button button) {
                // intentionally left blank
            }
        }.show(getMainWindow());
    }

    public void handleException(Exception e) {
        MyTunesRss.ERROR_QUEUE.add(e);
        getMainWindow().showNotification("An unexpected error has occured. Please consider sending a support request now.", ExceptionUtils.getFullStackTrace(e), Window.Notification.TYPE_ERROR_MESSAGE);
        getMainWindow().setContent(new SupportConfigPanel());
    }

    public StatusPanel getStatusPanel() {
        return myStatusPanel;
    }

    @Override
    public String getLogoutURL() {
        return "/";
    }
}