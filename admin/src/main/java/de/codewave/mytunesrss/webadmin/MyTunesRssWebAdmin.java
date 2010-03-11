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
import org.apache.commons.lang.exception.ExceptionUtils;

public class MyTunesRssWebAdmin extends Application {

    public void init() {
        setTheme("mytunesrss");
        Window main = new Window(MyTunesRssWebAdminUtils.getBundleString("mainWindowTitle", MyTunesRss.VERSION));
        main.getContent().setWidth(100, Sizeable.UNITS_PERCENTAGE);
        setMainWindow(main);
        main.addComponent(new LoginPanel(MyTunesRssWebAdminUtils.COMPONENT_FACTORY));
    }

    public void setMainComponent(Component component) {
        Window mainWindow = getMainWindow();
        mainWindow.removeAllComponents();
        mainWindow.addComponent(component);
    }

    public void showError(String messageKey, Object... parameters) {
        getMainWindow().showNotification(null, MyTunesRssWebAdminUtils.getBundleString(messageKey, parameters), Window.Notification.TYPE_ERROR_MESSAGE);
    }

    public void showWarning(String messageKey, Object... parameters) {
        getMainWindow().showNotification(null, MyTunesRssWebAdminUtils.getBundleString(messageKey, parameters), Window.Notification.TYPE_WARNING_MESSAGE);
    }

    public void showInfo(String messageKey, Object... parameters) {
        getMainWindow().showNotification(null, MyTunesRssWebAdminUtils.getBundleString(messageKey, parameters), Window.Notification.TYPE_HUMANIZED_MESSAGE);
    }

    public void handleException(Exception e) {
        // TODO send support request dialog
        getMainWindow().showNotification("An unexpected error has occured", ExceptionUtils.getFullStackTrace(e), Window.Notification.TYPE_ERROR_MESSAGE);
    }
}
