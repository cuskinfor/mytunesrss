/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.FileValidator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Arrays;

public class ServerConfigPanel extends MyTunesRssConfigPanel {

    private Form myGeneralForm;
    private Form myExtendedForm;
    private Form myHttpForm;
    private Form myHttpsForm;
    private CheckBox myAutoStartServer;
    private CheckBox myLocalTempArchive;
    private CheckBox myAvailableOnLocalNet;
    private TextField myServerName;
    private TextField myWebappContext;
    private TextField myTomcatMaxThreads;
    private TextField myTomcatAjpPort;
    private TextField myPort;
    private Select myTomcatProxyScheme;
    private TextField myTomcatProxyHost;
    private TextField myTomcatProxyPort;
    private TextField mySslPort;
    private Select myTomcatSslProxyScheme;
    private TextField myTomcatSslProxyHost;
    private TextField myTomcatSslProxyPort;
    private TextField mySslKeystoreFile;
    private Button mySslKeystoreFileSelect;
    private TextField mySslKeystorePass;
    private TextField mySslKeystoreKeyAlias;

    public ServerConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("serverConfigPanel.caption"), componentFactory.createGridLayout(2, 3, true, true), componentFactory);
    }

    protected void init(Application application) {
        myAutoStartServer = getComponentFactory().createCheckBox("serverConfigPanel.autoStartServer");
        myLocalTempArchive = getComponentFactory().createCheckBox("serverConfigPanel.localTempArchive");
        myAvailableOnLocalNet = getComponentFactory().createCheckBox("serverConfigPanel.availableOnLocalNet");
        myAvailableOnLocalNet.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                myServerName.setEnabled(myAvailableOnLocalNet.booleanValue());
            }
        });
        myServerName = getComponentFactory().createTextField("serverConfigPanel.serverName");
        myWebappContext = getComponentFactory().createTextField("serverConfigPanel.webappContext");
        myTomcatMaxThreads = getComponentFactory().createTextField("serverConfigPanel.tomcatMaxThreads", ValidatorFactory.createMinMaxValidator(5, 1000));
        MyTunesRssWebAdminUtils.setRequired(myTomcatMaxThreads);
        myTomcatAjpPort = getComponentFactory().createTextField("serverConfigPanel.tomcatAjpPort", ValidatorFactory.createPortValidator());
        myPort = getComponentFactory().createTextField("serverConfigPanel.port", ValidatorFactory.createPortValidator());
        MyTunesRssWebAdminUtils.setRequired(myPort);
        myTomcatProxyScheme = getComponentFactory().createSelect("serverConfigPanel.tomcatProxyScheme", Arrays.asList("HTTP", "HTTPS"));
        myTomcatProxyHost = getComponentFactory().createTextField("serverConfigPanel.tomcatProxyHost");
        myTomcatProxyPort = getComponentFactory().createTextField("serverConfigPanel.tomcatProxyPort", ValidatorFactory.createPortValidator());
        mySslPort = getComponentFactory().createTextField("serverConfigPanel.sslPort", ValidatorFactory.createPortValidator());
        myTomcatSslProxyScheme = getComponentFactory().createSelect("serverConfigPanel.tomcatSslProxyScheme", Arrays.asList("HTTPS", "HTTP"));
        myTomcatSslProxyHost = getComponentFactory().createTextField("serverConfigPanel.tomcatSslProxyHost");
        myTomcatSslProxyPort = getComponentFactory().createTextField("serverConfigPanel.tomcatSslProxyPort", ValidatorFactory.createPortValidator());
        mySslKeystoreFile = getComponentFactory().createTextField("serverConfigPanel.sslKeystoreFile", new FileValidator(getBundleString("serverConfigPanel.error.invalidKeystore"), null, FileValidator.PATTERN_ALL));
        mySslKeystoreFileSelect = getComponentFactory().createButton("serverConfigPanel.sslKeystoreFile.select", this);
        mySslKeystorePass = getComponentFactory().createPasswordTextField("serverConfigPanel.sslKeystorePass");
        mySslKeystoreKeyAlias = getComponentFactory().createTextField("serverConfigPanel.sslKeystoreKeyAlias");

        myGeneralForm = getComponentFactory().createForm(null, true);
        myGeneralForm.addField(myAutoStartServer, myAutoStartServer);
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

        addMainButtons(0, 2, 1, 2);
    }

    protected void initFromConfig(Application application) {
        myAutoStartServer.setValue(MyTunesRss.CONFIG.isAutoStartServer());
        myLocalTempArchive.setValue(MyTunesRss.CONFIG.isLocalTempArchive());
        myAvailableOnLocalNet.setValue(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        myServerName.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getServerName()));
        myServerName.setEnabled(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        myWebappContext.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebappContext()));
        myTomcatMaxThreads.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getTomcatMaxThreads()));
        myTomcatAjpPort.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getTomcatAjpPort(), 1, 65535, ""));
        myPort.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getPort(), 1, 65535, ""));
        myTomcatProxyScheme.select(StringUtils.upperCase(MyTunesRss.CONFIG.getTomcatProxyScheme()));
        myTomcatProxyHost.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getTomcatProxyHost()));
        myTomcatProxyPort.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getTomcatProxyPort(), 1, 65535, ""));
        mySslPort.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getSslPort(), 1, 65535, ""));
        myTomcatSslProxyScheme.select(StringUtils.upperCase(MyTunesRss.CONFIG.getTomcatSslProxyScheme()));
        myTomcatSslProxyHost.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getTomcatSslProxyHost()));
        myTomcatSslProxyPort.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getTomcatSslProxyPort(), 1, 65535, ""));
        mySslKeystoreFile.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getSslKeystoreFile()));
        mySslKeystorePass.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getSslKeystorePass()));
        mySslKeystoreKeyAlias.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getSslKeystoreKeyAlias()));
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setAutoStartServer(myAutoStartServer.booleanValue());
        MyTunesRss.CONFIG.setLocalTempArchive(myLocalTempArchive.booleanValue());
        MyTunesRss.CONFIG.setAvailableOnLocalNet(myAvailableOnLocalNet.booleanValue());
        MyTunesRss.CONFIG.setServerName(StringUtils.trimToNull(myServerName.toString()));
        MyTunesRss.CONFIG.setWebappContext(StringUtils.trimToNull(myWebappContext.toString()));
        MyTunesRss.CONFIG.setTomcatMaxThreads(StringUtils.trimToNull(myTomcatMaxThreads.toString()));
        MyTunesRss.CONFIG.setTomcatAjpPort(MyTunesRssUtils.getStringInteger(myTomcatAjpPort.toString(), 0));
        MyTunesRss.CONFIG.setPort(MyTunesRssUtils.getStringInteger(myPort.toString(), 0));
        MyTunesRss.CONFIG.setTomcatProxyScheme(myTomcatProxyScheme.toString());
        MyTunesRss.CONFIG.setTomcatProxyHost(StringUtils.trimToNull(myTomcatProxyHost.toString()));
        MyTunesRss.CONFIG.setTomcatProxyPort(MyTunesRssUtils.getStringInteger(myTomcatProxyPort.toString(), 0));
        MyTunesRss.CONFIG.setSslPort(MyTunesRssUtils.getStringInteger(mySslPort.toString(), 0));
        MyTunesRss.CONFIG.setTomcatSslProxyScheme(myTomcatSslProxyScheme.toString());
        MyTunesRss.CONFIG.setTomcatSslProxyHost(StringUtils.trimToNull(myTomcatSslProxyHost.toString()));
        MyTunesRss.CONFIG.setTomcatSslProxyPort(MyTunesRssUtils.getStringInteger(myTomcatSslProxyPort.toString(), 0));
        MyTunesRss.CONFIG.setSslKeystoreFile(StringUtils.trimToNull(mySslKeystoreFile.toString()));
        MyTunesRss.CONFIG.setSslKeystorePass(StringUtils.trimToNull(mySslKeystorePass.toString()));
        MyTunesRss.CONFIG.setSslKeystoreKeyAlias(StringUtils.trimToNull(mySslKeystoreKeyAlias.toString()));
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myGeneralForm, myExtendedForm, myHttpForm, myHttpsForm);
        if (!valid) {
            getApplication().showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == mySslKeystoreFileSelect) {
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("serverConfigPanel.caption.selectKeystoreFile"), new File((String) mySslKeystoreFile.getValue()), null, ServerSideFileChooser.PATTERN_ALL, false) {
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
