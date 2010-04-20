/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.ComponentFactory;
import org.apache.commons.lang.exception.ExceptionUtils;

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

    public void init() {
        myBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.webadmin.MyTunesRssAdmin", getLocale());
        myComponentFactory = new ComponentFactory(myBundle);
        myValidatorFactory = new ValidatorFactory(myBundle);
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
        getMainWindow().showNotification(null, getBundleString(messageKey, parameters), Window.Notification.TYPE_ERROR_MESSAGE);
    }

    public void showWarning(String messageKey, Object... parameters) {
        getMainWindow().showNotification(null, getBundleString(messageKey, parameters), Window.Notification.TYPE_WARNING_MESSAGE);
    }

    public void showInfo(String messageKey, Object... parameters) {
        getMainWindow().showNotification(null, getBundleString(messageKey, parameters), Window.Notification.TYPE_HUMANIZED_MESSAGE);
    }

    public void handleException(Exception e) {
        MyTunesRss.ERROR_QUEUE.add(e);
        getMainWindow().showNotification("An unexpected error has occured. Please consider sending a support request now.", ExceptionUtils.getFullStackTrace(e), Window.Notification.TYPE_ERROR_MESSAGE);
        getMainWindow().setContent(new SupportConfigPanel());
    }

    @Override
    public String getLogoutURL() {
        return "/";
    }
}
