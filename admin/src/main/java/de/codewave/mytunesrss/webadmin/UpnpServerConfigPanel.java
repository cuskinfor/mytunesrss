/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Form;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class UpnpServerConfigPanel extends MyTunesRssConfigPanel {

    private Form myServerForm;
    private CheckBox myServerActiveCheckbox;
    private SmartTextField myServerName;

    public void attach() {
        super.attach();
        init(getBundleString("upnpServerConfigPanel.caption"), getComponentFactory().createGridLayout(1, 2, true, true));
        myServerActiveCheckbox = getComponentFactory().createCheckBox("upnpServerConfigPanel.server.active");
        myServerName = getComponentFactory().createTextField("upnpServerConfigPanel.server.name", new StringLengthValidator(getBundleString("upnpServerConfigPanel.error.name", 100), 0, 100, true));
        myServerForm = getComponentFactory().createForm(null, false);
        myServerForm.addField(myServerActiveCheckbox, myServerActiveCheckbox);
        myServerForm.addField(myServerName, myServerName);
        addComponent(getComponentFactory().surroundWithPanel(myServerForm, FORM_PANEL_MARGIN_INFO, getBundleString("upnpServerConfigPanel.caption.server")));
        addDefaultComponents(0, 1, 0, 1, false);
        initFromConfig();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myServerForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    @Override
    protected void writeToConfig() {
        String oldServerName = StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUpnpMediaServerName());
        boolean oldServerState = MyTunesRss.CONFIG.isUpnpMediaServerActive();
        MyTunesRss.CONFIG.setUpnpMediaServerActive(myServerActiveCheckbox.booleanValue());
        MyTunesRss.CONFIG.setUpnpMediaServerName(StringUtils.trimToNull(myServerName.getStringValue(null)));
        if (MyTunesRss.CONFIG.isUpnpMediaServerActive() != oldServerState || !StringUtils.equals(oldServerName, StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUpnpMediaServerName()))) {
            MyTunesRss.stopUpnpMediaServer();
            MyTunesRss.startUpnpMediaServer();
        }
    }

    @Override
    protected void initFromConfig() {
        myServerActiveCheckbox.setValue(MyTunesRss.CONFIG.isUpnpMediaServerActive());
        myServerName.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUpnpMediaServerName()));
    }

}
