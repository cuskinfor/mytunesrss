/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.SmtpProtocol;
import de.codewave.vaadin.SmartPasswordField;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.GraphicsMagickExecutableFileValidator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;

public class MiscConfigPanel extends MyTunesRssConfigPanel {

    private Form myMainWindowForm;
    private Form myMyTunesRssComForm;
    private Form myWebInterfaceForm;
    private Form myProxyForm;
    private Form mySmtpForm;
    private CheckBox myHeadless;
    private SmartTextField myWebLoginMessage;
    private SmartTextField myWebWelcomeMessage;
    private SmartTextField myRssDescription;
    private CheckBox myServerBrowserActive;
    private CheckBox myOpenIdActive;
    private SmartTextField myProxyHost;
    private SmartTextField myProxyPort;
    private SmartTextField myMailHost;
    private SmartTextField myMailPort;
    private Select mySmtpProtocol;
    private SmartTextField myMailLogin;
    private SmartPasswordField myMailPassword;
    private SmartTextField myMailSender;
    private CheckBox myGraphicsMagickEnabled;
    private SmartTextField myGraphicsMagickBinary;
    private Button myGraphicsMagickBinarySelect;
    private Form myGraphicsMagickForm;
    private Button myGraphicsMagickHomepageButton;

    public void attach() {
        super.attach();
        init(getBundleString("miscConfigPanel.caption"), getComponentFactory().createGridLayout(1, 6, true, true));
        myMainWindowForm = getComponentFactory().createForm(null, true);
        myMyTunesRssComForm = getComponentFactory().createForm(null, true);
        myWebInterfaceForm = getComponentFactory().createForm(null, true);
        myProxyForm = getComponentFactory().createForm(null, true);
        mySmtpForm = getComponentFactory().createForm(null, true);
        myWebLoginMessage = getComponentFactory().createTextField("miscConfigPanel.webLoginMessage");
        myWebWelcomeMessage = getComponentFactory().createTextField("miscConfigPanel.webWelcomeMessage");
        myRssDescription = getComponentFactory().createTextField("miscConfigPanel.rssDescription");
        myServerBrowserActive = getComponentFactory().createCheckBox("miscConfigPanel.serverBrowserActive");
        myOpenIdActive = getComponentFactory().createCheckBox("miscConfigPanel.openIdActive");
        myProxyHost = getComponentFactory().createTextField("miscConfigPanel.proxyHost");
        myProxyPort = getComponentFactory().createTextField("miscConfigPanel.proxyPort", getApplication().getValidatorFactory().createPortValidator());
        myMailHost = getComponentFactory().createTextField("miscConfigPanel.mailHost");
        myMailPort = getComponentFactory().createTextField("miscConfigPanel.mailPort", getApplication().getValidatorFactory().createPortValidator());
        mySmtpProtocol = getComponentFactory().createSelect("miscConfigPanel.smtpProtocol", Arrays.asList(SmtpProtocol.PLAINTEXT, SmtpProtocol.STARTTLS, SmtpProtocol.SSL));
        myMailLogin = getComponentFactory().createTextField("miscConfigPanel.mailLogin");
        myMailPassword = getComponentFactory().createPasswordTextField("miscConfigPanel.mailPassword");
        myMailSender = getComponentFactory().createTextField("miscConfigPanel.mailSender", getApplication().getValidatorFactory().createEmailValidator());
        myHeadless = getComponentFactory().createCheckBox("miscConfigPanel.headless");
        myMainWindowForm.addField(myHeadless, myHeadless);
        Panel mainWindowPanel = getComponentFactory().surroundWithPanel(myMainWindowForm, FORM_PANEL_MARGIN_INFO, getBundleString("miscConfigPanel.caption.mainWindow"));
        addComponent(mainWindowPanel);
        myProxyForm.addField(myProxyHost, myProxyHost);
        myProxyForm.addField(myProxyPort, myProxyPort);
        Panel proxyPanel = getComponentFactory().surroundWithPanel(myProxyForm, FORM_PANEL_MARGIN_INFO, getBundleString("miscConfigPanel.caption.proxy"));
        addComponent(proxyPanel);
        mySmtpForm.addField(myMailHost, myMailHost);
        mySmtpForm.addField(myMailPort, myMailPort);
        mySmtpForm.addField(mySmtpProtocol, mySmtpProtocol);
        mySmtpForm.addField(myMailLogin, myMailLogin);
        mySmtpForm.addField(myMailPassword, myMailPassword);
        mySmtpForm.addField(myMailSender, myMailSender);
        Panel smtpPanel = getComponentFactory().surroundWithPanel(mySmtpForm, FORM_PANEL_MARGIN_INFO, getBundleString("miscConfigPanel.caption.smtp"));
        addComponent(smtpPanel);
        myWebInterfaceForm.addField(myWebLoginMessage, myWebLoginMessage);
        myWebInterfaceForm.addField(myWebWelcomeMessage, myWebWelcomeMessage);
        myWebInterfaceForm.addField(myRssDescription, myRssDescription);
        myWebInterfaceForm.addField(myServerBrowserActive, myServerBrowserActive);
        myWebInterfaceForm.addField(myOpenIdActive, myOpenIdActive);
        Panel webInterfacePanel = getComponentFactory().surroundWithPanel(myWebInterfaceForm, FORM_PANEL_MARGIN_INFO, getBundleString("miscConfigPanel.caption.webInterface"));
        addComponent(webInterfacePanel);
        myGraphicsMagickEnabled = getComponentFactory().createCheckBox("miscConfigPanel.graphicsMagickEnabled");
        myGraphicsMagickBinary = getComponentFactory().createTextField("miscConfigPanel.graphicsMagickBinary", new GraphicsMagickExecutableFileValidator(getBundleString("miscConfigPanel.graphicsMagickBinary.invalidBinary")));
        myGraphicsMagickBinary.setImmediate(false);
        myGraphicsMagickBinarySelect = getComponentFactory().createButton("miscConfigPanel.graphicsMagickBinary.select", this);
        myGraphicsMagickHomepageButton = getComponentFactory().createButton("miscConfigPanel.graphicsMagickHomepage", this);
        myGraphicsMagickForm = getComponentFactory().createForm(null, true);
        myGraphicsMagickForm.addField(myGraphicsMagickEnabled, myGraphicsMagickEnabled);
        myGraphicsMagickForm.addField(myGraphicsMagickBinary, myGraphicsMagickBinary);
        myGraphicsMagickForm.addField(myGraphicsMagickBinarySelect, myGraphicsMagickBinarySelect);
        myGraphicsMagickForm.addField(myGraphicsMagickHomepageButton, myGraphicsMagickHomepageButton);
        Panel graphicsMagickPanel = getComponentFactory().surroundWithPanel(myGraphicsMagickForm, FORM_PANEL_MARGIN_INFO, getBundleString("miscConfigPanel.caption.GraphicsMagick"));
        addComponent(graphicsMagickPanel);

        addDefaultComponents(0, 5, 0, 5, false);

        initFromConfig();
    }

