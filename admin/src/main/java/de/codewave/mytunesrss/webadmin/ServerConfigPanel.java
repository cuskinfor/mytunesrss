/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.FileValidator;
import de.codewave.vaadin.validation.SameValidator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ServerConfigPanel extends MyTunesRssConfigPanel {

    private Form myAdminForm;
    private Form myGeneralForm;
    private Form myExtendedForm;
    private Form myHttpForm;
    private Form myHttpsForm;
    private SmartTextField myAdminPort;
    private SmartTextField myAdminPassword;
    private SmartTextField myRetypeAdminPassword;
    private CheckBox myLocalTempArchive;
    private CheckBox myAvailableOnLocalNet;
    private SmartTextField myServerName;
    private SmartTextField myWebappContext;
    private SmartTextField myTomcatMaxThreads;
    private SmartTextField myTomcatAjpPort;
    private SmartTextField myPort;
    private SmartTextField mySslPort;
    private SmartTextField mySslKeystoreFile;
    private Button mySslKeystoreFileSelect;
    private SmartTextField mySslKeystorePass;
    private SmartTextField mySslKeystoreKeyAlias;
    private CheckBox myUpnpAdmin;
    private CheckBox myUpnpUserHttp;
    private CheckBox myUpnpUserHttps;

    public void attach() {
        super.attach();
        init(getBundleString("serverConfigPanel.caption"), getComponentFactory().createGridLayout(1, 6, true, true));
        myAdminPort = getComponentFactory().createTextField("serverConfigPanel.adminPort", getApplication().getValidatorFactory().createPortValidator());
        myAdminPassword = getComponentFactory().createPasswordTextField("serverConfigPanel.adminPassword");
        myRetypeAdminPassword = getComponentFactory().createPasswordTextField("serverConfigPanel.retypeAdminPassword", new SameValidator(myAdminPassword, getBundleString("serverConfigPanel.error.retypeAdminPassword")));
        myLocalTempArchive = getComponentFactory().createCheckBox("serverConfigPanel.localTempArchive");
        myAvailableOnLocalNet = getComponentFactory().createCheckBox("serverConfigPanel.availableOnLocalNet");
        myAvailableOnLocalNet.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                myServerName.setEnabled(myAvailableOnLocalNet.booleanValue());
            }
        });
        myServerName = getComponentFactory().createTextField("serverConfigPanel.serverName");
        myWebappContext = getComponentFactory().createTextField("serverConfigPanel.webappContext");
        myTomcatMaxThreads = getComponentFactory().createTextField("serverConfigPanel.tomcatMaxThreads", getApplication().getValidatorFactory().createMinMaxValidator(5, 1000));
        setRequired(myTomcatMaxThreads);
        myTomcatAjpPort = getComponentFactory().createTextField("serverConfigPanel.tomcatAjpPort", getApplication().getValidatorFactory().createPortValidator());
        myPort = getComponentFactory().createTextField("serverConfigPanel.port", getApplication().getValidatorFactory().createPortValidator());
        setRequired(myPort);
        mySslPort = getComponentFactory().createTextField("serverConfigPanel.sslPort", getApplication().getValidatorFactory().createPortValidator());
        mySslKeystoreFile = getComponentFactory().createTextField("serverConfigPanel.sslKeystoreFile", new FileValidator(getBundleString("serverConfigPanel.error.invalidKeystore"), null, FileValidator.PATTERN_ALL));
        mySslKeystoreFileSelect = getComponentFactory().createButton("serverConfigPanel.sslKeystoreFile.select", this);
        mySslKeystorePass = getComponentFactory().createPasswordTextField("serverConfigPanel.sslKeystorePass");
        mySslKeystoreKeyAlias = getComponentFactory().createTextField("serverConfigPanel.sslKeystoreKeyAlias");
        myUpnpAdmin = getComponentFactory().createCheckBox("serverConfigPanel.upnp");
        myUpnpUserHttp = getComponentFactory().createCheckBox("serverConfigPanel.upnp");
        myUpnpUserHttps = getComponentFactory().createCheckBox("serverConfigPanel.upnp");

        myAdminForm = getComponentFactory().createForm(null, true);
        myAdminForm.addField(myAdminPort, myAdminPort);
        myAdminForm.addField(myUpnpAdmin, myUpnpAdmin);
        myAdminForm.addField(myAdminPassword, myAdminPassword);
        myAdminForm.addField(myRetypeAdminPassword, myRetypeAdminPassword);
        Panel adminPanel = getComponentFactory().surroundWithPanel(myAdminForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.admin"));
        addComponent(adminPanel);

        myGeneralForm = getComponentFactory().createForm(null, true);
        myGeneralForm.addField(myLocalTempArchive, myLocalTempArchive);
        myGeneralForm.addField(myAvailableOnLocalNet, myAvailableOnLocalNet);
        myGeneralForm.addField(myServerName, myServerName);
        Panel generalPanel = getComponentFactory().surroundWithPanel(myGeneralForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.general"));
        addComponent(generalPanel);

        myHttpForm = getComponentFactory().createForm(null, true);
        myHttpForm.addField(myPort, myPort);
        myHttpForm.addField(myUpnpUserHttp, myUpnpUserHttp);
        Panel httpPanel = getComponentFactory().surroundWithPanel(myHttpForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.http"));
        addComponent(httpPanel);

        myHttpsForm = getComponentFactory().createForm(null, true);
        myHttpsForm.addField(mySslPort, mySslPort);
        myHttpsForm.addField(myUpnpUserHttps, myUpnpUserHttps);
        myHttpsForm.addField(mySslKeystoreFile, mySslKeystoreFile);
        myHttpsForm.addField(mySslKeystoreFileSelect, mySslKeystoreFileSelect);
        myHttpsForm.addField(mySslKeystorePass, mySslKeystorePass);
        myHttpsForm.addField(mySslKeystoreKeyAlias, mySslKeystoreKeyAlias);
        Panel httpsPanel = getComponentFactory().surroundWithPanel(myHttpsForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.https"));
        addComponent(httpsPanel);

        myExtendedForm = getComponentFactory().createForm(null, true);
        myExtendedForm.addField(myWebappContext, myWebappContext);
        myExtendedForm.addField(myTomcatMaxThreads, myTomcatMaxThreads);
        myExtendedForm.addField(myTomcatAjpPort, myTomcatAjpPort);
        Panel extendedPanel = getComponentFactory().surroundWithPanel(myExtendedForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.extended"));
        addComponent(extendedPanel);

        addDefaultComponents(0, 5, 0, 5, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myAdminPort.setValue(MyTunesRss.CONFIG.getAdminPort(), 1, 65535, "");
        myAdminPassword.setValue(MyTunesRss.CONFIG.getAdminPasswordHash());
        myRetypeAdminPassword.setValue(MyTunesRss.CONFIG.getAdminPasswordHash());
        myLocalTempArchive.setValue(MyTunesRss.CONFIG.isLocalTempArchive());
        myAvailableOnLocalNet.setValue(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        myServerName.setValue(MyTunesRss.CONFIG.getServerName());
        myServerName.setEnabled(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        myWebappContext.setValue(MyTunesRss.CONFIG.getWebappContext());
        myTomcatMaxThreads.setValue(MyTunesRss.CONFIG.getTomcatMaxThreads());
        myTomcatAjpPort.setValue(MyTunesRss.CONFIG.getTomcatAjpPort(), 1, 65535, "");
        myPort.setValue(MyTunesRss.CONFIG.getPort(), 1, 65535, "");
        mySslPort.setValue(MyTunesRss.CONFIG.getSslPort(), 1, 65535, "");
        mySslKeystoreFile.setValue(MyTunesRss.CONFIG.getSslKeystoreFile());
        mySslKeystorePass.setValue(MyTunesRss.CONFIG.getSslKeystorePass());
        mySslKeystoreKeyAlias.setValue(MyTunesRss.CONFIG.getSslKeystoreKeyAlias());
        myUpnpAdmin.setValue(MyTunesRss.CONFIG.isUpnpAdmin());
        myUpnpUserHttp.setValue(MyTunesRss.CONFIG.isUpnpUserHttp());
        myUpnpUserHttps.setValue(MyTunesRss.CONFIG.isUpnpUserHttps());
    }

    protected void writeToConfig() {
        boolean adminServerConfigChanged = isAdminServerConfigChanged();
        boolean musicServerConfigChanged = isMusicServerConfigChanged();
        MyTunesRss.CONFIG.setAdminPort(myAdminPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setAdminPasswordHash(myAdminPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST));
        MyTunesRss.CONFIG.setLocalTempArchive(myLocalTempArchive.booleanValue());
        MyTunesRss.CONFIG.setAvailableOnLocalNet(myAvailableOnLocalNet.booleanValue());
        MyTunesRss.CONFIG.setServerName(myServerName.getStringValue(null));
        MyTunesRss.CONFIG.setWebappContext(myWebappContext.getStringValue(null));
        MyTunesRss.CONFIG.setTomcatMaxThreads(myTomcatMaxThreads.getStringValue(null));
        MyTunesRss.CONFIG.setTomcatAjpPort(myTomcatAjpPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setPort(myPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setSslPort(mySslPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setSslKeystoreFile(mySslKeystoreFile.getStringValue(null));
        MyTunesRss.CONFIG.setSslKeystorePass(mySslKeystorePass.getStringValue(null));
        MyTunesRss.CONFIG.setSslKeystoreKeyAlias(mySslKeystoreKeyAlias.getStringValue(null));
        if (adminServerConfigChanged) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.info.adminServerRestart");
            MyTunesRss.EXECUTOR_SERVICE.schedule(new Runnable() {
                public void run() {
                    if (MyTunesRss.stopAdminServer()) {
                        if (!MyTunesRss.startAdminServer(MyTunesRss.CONFIG.getAdminPort())) {
                            MyTunesRss.startAdminServer(0);
                        }
                    }
                }
            }, 2, TimeUnit.SECONDS);
        }
        if (musicServerConfigChanged) {
            if (!MyTunesRss.WEBSERVER.isRunning() || MyTunesRss.WEBSERVER.stop()) {
                if (MyTunesRss.WEBSERVER.start()) {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.info.serverRestarted");
                } else {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.error.serverStartFailed");
                }
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.error.serverStopFailed");
            }
        }
        MyTunesRss.CONFIG.setUpnpAdmin(myUpnpAdmin.booleanValue());
        MyTunesRss.CONFIG.setUpnpUserHttp(myUpnpUserHttp.booleanValue());
        MyTunesRss.CONFIG.setUpnpUserHttps(myUpnpUserHttps.booleanValue());
        MyTunesRss.CONFIG.save();
    }

    private boolean isAdminServerConfigChanged() {
        boolean changed = !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAdminPort(), myAdminPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAdminPasswordHash(), myAdminPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST));
        return changed;
    }

    private boolean isMusicServerConfigChanged() {
        boolean changed = !MyTunesRssUtils.equals(MyTunesRss.CONFIG.isAvailableOnLocalNet(), myAvailableOnLocalNet.booleanValue());
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getServerName()), myServerName.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getWebappContext()), myWebappContext.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getTomcatMaxThreads()), myTomcatMaxThreads.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getTomcatAjpPort(), myTomcatAjpPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getPort(), myPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getSslPort(), mySslPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystoreFile()), mySslKeystoreFile.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystorePass()), mySslKeystorePass.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystoreKeyAlias()), mySslKeystoreKeyAlias.getStringValue(null));
        return changed;
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myAdminForm, myGeneralForm, myExtendedForm, myHttpForm, myHttpsForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == mySslKeystoreFileSelect) {
            File dir = StringUtils.isNotBlank((String) mySslKeystoreFile.getValue()) ? new File((String) mySslKeystoreFile.getValue()) : null;
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("serverConfigPanel.caption.selectKeystoreFile"), dir, null, ServerSideFileChooser.PATTERN_ALL, false, "Roots") { // TODO i18n
                @Override
                protected void onFileSelected(File file) {
                    mySslKeystoreFile.setValue(file.getAbsolutePath());
                    getWindow().getParent().removeWindow(this);
                }
            }.show(getWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }
}
