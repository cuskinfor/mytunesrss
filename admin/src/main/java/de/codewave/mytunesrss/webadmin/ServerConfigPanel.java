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
import de.codewave.vaadin.SmartPasswordField;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.FileValidator;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;
import de.codewave.vaadin.validation.SameValidator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerConfigPanel extends MyTunesRssConfigPanel {

    private static class ListenAddress {
        private String myValue;
        private String myDisplayName;

        private ListenAddress(String value, String displayName) {
            myValue = value;
            myDisplayName = displayName;
        }

        @Override
        public int hashCode() {
            return myValue != null ? myValue.hashCode() : 0;
        }

        @Override
        public boolean equals(Object other) {
            return other != null && other.getClass().equals(getClass()) && StringUtils.equals(myValue, ((ListenAddress) other).myValue);
        }

        @Override
        public String toString() {
            return myDisplayName;
        }

        public String getValue() {
            return myValue;
        }
    }

    private Form myAdminForm;
    private Form myGeneralForm;
    private Form myExtendedForm;
    private Form myHttpForm;
    private Form myHttpsForm;
    private Form myAccessLogForm;
    private Select myAdminListenAddress;
    private SmartTextField myAdminPort;
    private SmartPasswordField myAdminPassword;
    private SmartPasswordField myRetypeAdminPassword;
    private CheckBox myLocalTempArchive;
    private CheckBox myAvailableOnLocalNet;
    private SmartTextField myServerName;
    private SmartTextField myWebappContext;
    private SmartTextField myTomcatMaxThreads;
    private Select myTomcatAjpListenAddress;
    private SmartTextField myTomcatAjpPort;
    private Select myListenAddress;
    private SmartTextField myPort;
    private Select mySslListenAddress;
    private SmartTextField mySslPort;
    private SmartTextField mySslKeystoreFile;
    private Button mySslKeystoreFileSelect;
    private SmartPasswordField mySslKeystorePass;
    private SmartTextField mySslKeystoreKeyAlias;
    private CheckBox myUpnpAdmin;
    private CheckBox myUpnpUserHttp;
    private CheckBox myUpnpUserHttps;
    private SmartTextField myUserAccessLogRetainDays;
    private SmartTextField myAdminAccessLogRetainDays;
    private CheckBox myUserAccessLogExtended; 
    private CheckBox myAdminAccessLogExtended;
    private Select myAccessLogTz;

    public void attach() {
        super.attach();
        init(getBundleString("serverConfigPanel.caption"), getComponentFactory().createGridLayout(1, 7, true, true));
        myAdminListenAddress = getComponentFactory().createSelect("serverConfigPanel.adminListenAddress", getListenAddresses());
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
        myTomcatAjpListenAddress = getComponentFactory().createSelect("serverConfigPanel.tomcatAjpListenAddress", getListenAddresses());
        myTomcatAjpPort = getComponentFactory().createTextField("serverConfigPanel.tomcatAjpPort", getApplication().getValidatorFactory().createPortValidator());
        myListenAddress = getComponentFactory().createSelect("serverConfigPanel.listenAddress", getListenAddresses());
        myPort = getComponentFactory().createTextField("serverConfigPanel.port", getApplication().getValidatorFactory().createPortValidator());
        setRequired(myPort);
        mySslListenAddress = getComponentFactory().createSelect("serverConfigPanel.sslListenAddress", getListenAddresses());
        mySslPort = getComponentFactory().createTextField("serverConfigPanel.sslPort", getApplication().getValidatorFactory().createPortValidator());
        mySslKeystoreFile = getComponentFactory().createTextField("serverConfigPanel.sslKeystoreFile", new FileValidator(getBundleString("serverConfigPanel.error.invalidKeystore"), null, FileValidator.PATTERN_ALL));
        mySslKeystoreFileSelect = getComponentFactory().createButton("serverConfigPanel.sslKeystoreFile.select", this);
        mySslKeystorePass = getComponentFactory().createPasswordTextField("serverConfigPanel.sslKeystorePass");
        mySslKeystoreKeyAlias = getComponentFactory().createTextField("serverConfigPanel.sslKeystoreKeyAlias");
        myUpnpAdmin = getComponentFactory().createCheckBox("serverConfigPanel.upnp");
        myUpnpUserHttp = getComponentFactory().createCheckBox("serverConfigPanel.upnp");
        myUpnpUserHttps = getComponentFactory().createCheckBox("serverConfigPanel.upnp");
        myUserAccessLogRetainDays = getComponentFactory().createTextField("serverConfigPanel.accesslog.user.retain", getApplication().getValidatorFactory().createMinMaxValidator(1, 90));
        myAdminAccessLogRetainDays = getComponentFactory().createTextField("serverConfigPanel.accesslog.admin.retain", getApplication().getValidatorFactory().createMinMaxValidator(1, 90));
        myUserAccessLogExtended = getComponentFactory().createCheckBox("serverConfigPanel.accesslog.user.ext");
        myAdminAccessLogExtended = getComponentFactory().createCheckBox("serverConfigPanel.accesslog.admin.retain");
        List<String> timezones = new ArrayList<String>();
        for (int i = 12; i > 0; i--) {
            timezones.add("GMT-" + StringUtils.leftPad("" + i, 2, '0'));
        }
        timezones.add("GMT");
        for (int i = 1; i < 13; i++) {
            timezones.add("GMT+" + StringUtils.leftPad("" + i, 2, '0'));
        }
        myAccessLogTz = getComponentFactory().createSelect("serverConfigPanel.accesslog.tz", timezones);

        myAdminForm = getComponentFactory().createForm(null, true);
        myAdminForm.addField(myAdminListenAddress, myAdminListenAddress);
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
        myHttpForm.addField(myListenAddress, myListenAddress);
        myHttpForm.addField(myPort, myPort);
        myHttpForm.addField(myUpnpUserHttp, myUpnpUserHttp);
        Panel httpPanel = getComponentFactory().surroundWithPanel(myHttpForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.http"));
        addComponent(httpPanel);

        myHttpsForm = getComponentFactory().createForm(null, true);
        myHttpsForm.addField(mySslListenAddress, mySslListenAddress);
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
        myExtendedForm.addField(myTomcatAjpListenAddress, myTomcatAjpListenAddress);
        myExtendedForm.addField(myTomcatAjpPort, myTomcatAjpPort);
        Panel extendedPanel = getComponentFactory().surroundWithPanel(myExtendedForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.extended"));
        addComponent(extendedPanel);

        myAccessLogForm = getComponentFactory().createForm(null, true);
        myAccessLogForm.addField(myUserAccessLogRetainDays, myUserAccessLogRetainDays);
        myAccessLogForm.addField(myUserAccessLogExtended, myUserAccessLogExtended);
        myAccessLogForm.addField(myAdminAccessLogRetainDays, myAdminAccessLogRetainDays);
        myAccessLogForm.addField(myAdminAccessLogExtended, myAdminAccessLogExtended);
        myAccessLogForm.addField(myAccessLogTz, myAccessLogTz);
        Panel accessLogPanel = getComponentFactory().surroundWithPanel(myAccessLogForm, FORM_PANEL_MARGIN_INFO, getBundleString("serverConfigPanel.caption.accesslog"));
        addComponent(accessLogPanel);

        addDefaultComponents(0, 6, 0, 6, false);

        initFromConfig();
    }

    private Collection<ListenAddress> getListenAddresses() {
        List<ListenAddress> addresses = new ArrayList<ListenAddress>();
        addresses.add(new ListenAddress(null, getBundleString("serverConfigPanel.listenAddress.all")));
        for (String address : MyTunesRssUtils.getAvailableListenAddresses()) {
            addresses.add(new ListenAddress(address, address));
        }
        return addresses;
    }

    protected void initFromConfig() {
        myAdminListenAddress.setValue(new ListenAddress(MyTunesRss.CONFIG.getAdminHost(), null));
        myAdminPort.setValue(MyTunesRss.CONFIG.getAdminPort(), 1, 65535, "");
        myAdminPassword.setValue(MyTunesRss.CONFIG.getAdminPasswordHash());
        myRetypeAdminPassword.setValue(MyTunesRss.CONFIG.getAdminPasswordHash());
        myLocalTempArchive.setValue(MyTunesRss.CONFIG.isLocalTempArchive());
        myAvailableOnLocalNet.setValue(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        myServerName.setValue(MyTunesRss.CONFIG.getServerName());
        myServerName.setEnabled(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        myWebappContext.setValue(MyTunesRss.CONFIG.getWebappContext());
        myTomcatMaxThreads.setValue(MyTunesRss.CONFIG.getTomcatMaxThreads());
        myTomcatAjpListenAddress.setValue(new ListenAddress(MyTunesRss.CONFIG.getAjpHost(), null));
        myTomcatAjpPort.setValue(MyTunesRss.CONFIG.getTomcatAjpPort(), 1, 65535, "");
        myListenAddress.setValue(new ListenAddress(MyTunesRss.CONFIG.getHost(), null));
        myPort.setValue(MyTunesRss.CONFIG.getPort(), 1, 65535, "");
        mySslListenAddress.setValue(new ListenAddress(MyTunesRss.CONFIG.getSslHost(), null));
        mySslPort.setValue(MyTunesRss.CONFIG.getSslPort(), 1, 65535, "");
        mySslKeystoreFile.setValue(MyTunesRss.CONFIG.getSslKeystoreFile());
        mySslKeystorePass.setValue(MyTunesRss.CONFIG.getSslKeystorePass());
        mySslKeystoreKeyAlias.setValue(MyTunesRss.CONFIG.getSslKeystoreKeyAlias());
        myUpnpAdmin.setValue(MyTunesRss.CONFIG.isUpnpAdmin());
        myUpnpUserHttp.setValue(MyTunesRss.CONFIG.isUpnpUserHttp());
        myUpnpUserHttps.setValue(MyTunesRss.CONFIG.isUpnpUserHttps());
        myUserAccessLogRetainDays.setValue(MyTunesRss.CONFIG.getUserAccessLogRetainDays());
        myAdminAccessLogRetainDays.setValue(MyTunesRss.CONFIG.getAdminAccessLogRetainDays());
        myUserAccessLogExtended.setValue(MyTunesRss.CONFIG.isUserAccessLogExtended());
        myAdminAccessLogExtended.setValue(MyTunesRss.CONFIG.isAdminAccessLogExtended());
        myAccessLogTz.setValue(MyTunesRss.CONFIG.getAccessLogTz());
    }

    protected void writeToConfig() {
        boolean adminServerConfigChanged = isAdminServerConfigChanged();
        boolean musicServerConfigChanged = isMusicServerConfigChanged();
        MyTunesRss.CONFIG.setAdminHost(((ListenAddress)myAdminListenAddress.getValue()).getValue());
        MyTunesRss.CONFIG.setAdminPort(myAdminPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setAdminPasswordHash(myAdminPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST.get()));
        MyTunesRss.CONFIG.setLocalTempArchive(myLocalTempArchive.booleanValue());
        MyTunesRss.CONFIG.setAvailableOnLocalNet(myAvailableOnLocalNet.booleanValue());
        MyTunesRss.CONFIG.setServerName(myServerName.getStringValue(null));
        MyTunesRss.CONFIG.setWebappContext(myWebappContext.getStringValue(null));
        MyTunesRss.CONFIG.setTomcatMaxThreads(myTomcatMaxThreads.getStringValue(null));
        MyTunesRss.CONFIG.setAjpHost(((ListenAddress) myTomcatAjpListenAddress.getValue()).getValue());
        MyTunesRss.CONFIG.setTomcatAjpPort(myTomcatAjpPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setHost(((ListenAddress) myListenAddress.getValue()).getValue());
        MyTunesRss.CONFIG.setPort(myPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setSslHost(((ListenAddress) mySslListenAddress.getValue()).getValue());
        MyTunesRss.CONFIG.setSslPort(mySslPort.getIntegerValue(0));
        MyTunesRss.CONFIG.setSslKeystoreFile(mySslKeystoreFile.getStringValue(null));
        MyTunesRss.CONFIG.setSslKeystorePass(mySslKeystorePass.getStringValue(null));
        MyTunesRss.CONFIG.setSslKeystoreKeyAlias(mySslKeystoreKeyAlias.getStringValue(null));
        MyTunesRss.CONFIG.setUserAccessLogRetainDays(myUserAccessLogRetainDays.getIntegerValue(1));
        MyTunesRss.CONFIG.setAdminAccessLogRetainDays(myAdminAccessLogRetainDays.getIntegerValue(1));
        MyTunesRss.CONFIG.setUserAccessLogExtended(myUserAccessLogExtended.booleanValue());
        MyTunesRss.CONFIG.setAdminAccessLogExtended(myAdminAccessLogExtended.booleanValue());
        MyTunesRss.CONFIG.setAccessLogTz(myAccessLogTz.getValue().toString());
        if (adminServerConfigChanged) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.info.adminServerRestart");
            MyTunesRss.EXECUTOR_SERVICE.schedule(new Runnable() {
                public void run() {
                    if (MyTunesRss.stopAdminServer()) {
                        if (!MyTunesRss.startAdminServer(MyTunesRss.CONFIG.getAdminHost(), MyTunesRss.CONFIG.getAdminPort())) {
                            MyTunesRss.startAdminServer(null, 0);
                        }
                    }
                }
            }, 2, TimeUnit.SECONDS);
        }
        MyTunesRss.CONFIG.setUpnpAdmin(myUpnpAdmin.booleanValue());
        MyTunesRss.CONFIG.setUpnpUserHttp(myUpnpUserHttp.booleanValue());
        MyTunesRss.CONFIG.setUpnpUserHttps(myUpnpUserHttps.booleanValue());
        MyTunesRss.CONFIG.save();
        if (musicServerConfigChanged) {
            MyTunesRss.stopWebserver();
            if (!MyTunesRss.WEBSERVER.isRunning()) {
                Exception e = MyTunesRss.startWebserver();
                if (MyTunesRss.WEBSERVER.isRunning()) {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.info.serverRestarted");
                } else {
                    if (e != null) {
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.error.serverStartFailedWithException", e.getMessage());
                    } else {
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.error.serverStartFailed");
                    }
                }
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("serverConfigPanel.error.serverStopFailed");
            }
        }
    }

    private boolean isAdminServerConfigChanged() {
        boolean changed = !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAdminPort(), myAdminPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAdminPasswordHash(), myAdminPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST.get()));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAdminHost(), ((ListenAddress)myAdminListenAddress.getValue()).getValue());
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAdminAccessLogRetainDays(), myAdminAccessLogRetainDays.getIntegerValue(1));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.isAdminAccessLogExtended(), myAdminAccessLogExtended.booleanValue());
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAccessLogTz(), myAccessLogTz.getValue());
        return changed;
    }

    private boolean isMusicServerConfigChanged() {
        boolean changed = !MyTunesRssUtils.equals(MyTunesRss.CONFIG.isAvailableOnLocalNet(), myAvailableOnLocalNet.booleanValue());
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getServerName()), myServerName.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getWebappContext()), myWebappContext.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getTomcatMaxThreads()), myTomcatMaxThreads.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAjpHost(), ((ListenAddress)myTomcatAjpListenAddress.getValue()).getValue());
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getTomcatAjpPort(), myTomcatAjpPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getHost(), ((ListenAddress)myListenAddress.getValue()).getValue());
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getPort(), myPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getSslHost(), ((ListenAddress)mySslListenAddress.getValue()).getValue());
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getSslPort(), mySslPort.getIntegerValue(0));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystoreFile()), mySslKeystoreFile.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystorePass()), mySslKeystorePass.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(StringUtils.trimToNull(MyTunesRss.CONFIG.getSslKeystoreKeyAlias()), mySslKeystoreKeyAlias.getStringValue(null));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getUserAccessLogRetainDays(), myUserAccessLogRetainDays.getIntegerValue(1));
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.isUserAccessLogExtended(), myUserAccessLogExtended.booleanValue());
        changed |= !MyTunesRssUtils.equals(MyTunesRss.CONFIG.getAccessLogTz(), myAccessLogTz.getValue());
        return changed;
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myAdminForm, myGeneralForm, myExtendedForm, myHttpForm, myHttpsForm, myAccessLogForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == mySslKeystoreFileSelect) {
            File dir = StringUtils.isNotBlank((String) mySslKeystoreFile.getValue()) ? new File((String) mySslKeystoreFile.getValue()) : null;
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("serverConfigPanel.caption.selectKeystoreFile"), dir, null, ServerSideFileChooser.PATTERN_ALL, false, getApplication().getServerSideFileChooserLabels()) {
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