    protected void initFromConfig() {

        myWebLoginMessage.setValue(MyTunesRss.CONFIG.getWebLoginMessage());
        myWebWelcomeMessage.setValue(MyTunesRss.CONFIG.getWebWelcomeMessage());
        myRssDescription.setValue(MyTunesRss.CONFIG.getRssDescription());
        myServerBrowserActive.setValue(MyTunesRss.CONFIG.isServerBrowserActive());
        myOpenIdActive.setValue(MyTunesRss.CONFIG.isOpenIdActive());
        myProxyHost.setValue(MyTunesRss.CONFIG.getProxyHost());
        myProxyPort.setValue(MyTunesRss.CONFIG.getProxyPort(), 1, 65535, "");
        myMailHost.setValue(MyTunesRss.CONFIG.getMailHost());
        myMailPort.setValue(MyTunesRss.CONFIG.getMailPort(), 1, 65535, "");
        mySmtpProtocol.setValue(MyTunesRss.CONFIG.getSmtpProtocol());
        myMailLogin.setValue(MyTunesRss.CONFIG.getMailLogin());
        myMailPassword.setValue(MyTunesRss.CONFIG.getMailPassword());
        myMailSender.setValue(MyTunesRss.CONFIG.getMailSender());
        myHeadless.setValue(MyTunesRss.CONFIG.isHeadless());
        myGraphicsMagickEnabled.setValue(MyTunesRss.CONFIG.isGmEnabled());
        myGraphicsMagickBinary.setValue(MyTunesRss.CONFIG.getGmExecutable() != null ? MyTunesRss.CONFIG.getGmExecutable().getAbsolutePath() : "");
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setWebLoginMessage(myWebLoginMessage.getStringValue(null));
        MyTunesRss.CONFIG.setWebWelcomeMessage(myWebWelcomeMessage.getStringValue(null));
        MyTunesRss.CONFIG.setRssDescription(myRssDescription.getStringValue(null));
        MyTunesRss.CONFIG.setServerBrowserActive(myServerBrowserActive.booleanValue());
        MyTunesRss.CONFIG.setOpenIdActive(myOpenIdActive.booleanValue());
        MyTunesRss.CONFIG.setProxyHost(myProxyHost.getStringValue(null));
        MyTunesRss.CONFIG.setProxyPort(myProxyPort.getIntegerValue(-1));
        MyTunesRss.CONFIG.setMailHost(myMailHost.getStringValue(null));
        MyTunesRss.CONFIG.setMailPort(myMailPort.getIntegerValue(-1));
        MyTunesRss.CONFIG.setSmtpProtocol((SmtpProtocol) mySmtpProtocol.getValue());
        MyTunesRss.CONFIG.setMailLogin(myMailLogin.getStringValue(null));
        MyTunesRss.CONFIG.setMailPassword(myMailPassword.getStringValue(null));
        MyTunesRss.CONFIG.setMailSender(myMailSender.getStringValue(null));
        MyTunesRss.CONFIG.setHeadless(myHeadless.booleanValue());
        String gmBinary = myGraphicsMagickBinary.getStringValue(null);
        MyTunesRss.CONFIG.setGmExecutable(gmBinary != null ? new File(gmBinary) : null);
        MyTunesRss.CONFIG.setGmEnabled(myGraphicsMagickEnabled.booleanValue());
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myGraphicsMagickForm, myMainWindowForm, myMyTunesRssComForm, myWebInterfaceForm, myProxyForm, mySmtpForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        } else {
            if (MyTunesRss.CONFIG.isHeadless() != myHeadless.booleanValue()) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showWarning("miscConfigPanel.warning.headlessChanged");
            }
        }
        return valid;
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myGraphicsMagickHomepageButton) {
            getWindow().open(new ExternalResource("http://www.graphicsmagick.org"));
        } else if (clickEvent.getButton() == myGraphicsMagickBinarySelect) {
            File dir = StringUtils.isNotBlank((CharSequence) myGraphicsMagickBinary.getValue()) ? new File((String) myGraphicsMagickBinary.getValue()) : null;
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("miscConfigPanel.caption.selectGraphicsMagickBinary"), dir, null, ServerSideFileChooser.PATTERN_ALL, false, getApplication().getServerSideFileChooserLabels()) {
                @Override
                protected void onFileSelected(File file) {
                    myGraphicsMagickBinary.setValue(file.getAbsolutePath());
                    getWindow().getParent().removeWindow(this);
                }
            }.show(getWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }
}
