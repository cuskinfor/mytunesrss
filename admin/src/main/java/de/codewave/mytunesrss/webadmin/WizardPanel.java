/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.DatabaseJobRunningException;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.webadmin.datasource.DatasourcesConfigPanel;
import de.codewave.vaadin.SmartField;
import de.codewave.vaadin.SmartPasswordField;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.UUID;

public class WizardPanel extends Panel implements Button.ClickListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WizardPanel.class);

    private Button myAddDatasourceButton;
    private SmartTextField myDatasourcePath;
    private SmartTextField myUsername;
    private SmartPasswordField myPassword;
    private SmartPasswordField myRetypePassword;
    private Button myFinishButton;
    private Button mySkipButton;

    public void attach() {
        super.attach();
        setCaption(getApplication().getBundleString("wizardPanel.caption"));
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        myDatasourcePath = getApplication().getComponentFactory().createTextField("wizardPanel.datasource");
        myAddDatasourceButton = getApplication().getComponentFactory().createButton("wizardPanel.addDatasource", this);
        myUsername = getApplication().getComponentFactory().createTextField("wizardPanel.username");
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
        Panel mainButtons = new Panel();
        addComponent(mainButtons);
        mainButtons.addStyleName("light");
        mainButtons.setContent(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        mainButtons.addComponent(myFinishButton);
        mainButtons.addComponent(mySkipButton);
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddDatasourceButton) {
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getApplication().getBundleString("datasourcesConfigPanel.caption.selectLocalDatasource"), null, ServerSideFileChooser.PATTERN_ALL, DatasourcesConfigPanel.XML_FILE_PATTERN, false, getApplication().getServerSideFileChooserLabels()) {
                @Override
                protected void onFileSelected(File file) {
                    myDatasourcePath.setValue(file.getAbsolutePath());
                    getParent().removeWindow(this);
                }
            }.show(getWindow());
        } else {
            final MainWindow applicationWindow = (MainWindow) VaadinUtils.getApplicationWindow(this);
            if (clickEvent.getSource() == myFinishButton) {
                if (isAnyEmpty(myDatasourcePath, myUsername, myPassword, myRetypePassword)) {
                    applicationWindow.showError("wizardPanel.error.allFieldsMandatory");
                } else {
                    DatasourceConfig datasourceConfig = DatasourceConfig.create(UUID.randomUUID().toString(), "Default", myDatasourcePath.getStringValue(null));
                    if (datasourceConfig == null) {
                        applicationWindow.showError("error.invalidDatasourcePath");
                    } else if (!myPassword.getStringValue("1").equals(myRetypePassword.getStringValue("2"))) {
                        applicationWindow.showError("editUserConfigPanel.error.retypePassword");
                    } else {
                        MyTunesRss.CONFIG.setDatasources(Collections.singletonList(datasourceConfig));
                        User user = new User(myUsername.getStringValue(null));
                        user.setPasswordHash(myPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST.get()));
                        user.setEmptyPassword(false);
                        MyTunesRss.CONFIG.addUser(user);
                        MyTunesRss.CONFIG.setInitialWizard(false); // do not run wizard again
                        MyTunesRss.CONFIG.save();
                        try {
                            MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(MyTunesRss.CONFIG.getDatasources(), true);
                        } catch (DatabaseJobRunningException e) {
                            LOGGER.error("There was already a database job running!", e);
                        }
                        applicationWindow.showBlockingMessage("wizard.working.message");
                        Thread wizardWatchdog = new Thread(new Runnable() {
                            public void run() {
                                while (MyTunesRss.EXECUTOR_SERVICE.isDatabaseJobRunning()) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ignored) {
                                        LOGGER.info("Wizard watchdog thread has been interrupted!");
                                    }
                                }
                                applicationWindow.hideBlockingMessage();
                            }
                        }, "WizardWatchdog");
                        wizardWatchdog.setDaemon(true);
                        wizardWatchdog.start();
                        applicationWindow.showComponent(new StatusPanel());
                    }
                }
            } else if (clickEvent.getSource() == mySkipButton) {
                MyTunesRss.CONFIG.setInitialWizard(false); // do not run wizard again
                MyTunesRss.CONFIG.save();
                applicationWindow.showComponent(getApplication().getNewWindowPanel());
            }
        }
    }

    private boolean isAnyEmpty(SmartField... fields) {
        for (SmartField field : fields) {
            if (StringUtils.isEmpty(field.getStringValue(null))) {
                return true;
            }
        }
        return false;
    }
}
