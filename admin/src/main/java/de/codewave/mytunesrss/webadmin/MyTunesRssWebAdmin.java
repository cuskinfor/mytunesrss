/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;

public class MyTunesRssWebAdmin extends Application {
    public void init() {
        Window main = new Window("MyTunesRSS Web Admin");
        setMainWindow(main);
        Tree configPages = new Tree("MyTunesRSS configuration");
        for (String item : new String[] {"Status", "Server", "Database", "Data sources", "Data import", "Content", "User management", "Admin notifications", "Statistics", "Miscellaneous", "Streaming", "Addons", "Support and registration"}) {
            configPages.addItem(item);
            configPages.setChildrenAllowed(item, false);
        }
        configPages.setImmediate(true);
        final SplitPanel splitter = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);
        configPages.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                Panel p = new Panel();
                Form f = new Form();
                f.setCaption("Allgemeine Einstellungen");
                f.addField("autoStartServer", new CheckBox(MyTunesRssUtils.getBundleString("settings.autoStartServer"), MyTunesRss.CONFIG.isAutoStartServer()));
                f.addField("localTempArchive", new CheckBox(MyTunesRssUtils.getBundleString("settings.tempZipArchives"), MyTunesRss.CONFIG.isLocalTempArchive()));
                f.addField("availableOnLocalNet", new CheckBox(MyTunesRssUtils.getBundleString("settings.availableOnLocalNet"), MyTunesRss.CONFIG.isAvailableOnLocalNet()));
                TextField serverNameTextField = new TextField(MyTunesRssUtils.getBundleString("settings.serverName"), MyTunesRss.CONFIG.getServerName());
                serverNameTextField.setEnabled(MyTunesRss.CONFIG.isAvailableOnLocalNet());
                f.addField("availableOnLocalNet", serverNameTextField);
                p.addComponent(f);
                splitter.setSecondComponent(p);
            }
        });
        splitter.setFirstComponent(configPages);
        splitter.setSecondComponent(new Panel("Overview Panel"));
        main.addComponent(splitter);
    }
}
