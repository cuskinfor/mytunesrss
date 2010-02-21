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
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.validation.FileValidator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Arrays;

public class ServerConfigPanel extends Panel implements Button.ClickListener {

    private ComponentFactory myComponentFactory;
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
    private Button mySave;
    private Button myReset;
    private Button myCancel;

    public ServerConfigPanel(ComponentFactory componentFactory) {
        super(MyTunesRssWebAdminUtils.getBundleString("serverConfigPanel.caption"), componentFactory.createGridLayout(2, 3, true, true));
        myComponentFactory = componentFactory;
        init();
        initFromConfig();
    }

    private void init() {
        myAutoStartServer = myComponentFactory.createCheckBox("serverConfigPanel.autoStartServer");
        myLocalTempArchive = myComponentFactory.createCheckBox("serverConfigPanel.localTempArchive");
        myAvailableOnLocalNet = myComponentFactory.createCheckBox("serverConfigPanel.availableOnLocalNet");
        myAvailableOnLocalNet.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                myServerName.setEnabled(myAvailableOnLocalNet.booleanValue());
            }
        });
        myServerName = myComponentFactory.createTextField("serverConfigPanel.serverName");
        myWebappContext = myComponentFactory.createTextField("serverConfigPanel.webappContext");
        myTomcatMaxThreads = myComponentFactory.createTextField("serverConfigPanel.tomcatMaxThreads", ValidatorFactory.createMinMaxValidator(5, 1000));
        MyTunesRssWebAdminUtils.setRequired(myTomcatMaxThreads);
        myTomcatAjpPort = myComponentFactory.createTextField("serverConfigPanel.tomcatAjpPort", ValidatorFactory.createPortValidator());
        myPort = myComponentFactory.createTextField("serverConfigPanel.port", ValidatorFactory.createPortValidator());
        MyTunesRssWebAdminUtils.setRequired(myPort);
        myTomcatProxyScheme = myComponentFactory.createSelect("serverConfigPanel.tomcatProxyScheme", Arrays.asList("HTTP", "HTTPS"));
        myTomcatProxyHost = myComponentFactory.createTextField("serverConfigPanel.tomcatProxyHost");
        myTomcatProxyPort = myComponentFactory.createTextField("serverConfigPanel.tomcatProxyPort", ValidatorFactory.createPortValidator());
        mySslPort = myComponentFactory.createTextField("serverConfigPanel.sslPort", ValidatorFactory.createPortValidator());
        myTomcatSslProxyScheme = myComponentFactory.createSelect("serverConfigPanel.tomcatSslProxyScheme", Arrays.asList("HTTPS", "HTTP"));
        myTomcatSslProxyHost = myComponentFactory.createTextField("serverConfigPanel.tomcatSslProxyHost");
        myTomcatSslProxyPort = myComponentFactory.createTextField("serverConfigPanel.tomcatSslProxyPort", ValidatorFactory.createPortValidator());
        mySslKeystoreFile = myComponentFactory.createTextField("serverConfigPanel.sslKeystoreFile", new FileValidator(MyTunesRssWebAdminUtils.getBundleString("serverConfigPanel.error.invalidKeystore"), true, false, null));
        mySslKeystoreFileSelect = myComponentFactory.createButton("serverConfigPanel.sslKeystoreFile.select", this);
        mySslKeystorePass = myComponentFactory.createPasswordTextField("serverConfigPanel.sslKeystorePass");
        mySslKeystoreKeyAlias = myComponentFactory.createTextField("serverConfigPanel.sslKeystoreKeyAlias");
        mySave = myComponentFactory.createButton("save", this);
        myReset = myComponentFactory.createButton("reset", this);
        myCancel = myComponentFactory.createButton("cancel", this);

        myGeneralForm = myComponentFactory.createForm(MyTunesRssWebAdminUtils.getBundleString("serverConfigPanel.caption.general"), true);
        myGeneralForm.addField(myAutoStartServer, myAutoStartServer);
        myGeneralForm.addField(myLocalTempArchive, myLocalTempArchive);
        myGeneralForm.addField(myAvailableOnLocalNet, myAvailableOnLocalNet);
        myGeneralForm.addField(myServerName, myServerName);
        Panel generalPanel = myComponentFactory.surroundWithPanel(myGeneralForm, true, null);
        addComponent(generalPanel);

        myExtendedForm = myComponentFactory.createForm(MyTunesRssWebAdminUtils.getBundleString("serverConfigPanel.caption.extended"), true);
        myExtendedForm.addField(myWebappContext, myWebappContext);
        myExtendedForm.addField(myTomcatMaxThreads, myTomcatMaxThreads);
        myExtendedForm.addField(myTomcatAjpPort, myTomcatAjpPort);
        Panel extendedPanel = myComponentFactory.surroundWithPanel(myExtendedForm, true, null);
        addComponent(extendedPanel);

        myHttpForm = myComponentFactory.createForm(MyTunesRssWebAdminUtils.getBundleString("serverConfigPanel.caption.http"), true);
        myHttpForm.addField(myPort, myPort);
        myHttpForm.addField(myTomcatProxyScheme, myTomcatProxyScheme);
        myHttpForm.addField(myTomcatProxyHost, myTomcatProxyHost);
        myHttpForm.addField(myTomcatProxyPort, myTomcatProxyPort);
        Panel httpPanel = myComponentFactory.surroundWithPanel(myHttpForm, true, null);
        addComponent(httpPanel);

        myHttpsForm = myComponentFactory.createForm(MyTunesRssWebAdminUtils.getBundleString("serverConfigPanel.caption.https"), true);
        myHttpsForm.addField(mySslPort, mySslPort);
        myHttpsForm.addField(myTomcatSslProxyScheme, myTomcatSslProxyScheme);
        myHttpsForm.addField(myTomcatSslProxyHost, myTomcatSslProxyHost);
        myHttpsForm.addField(myTomcatSslProxyPort, myTomcatSslProxyPort);
        myHttpsForm.addField(mySslKeystoreFile, mySslKeystoreFile);
        myHttpsForm.addField(mySslKeystoreFileSelect, mySslKeystoreFileSelect);
        myHttpsForm.addField(mySslKeystorePass, mySslKeystorePass);
        myHttpsForm.addField(mySslKeystoreKeyAlias, mySslKeystoreKeyAlias);
        Panel httpsPanel = myComponentFactory.surroundWithPanel(myHttpsForm, true, null);
        addComponent(httpsPanel);

        Panel mainButtons = new Panel();
        mainButtons.addStyleName("light");
        mainButtons.setContent(myComponentFactory.createHorizontalLayout(false, true));
        ((GridLayout) getContent()).addComponent(mainButtons, 0, 2, 1, 2);
        mainButtons.addComponent(mySave);
        mainButtons.addComponent(myReset);
        mainButtons.addComponent(myCancel);
    }

    private void initFromConfig() {
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

    private void writeToConfig() {
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

    public void buttonClick(Button.ClickEvent clickEvent) {
        MyTunesRssWebAdmin application = ((MyTunesRssWebAdmin) getApplication());
        if (clickEvent.getButton() == mySave) {
            if (!VaadinUtils.isValid(myGeneralForm, myExtendedForm, myHttpForm, myHttpsForm)) {
                application.showError("error.formInvalid");
                return;
            }
            writeToConfig();
            application.setMainComponent(new StatusPanel(myComponentFactory));
        } else if (clickEvent.getButton() == myReset) {
            initFromConfig();
        } else if (clickEvent.getButton() == myCancel) {
            application.setMainComponent(new StatusPanel(myComponentFactory));
        } else if (clickEvent.getButton() == mySslKeystoreFileSelect) {
            Window window = new Window("Choose a keystore file", new ServerSideFileChooser(new File(mySslKeystoreFile.getValue().toString()), false, true, null) {
                @Override
                protected void onCancel() {
                    getApplication().getMainWindow().removeWindow(getWindow());
                }

                @Override
                protected void onFileSelected(File file) {
                    mySslKeystoreFile.setValue(file.getAbsolutePath());
                    getApplication().getMainWindow().removeWindow(getWindow());
                }
            });
            window.setModal(true);
            window.setResizable(false);
            window.setWidth(600, Sizeable.UNITS_PIXELS);
            getApplication().getMainWindow().addWindow(window);
        }
    }
}
