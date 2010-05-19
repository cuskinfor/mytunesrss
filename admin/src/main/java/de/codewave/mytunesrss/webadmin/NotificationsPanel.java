/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.MyTunesRssNotification;

import java.util.List;

public class NotificationsPanel extends Panel {
    private List<MyTunesRssNotification> myNotifications;

    public NotificationsPanel(List<MyTunesRssNotification> notifications) {
        myNotifications = notifications;
    }

    @Override
    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    @Override
    public void attach() {
        Accordion accordion = new Accordion();
        setContent(accordion);
        for (MyTunesRssNotification notification : myNotifications) {
            Panel notificationPanel = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
            notificationPanel.addComponent(new Label(notification.getDetail()));
            accordion.addTab(notificationPanel, notification.getHeader(), null);
        }
    }
}
