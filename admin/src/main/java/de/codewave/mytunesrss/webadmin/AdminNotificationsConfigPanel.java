/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Form;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminNotificationsConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(AdminNotificationsConfigPanel.class);

    private Form myEmailForm;
    private Form myNotificationsForm;
    private SmartTextField myAdminEmail;
    private CheckBox myNotifyOnDatabaseUpdate;
    private CheckBox myNotifyOnEmailChange;
    private CheckBox myNotifyOnInternalError;
    private CheckBox myNotifyOnLoginFailure;
    private CheckBox myNotifyOnMissingFile;
    private CheckBox myNotifyOnPasswordChange;
    private CheckBox myNotifyOnQuotaExceeded;
    private CheckBox myNotifyOnTranscodingFailure;
    private CheckBox myNotifyOnWebUpload;

    public AdminNotificationsConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("adminNotificationsConfigPanel.caption"), componentFactory.createGridLayout(1, 3, true, true), componentFactory);
    }

    protected void init(Application application) {
        myEmailForm = getComponentFactory().createForm(null, true);
        myNotificationsForm = getComponentFactory().createForm(null, true);
        myAdminEmail = getComponentFactory().createTextField("adminNotificationsConfigPanel.adminEmail", ValidatorFactory.createEmailValidator());
        myNotifyOnDatabaseUpdate = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnDatabaseUpdate");
        myNotifyOnEmailChange = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnEmailChange");
        myNotifyOnInternalError = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnInternalError");
        myNotifyOnLoginFailure = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnLoginFailure");
        myNotifyOnPasswordChange = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnPasswordChange");
        myNotifyOnQuotaExceeded = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnQuotaExceeded");
        myNotifyOnTranscodingFailure = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnTranscodingFailure");
        myNotifyOnMissingFile = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnMissingFile");
        myNotifyOnWebUpload = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnWebUpdate");
        myEmailForm.addField(myAdminEmail, myAdminEmail);
        myNotificationsForm.addField(myNotifyOnDatabaseUpdate, myNotifyOnDatabaseUpdate);
        myNotificationsForm.addField(myNotifyOnEmailChange, myNotifyOnEmailChange);
        myNotificationsForm.addField(myNotifyOnInternalError, myNotifyOnInternalError);
        myNotificationsForm.addField(myNotifyOnLoginFailure, myNotifyOnLoginFailure);
        myNotificationsForm.addField(myNotifyOnMissingFile, myNotifyOnMissingFile);
        myNotificationsForm.addField(myNotifyOnPasswordChange, myNotifyOnPasswordChange);
        myNotificationsForm.addField(myNotifyOnQuotaExceeded, myNotifyOnQuotaExceeded);
        myNotificationsForm.addField(myNotifyOnTranscodingFailure, myNotifyOnTranscodingFailure);
        myNotificationsForm.addField(myNotifyOnWebUpload, myNotifyOnWebUpload);
        addComponent(getComponentFactory().surroundWithPanel(myEmailForm, FORM_PANEL_MARGIN_INFO, getBundleString("adminNotificationsConfigPanel.email.caption")));
        addComponent(getComponentFactory().surroundWithPanel(myNotificationsForm, FORM_PANEL_MARGIN_INFO, getBundleString("adminNotificationsConfigPanel.notifications.caption")));

        addMainButtons(0, 2, 0, 2);
    }

    protected void initFromConfig(Application application) {
        myAdminEmail.setValue(MyTunesRss.CONFIG.getAdminEmail());
        myNotifyOnDatabaseUpdate.setValue(MyTunesRss.CONFIG.isNotifyOnDatabaseUpdate());
        myNotifyOnEmailChange.setValue(MyTunesRss.CONFIG.isNotifyOnEmailChange());
        myNotifyOnInternalError.setValue(MyTunesRss.CONFIG.isNotifyOnInternalError());
        myNotifyOnLoginFailure.setValue(MyTunesRss.CONFIG.isNotifyOnLoginFailure());
        myNotifyOnMissingFile.setValue(MyTunesRss.CONFIG.isNotifyOnMissingFile());
        myNotifyOnPasswordChange.setValue(MyTunesRss.CONFIG.isNotifyOnPasswordChange());
        myNotifyOnQuotaExceeded.setValue(MyTunesRss.CONFIG.isNotifyOnQuotaExceeded());
        myNotifyOnTranscodingFailure.setValue(MyTunesRss.CONFIG.isNotifyOnTranscodingFailure());
        myNotifyOnWebUpload.setValue(MyTunesRss.CONFIG.isNotifyOnWebUpload());
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setAdminEmail(myAdminEmail.getStringValue(null));
        MyTunesRss.CONFIG.setNotifyOnDatabaseUpdate((Boolean) myNotifyOnDatabaseUpdate.getValue());
        MyTunesRss.CONFIG.setNotifyOnEmailChange((Boolean) myNotifyOnEmailChange.getValue());
        MyTunesRss.CONFIG.setNotifyOnInternalError((Boolean) myNotifyOnInternalError.getValue());
        MyTunesRss.CONFIG.setNotifyOnLoginFailure((Boolean) myNotifyOnLoginFailure.getValue());
        MyTunesRss.CONFIG.setNotifyOnMissingFile((Boolean) myNotifyOnMissingFile.getValue());
        MyTunesRss.CONFIG.setNotifyOnPasswordChange((Boolean) myNotifyOnPasswordChange.getValue());
        MyTunesRss.CONFIG.setNotifyOnQuotaExceeded((Boolean) myNotifyOnQuotaExceeded.getValue());
        MyTunesRss.CONFIG.setNotifyOnTranscodingFailure((Boolean) myNotifyOnTranscodingFailure.getValue());
        MyTunesRss.CONFIG.setNotifyOnWebUpload((Boolean) myNotifyOnWebUpload.getValue());
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myEmailForm, myNotificationsForm);
        if (!valid) {
            getApplication().showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }
}