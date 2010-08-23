/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssExecutorService;
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
    private CheckBox myStartAdminBrowser;
    private CheckBox myLocalTempArchive;
    private CheckBox myAvailableOnLocalNet;
    private SmartTextField myServerName;
    private SmartTextField myWebappContext;
    private SmartTextField myTomcatMaxThreads;
    private SmartTextField myTomcatAjpPort;
    private SmartTextField myPort;
    private Select myTomcatProxyScheme;
    private SmartTextField myTomcatProxyHost;
    private SmartTextField myTomcatProxyPort;
    private SmartTextField mySslPort;
    private Select myTomcatSslProxyScheme;
    private SmartTextField myTomcatSslProxyHost;
    private SmartTextField myTomcatSslProxyPort;
    private SmartTextField mySslKeystoreFile;
    private Button mySslKeystoreFileSelect;
    private SmartTextField mySslKeystorePass;
    private SmartTextField mySslKeystoreKeyAlias;

    public void attach() {
        init(getBundleString("serverConfigPanel.caption"), getComponentFactory().createGridLayout(1, 6, true, true));
        myAdminPort = getComponentFactory().createTextField("serverConfigPanel.adminPort", getApplication().getValidatorFactory().createPortValidator());
        myAdminPassword = getComponentFactory().createPasswordTextField("serverConfigPanel.adminPassword");
        myRetypeAdminPassword = getComponentFactory().createPasswordTextField("serverConfigPanel.retypeAdminPassword", new SameValidator(myAdminPassword, getBundleString("serverConfigPanel.error.retypeAdminPassword")));
        myStartAdminBrowser = getComponentFactory().createCheckBox("serverConfigPanel.startAdminBrowser");
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
        myTomcatProxyScheme = getComponentFactory().createSelect("serverConfigPanel.tomcatProxyScheme", Arrays.asList("HTTP", "HTTPS"));
        myTomcatProxyHost = getComponentFactory().createTextField("serverConfigPanel.tomcatProxyHost");
        myTomcatProxyPort = getComponentFactory().createTextField("serverConfigPanel.tomcatProxyPort", getApplication().getValidatorFactory().createPortValidator());
        mySslPort = getComponentFactory().createTextField("serverConfigPanel.sslPort", getApplication().getValidatorFactory().createPortValidator());
        myTomcatSslProxyScheme = getComponentFactory().createSelect("serverConfigPanel.tomcatSslProxyScheme", Arrays.asList("HTTPS", "HTTP"));
        myTomcatSslProxyHost = getComponentFactory().createTextField("serverConfigPanel.tomcatSslProxyHost");
        myTomcatSslProxyPort = getComponentFactory().createTextField("serverConfigPanel.tomcatSslProxyPort", getApplication().getValidatorFactory().createPortValidator());
        mySslKeystoreFile = getComponentFactory().createTextField("serverConfigPanel.sslKeystoreFile", new FileValidator(getBundleString("serverConfigPanel.error.invalidKeystore"), null, FileValidator.PATTERN_ALL));
        mySslKeystoreFileSelect = getComponentFactory().createButton("serverConfigPanel.sslKeystoreFile.select", this);
        mySslKeystorePass = getComponentFactory().createPasswordTextField("serverConfigPanel.sslKeystorePass");
        mySslKeystoreKeyAlias = getComponentFactory().createTextField("serverConfigPanel.sslKeystoreKeyAlias");

        myAdminForm = getComponentFactory().createForm(null, true);
        myAdminForm.addField(myAdminPort, myAdminPort);
        myAdminForm.addField(myAdminPassword, myAdminPassword);
        myAdminForm.addField(myRetypeAdminPassword, myRetypeAdminPassword);
        myAdminForm.addField(myStartAdminBrowser, myStartAdminBrowser);
        Panel adminPanel = getComponentFactory().surroundWithPanel(myAdminForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.admin"));
        addComponent(adminPanel);

        myGeneralForm = getComponentFactory().createForm(null, true);
        myGeneralForm.addField(myLocalTempArchive, myLocalTempArchive);
        myGeneralForm.addField(myAvailableOnLocalNet, myAvailableOnLocalNet);
        myGeneralForm.addField(myServerName, myServerName);
        Panel generalPanel = getComponentFactory().surroundWithPanel(myGeneralForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.general"));
        addComponent(generalPanel);

        myExtendedForm = getComponentFactory().createForm(null, true);
        myExtendedForm.addField(myWebappContext, myWebappContext);
        myExtendedForm.addField(myTomcatMaxThreads, myTomcatMaxThreads);
        myExtendedForm.addField(myTomcatAjpPort, myTomcatAjpPort);
        Panel extendedPanel = getComponentFactory().surroundWithPanel(myExtendedForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.extended"));
        addComponent(extendedPanel);

        myHttpForm = getComponentFactory().createForm(null, true);
        myHttpForm.addField(myPort, myPort);
        myHttpForm.addField(myTomcatProxyScheme, myTomcatProxyScheme);
        myHttpForm.addField(myTomcatProxyHost, myTomcatProxyHost);
        myHttpForm.addField(myTomcatProxyPort, myTomcatProxyPort);
        Panel httpPanel = getComponentFactory().surroundWithPanel(myHttpForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.http"));
        addComponent(httpPanel);

        myHttpsForm = getComponentFactory().createForm(null, true);
        myHttpsForm.addField(mySslPort, mySslPort);
        myHttpsForm.addField(myTomcatSslProxyScheme, myTomcatSslProxyScheme);
        myHttpsForm.addField(myTomcatSslProxyHost, myTomcatSslProxyHost);
        myHttpsForm.addField(myTomcatSslProxyPort, myTomcatSslProxyPort);
        myHttpsForm.addField(mySslKeystoreFile, mySslKeystoreFile);
        myHttpsForm.addField(mySslKeystoreFileSelect, mySslKeystoreFileSelect);
        myHttpsForm.addField(mySslKeystorePass, mySslKeystorePass);
        myHttpsForm.addField(mySslKeystoreKeyAlias, mySslKeystoreKeyAlias);
        Panel httpsPanel = getComponentFactory().surroundWithPanel(myHttpsForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.https"));
        addComponent(httpsPanel);

        attach(0, 5, 0, 5);

        initFromConfig();
    }

    protected void initFromConfig() {
        myAdminPort.setValue(MyTunesRss.CONFIG.getAdminPort(), 1, 65535, "");
        myAdminPassword.setValue(MyTunesRss.CONFIG.getAdminPasswordHash());
        myRetypeAdminPassword.setValue(MyTunesRss.CONFIG.getAdminPasswordHash());
        myStartAdminBrowser.setValue(MyTunesRss.CONFIG.isStartAdminBrowser());
        myLocalTempArchive.setValue(MyTunesRss.CONFIG.isLocalTempArchive());
        myAvailableOnLocalNet.setValue(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        myServerName.setValue(MyTunesRss.CONFIG.getServerName());
        myServerName.setEnabled(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        myWebappContext.setValue(MyTunesRss.CONFIG.getWebappContext());
        myTomcatMaxThreads.setValue(MyTunesRss.CONFIG.getTomcatMaxThreads());
        myTomcatAjpPort.setValue(MyTunesRss.CONFIG.getTomcatAjpPort(), 1, 65535, "");
        myPort.setValue(MyTunesRss.CONFIG.getPort(), 1, 65535, "");
        myTomcatProxyScheme.select(StringUtils.upperCase(MyTunesRss.CONFIG.getTomcatProxyScheme()));
        myTomcatProxyHost.setValue(MyTunesRss.CONFIG.getTomcatProxyHost());
        myTomcatProxyPort.setValue(MyTunesRss.CONFIG.getTomcatProxyPort(), 1, 65535, "");
        mySslPort.setValue(MyTunesRss.CONFIG.getSslPort(), 1, 65535, "");
        myTomcatSslProxyScheme.select(StringUtils.upperCase(MyTunesRss.CONFIG.getTomcatSslProxyScheme()));
        myTomcatSslProxyHost.setValue(MyTunesRss.CONFIG.getTomcatSslProxyHost());
        myTomcatSslProxyPort.setValue(MyTunesRss.CONFIG.getTomcatSslProxyPort(), 1, 65535, "");
        mySslKeystoreFile.setValue(MyTunesRss.CONFIG.getSslKeystoreFile());
        mySslKeystorePass.setValue(MyTunesRss.CONFIG.getSslKeystorePass());
        mySslKeystoreKeyAlias.setValue(MyTunesRss.CONFIG.getSslKeystoreKeyAlias());
    }

    protected void writeToConfig() {
        boolean adminServerConfigChanged = isAdminServerConfigChanged();
        boolean musicServerConfigChanged = isMusicServerConfigChanged();
        MyTunesRss.CONFIG.setAdminPort(myAdminPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setAdminPasswordHash(myAdminPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST));
        MyTunesRss.CONFIG.setStartAdminBrowser(myStartAdminBrowser.booleanValue());
        MyTunesRss.CONFIG.setLocalTempArchive(myLocalTempArchive.booleanValue());
        MyTunesRss.CONFIG.setAvailableOnLocalNet(myAvailableOnLocalNet.booleanValue());
        MyTunesRss.CONFIG.setServerName(myServerName.getStringValue(null));
        MyTunesRss.CONFIG.setWebappContext(myWebappContext.getStringValue(null));
        MyTunesRss.CONFIG.setTomcatMaxThreads(myTomcatMaxThreads.getStringValue(null));
        MyTunesRss.CONFIG.setTomcatAjpPort(myTomcatAjpPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setPort(myPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setTomcatProxyScheme(myTomcatProxyScheme.toString());
        MyTunesRss.CONFIG.setTomcatProxyHost(myTomcatProxyHost.getStringValue(null));
        MyTunesRss.CONFIG.setTomcatProxyPort(myTomcatProxyPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setSslPort(mySslPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setTomcatSslProxyScheme(myTomcatSslProxyScheme.toString());
        MyTunesRss.CONFIG.setTomcatSslProxyHost(myTomcatSslProxyHost.getStringValue(null));
        MyTunesRss.CONFIG.setTomcatSslProxyPort(myTomcatSslProxyPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setSslKeystoreFile(mySslKeystoreFile.getStringValue(null));
        MyTunesRss.CONFIG.setSslKeystorePass(mySslKeystorePass.getStringValue(null));
        MyTunesRss.CONFIG.setSslKeystoreKeyAlias(mySslKeystoreKeyAlias.getStringValue(null));
        if (adminServerConfigChanged) {
            getApplication().showInfo("serverConfigPanel.info.adminServerRestart");
            MyTunesRss.EXECUTOR_SERVICE.schedule(new Runnable() {
                public void run() {
                    if (MyTunesRss.stopAdminServer()) {
                        MyTunesRss.startAdminServer();
                    }
                }
            }, 2, TimeUnit.SECONDS);
        }
        if (musicServerConfigChanged) {
            if (!MyTunesRss.WEBSERVER.isRunning() || MyTunesRss.WEBSERVER.stop()) {
                if (MyTunesRss.WEBSERVER.start()) {
                    getApplication().showInfo("serverConfigPanel.info.serverRestarted");
                } else {
                    getApplication().showInfo("serverConfigPanel.error.serverStartFailed");
                }
            } else {
                getApplication().showInfo("serverConfigPanel.error.serverStopFailed");
            }
        }
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
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getTomcatProxyScheme(), myTomcatProxyScheme.toString());
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getTomcatProxyHost()), myTomcatProxyHost.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getTomcatProxyPort(), myTomcatProxyPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getSslPort(), mySslPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getTomcatSslProxyScheme(), myTomcatSslProxyScheme.toString());
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getTomcatSslProxyHost()), myTomcatSslProxyHost.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getTomcatSslProxyPort(), myTomcatSslProxyPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystoreFile()), mySslKeystoreFile.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystorePass()), mySslKeystorePass.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystoreKeyAlias()), mySslKeystoreKeyAlias.getStringValue(null));
        return changed;
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myAdminForm, myGeneralForm, myExtendedForm, myHttpForm, myHttpsForm);
        if (!valid) {
            getApplication().showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == mySslKeystoreFileSelect) {
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("serverConfigPanel.caption.selectKeystoreFile"), new File((String) mySslKeystoreFile.getValue()), null, ServerSideFileChooser.PATTERN_ALL, false, "Roots") { // TODO i18n
                @Override
                protected void onFileSelected(File file) {
                    mySslKeystoreFile.setValue(file.getAbsolutePath());
                    getApplication().getMainWindow().removeWindow(this);
                }
            }.show(getApplication().getMainWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }
}
