/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.DatasourceConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;

public class WizardPanel extends Panel implements Button.ClickListener {

    private Button myAddDatasourceButton;
    private Label myDatasourcePath;
    private SmartTextField myUsername;
    private SmartTextField myPassword;
    private SmartTextField myRetypePassword;
    private Button myFinishButton;
    private Button mySkipButton;

    public void attach() {
        super.attach();
        setCaption(getApplication().getBundleString("wizardPanel.caption"));
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        myDatasourcePath = new Label(getApplication().getBundleString("wizardPanel.datasource", ""));
        myAddDatasourceButton = getApplication().getComponentFactory().createButton("wizardPanel.addDatasource", this);
        myUsername = getApplication().getComponentFactory().createPasswordTextField("wizardPanel.username");
        myPassword = getApplication().getComponentFactory().createPasswordTextField("wizardPanel.password");
        myRetypePassword = getApplication().getComponentFactory().createPasswordTextField("wizardPanel.retypePassword");
        myFinishButton = getApplication().getComponentFactory().createButton("wizardPanel.finish", this);
        mySkipButton = getApplication().getComponentFactory().createButton("wizardPanel.skip", this);
        addComponent(new Label(getApplication().getBundleString("wizardPanel.descGeneral")));
        addComponent(new Label(getApplication().getBundleString("wizardPanel.descDatasource")));
        addComponent(myDatasourcePath);
        addComponent(myAddDatasourceButton);
        addComponent(new Label(getApplication().getBundleString("wizardPanel.descUser")));
        addComponent(myUsername);
        addComponent(myPassword);
        addComponent(myRetypePassword);
        addComponent(new Label(getApplication().getBundleString("wizardPanel.descButtons")));
        addComponent(myFinishButton);
        addComponent(mySkipButton);
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddDatasourceButton) {
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getApplication().getBundleString("datasourcesConfigPanel.caption.selectLocalDatasource"), null, ServerSideFileChooser.PATTERN_ALL, DatasourcesConfigPanel.XML_FILE_PATTERN, false, "Roots") { // TODO i18n
                @Override
                protected void onFileSelected(File file) {
                    MyTunesRss.CONFIG.setDatasources(Collections.singletonList(DatasourceConfig.create(file.getAbsolutePath())));
                    myDatasourcePath.setValue(((MyTunesRssWebAdmin)getApplication()).getBundleString("wizardPanel.datasource", file.getAbsolutePath()));
                    getParent().removeWindow(this);
                }
            }.show(getWindow());
        } else if (clickEvent.getSource() == myFinishButton) {
            if (MyTunesRss.CONFIG.getDatasources().size() == 0 || isAnyEmpty(myUsername, myPassword, myRetypePassword)) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("wizardPanel.error.allFieldsMandatory");
            } else if (!myPassword.getStringValue("1").equals(myRetypePassword.getStringValue("2"))) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("editUserConfigPanel.error.retypePassword");
            } else {
                User user = new User(myUsername.getStringValue(null));
                user.setPasswordHash(myPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST));
                MyTunesRss.CONFIG.addUser(user);
                MyTunesRss.CONFIG.setInitialWizard(false); // do not run wizard again
                MyTunesRss.CONFIG.save();
                MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(true);
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new WizardWorkingPanel());
            }
        } else if (clickEvent.getSource() == mySkipButton) {
            MyTunesRss.CONFIG.setDatasources(Collections.<DatasourceConfig>emptyList());
        }
    }

    private boolean isAnyEmpty(SmartTextField... fields) {
        for (SmartTextField field : fields) {
            if (StringUtils.isEmpty(field.getStringValue(null))) {
                return true;
            }
        }
        return false;
    }
}
