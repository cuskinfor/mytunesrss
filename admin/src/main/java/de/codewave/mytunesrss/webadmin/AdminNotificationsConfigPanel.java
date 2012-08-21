/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Form;
import de.codewave.mytunesrss.MyTunesRss;
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
    private CheckBox myNotifyOnOutdatedItunesXml;
    private CheckBox myNotifyOnSkippedDatabaseUpdate;

    public void attach() {
        super.attach();
        init(getBundleString("adminNotificationsConfigPanel.caption"), getComponentFactory().createGridLayout(1, 3, true, true));
        myEmailForm = getComponentFactory().createForm(null, true);
        myNotificationsForm = getComponentFactory().createForm(null, true);
        myAdminEmail = getComponentFactory().createTextField("adminNotificationsConfigPanel.adminEmail", getApplication().getValidatorFactory().createEmailValidator());
        myNotifyOnDatabaseUpdate = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnDatabaseUpdate");
        myNotifyOnEmailChange = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnEmailChange");
        myNotifyOnInternalError = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnInternalError");
        myNotifyOnLoginFailure = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnLoginFailure");
        myNotifyOnPasswordChange = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnPasswordChange");
        myNotifyOnQuotaExceeded = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnQuotaExceeded");
        myNotifyOnTranscodingFailure = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnTranscodingFailure");
        myNotifyOnMissingFile = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnMissingFile");
        myNotifyOnOutdatedItunesXml = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnOutdatedItunesXml");
        myNotifyOnSkippedDatabaseUpdate = getComponentFactory().createCheckBox("adminNotificationsConfigPanel.notifyOnSkippedDatabaseUpdate");
        myEmailForm.addField(myAdminEmail, myAdminEmail);
        myNotificationsForm.addField(myNotifyOnDatabaseUpdate, myNotifyOnDatabaseUpdate);
        myNotificationsForm.addField(myNotifyOnEmailChange, myNotifyOnEmailChange);
        myNotificationsForm.addField(myNotifyOnInternalError, myNotifyOnInternalError);
        myNotificationsForm.addField(myNotifyOnLoginFailure, myNotifyOnLoginFailure);
        myNotificationsForm.addField(myNotifyOnMissingFile, myNotifyOnMissingFile);
        myNotificationsForm.addField(myNotifyOnPasswordChange, myNotifyOnPasswordChange);
        myNotificationsForm.addField(myNotifyOnQuotaExceeded, myNotifyOnQuotaExceeded);
        myNotificationsForm.addField(myNotifyOnTranscodingFailure, myNotifyOnTranscodingFailure);
        myNotificationsForm.addField(myNotifyOnOutdatedItunesXml, myNotifyOnOutdatedItunesXml);
        myNotificationsForm.addField(myNotifyOnSkippedDatabaseUpdate, myNotifyOnSkippedDatabaseUpdate);
        addComponent(getComponentFactory().surroundWithPanel(myEmailForm, FORM_PANEL_MARGIN_INFO, getBundleString("adminNotificationsConfigPanel.email.caption")));
        addComponent(getComponentFactory().surroundWithPanel(myNotificationsForm, FORM_PANEL_MARGIN_INFO, getBundleString("adminNotificationsConfigPanel.notifications.caption")));

        addDefaultComponents(0, 2, 0, 2, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myAdminEmail.setValue(MyTunesRss.CONFIG.getAdminEmail());
        myNotifyOnDatabaseUpdate.setValue(MyTunesRss.CONFIG.isNotifyOnDatabaseUpdate());
        myNotifyOnEmailChange.setValue(MyTunesRss.CONFIG.isNotifyOnEmailChange());
        myNotifyOnInternalError.setValue(MyTunesRss.CONFIG.isNotifyOnInternalError());
        myNotifyOnLoginFailure.setValue(MyTunesRss.CONFIG.isNotifyOnLoginFailure());
        myNotifyOnMissingFile.setValue(MyTunesRss.CONFIG.isNotifyOnMissingFile());
        myNotifyOnPasswordChange.setValue(MyTunesRss.CONFIG.isNotifyOnPasswordChange());
        myNotifyOnQuotaExceeded.setValue(MyTunesRss.CONFIG.isNotifyOnQuotaExceeded());
        myNotifyOnTranscodingFailure.setValue(MyTunesRss.CONFIG.isNotifyOnTranscodingFailure());
        myNotifyOnOutdatedItunesXml.setValue(MyTunesRss.CONFIG.isNotifyOnOutdatedItunesXml());
        myNotifyOnSkippedDatabaseUpdate.setValue(MyTunesRss.CONFIG.isNotifyOnSkippedDatabaseUpdate());
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
        MyTunesRss.CONFIG.setNotifyOnOutdatedItunesXml((Boolean) myNotifyOnOutdatedItunesXml.getValue());
        MyTunesRss.CONFIG.setNotifyOnSkippedDatabaseUpdate((Boolean) myNotifyOnSkippedDatabaseUpdate.getValue());
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myEmailForm, myNotificationsForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }
}