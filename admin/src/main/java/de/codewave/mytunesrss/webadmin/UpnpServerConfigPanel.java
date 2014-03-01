/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Form;
import de.codewave.mytunesrss.MyTunesRss;

public class UpnpServerConfigPanel extends MyTunesRssConfigPanel {

    private CheckBox myServerActiveCheckbox;

    public void attach() {
        super.attach();
        init(getBundleString("upnpServerConfigPanel.caption"), getComponentFactory().createGridLayout(1, 2, true, true));
        myServerActiveCheckbox = getComponentFactory().createCheckBox("upnpServerConfigPanel.server.active");
        Form form = getComponentFactory().createForm(null, false);
        form.addField(myServerActiveCheckbox, myServerActiveCheckbox);
        addComponent(getComponentFactory().surroundWithPanel(form, FORM_PANEL_MARGIN_INFO, getBundleString("upnpServerConfigPanel.caption.server")));
        addDefaultComponents(0, 1, 0, 1, false);
        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        MyTunesRss.CONFIG.setUpnpMediaServerActive(myServerActiveCheckbox.booleanValue());
    }

    @Override
    protected void initFromConfig() {
        myServerActiveCheckbox.setValue(MyTunesRss.CONFIG.isUpnpMediaServerActive());
    }

}
