/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.SmtpProtocol;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;

import java.util.Arrays;

public class MiscConfigPanel extends MyTunesRssConfigPanel {

    private Form myMyTunesRssComForm;
    private Form myWebInterfaceForm;
    private Form myProxyForm;
    private Form mySmtpForm;
    private SmartTextField myMyTunesRssComUser;
    private SmartTextField myMyTunesRssComPassword;
    private CheckBox myMyTunesRssComSsl;
    private SmartTextField myWebLoginMessage;
    private SmartTextField myWebWelcomeMessage;
    private CheckBox myServerBrowserActive;
    private SmartTextField myProxyHost;
    private SmartTextField myProxyPort;
    private SmartTextField myMailHost;
    private SmartTextField myMailPort;
    private Select mySmtpProtocol;
    private SmartTextField myMailLogin;
    private SmartTextField myMailPassword;
    private SmartTextField myMailSender;

    public void attach() {
        super.attach();
        init(getBundleString("miscConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));
        myMyTunesRssComForm = getComponentFactory().createForm(null, true);
        myWebInterfaceForm = getComponentFactory().createForm(null, true);
        myProxyForm = getComponentFactory().createForm(null, true);
        mySmtpForm = getComponentFactory().createForm(null, true);
        myMyTunesRssComUser = getComponentFactory().createTextField("miscConfigPanel.myTunesRssComUser");
        myMyTunesRssComPassword = getComponentFactory().createPasswordTextField("miscConfigPanel.myTunesRssComPassword");
        myMyTunesRssComSsl = getComponentFactory().createCheckBox("miscConfigPanel.myTunesRssComSsl");
        myWebLoginMessage = getComponentFactory().createTextField("miscConfigPanel.webLoginMessage");
        myWebWelcomeMessage = getComponentFactory().createTextField("miscConfigPanel.webWelcomeMessage");
        myServerBrowserActive = getComponentFactory().createCheckBox("miscConfigPanel.serverBrowserActive");
        myProxyHost = getComponentFactory().createTextField("miscConfigPanel.proxyHost");
        myProxyPort = getComponentFactory().createTextField("miscConfigPanel.proxyPort", getApplication().getValidatorFactory().createPortValidator());
        myMailHost = getComponentFactory().createTextField("miscConfigPanel.mailHost");
        myMailPort = getComponentFactory().createTextField("miscConfigPanel.mailPort", getApplication().getValidatorFactory().createPortValidator());
        mySmtpProtocol = getComponentFactory().createSelect("miscConfigPanel.smtpProtocol", Arrays.asList(SmtpProtocol.PLAINTEXT, SmtpProtocol.STARTTLS, SmtpProtocol.SSL));
        myMailLogin = getComponentFactory().createTextField("miscConfigPanel.mailLogin");
        myMailPassword = getComponentFactory().createPasswordTextField("miscConfigPanel.mailPassword");
        myMailSender = getComponentFactory().createTextField("miscConfigPanel.mailSender", getApplication().getValidatorFactory().createEmailValidator());
        myMyTunesRssComForm.addField(myMyTunesRssComUser, myMyTunesRssComUser);
        myMyTunesRssComForm.addField(myMyTunesRssComPassword, myMyTunesRssComPassword);
        myMyTunesRssComForm.addField(myMyTunesRssComSsl, myMyTunesRssComSsl);
        Panel myTunesRssComPanel = getComponentFactory().surroundWithPanel(myMyTunesRssComForm, FORM_PANEL_MARGIN_INFO, getBundleString("miscConfigPanel.caption.myTunesRssCom"));
        addComponent(myTunesRssComPanel);
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
        myWebInterfaceForm.addField(myServerBrowserActive, myServerBrowserActive);
        Panel webInterfacePanel = getComponentFactory().surroundWithPanel(myWebInterfaceForm, FORM_PANEL_MARGIN_INFO, getBundleString("miscConfigPanel.caption.webInterface"));
        addComponent(webInterfacePanel);

        addDefaultComponents(0, 4, 0, 4, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myMyTunesRssComUser.setValue(MyTunesRss.CONFIG.getMyTunesRssComUser());
        myMyTunesRssComPassword.setValue(MyTunesRss.CONFIG.getMyTunesRssComPasswordHash());
        myMyTunesRssComSsl.setValue(MyTunesRss.CONFIG.isMyTunesRssComSsl());
        myWebLoginMessage.setValue(MyTunesRss.CONFIG.getWebLoginMessage());
        myWebWelcomeMessage.setValue(MyTunesRss.CONFIG.getWebWelcomeMessage());
        myServerBrowserActive.setValue(MyTunesRss.CONFIG.isServerBrowserActive());
        myProxyHost.setValue(MyTunesRss.CONFIG.getProxyHost());
        myProxyPort.setValue(MyTunesRss.CONFIG.getProxyPort(), 1, 65535, "");
        myMailHost.setValue(MyTunesRss.CONFIG.getMailHost());
        myMailPort.setValue(MyTunesRss.CONFIG.getMailPort(), 1, 65535, "");
        mySmtpProtocol.setValue(MyTunesRss.CONFIG.getSmtpProtocol());
        myMailLogin.setValue(MyTunesRss.CONFIG.getMailLogin());
        myMailPassword.setValue(MyTunesRss.CONFIG.getMailPassword());
        myMailSender.setValue(MyTunesRss.CONFIG.getMailSender());
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setMyTunesRssComUser(myMyTunesRssComUser.getStringValue(null));
        MyTunesRss.CONFIG.setMyTunesRssComPasswordHash(myMyTunesRssComPassword.getStringHashValue(MyTunesRss.SHA1_DIGEST));
        MyTunesRss.CONFIG.setMyTunesRssComSsl(myMyTunesRssComSsl.booleanValue());
        MyTunesRss.CONFIG.setWebLoginMessage(myWebLoginMessage.getStringValue(null));
        MyTunesRss.CONFIG.setWebWelcomeMessage(myWebWelcomeMessage.getStringValue(null));
        MyTunesRss.CONFIG.setServerBrowserActive(myServerBrowserActive.booleanValue());
        MyTunesRss.CONFIG.setProxyHost(myProxyHost.getStringValue(null));
        MyTunesRss.CONFIG.setProxyPort(myProxyPort.getIntegerValue(-1));
        MyTunesRss.CONFIG.setMailHost(myMailHost.getStringValue(null));
        MyTunesRss.CONFIG.setMailPort(myMailPort.getIntegerValue(-1));
        MyTunesRss.CONFIG.setSmtpProtocol((SmtpProtocol) mySmtpProtocol.getValue());
        MyTunesRss.CONFIG.setMailLogin(myMailLogin.getStringValue(null));
        MyTunesRss.CONFIG.setMailPassword(myMailPassword.getStringValue(null));
        MyTunesRss.CONFIG.setMailSender(myMailSender.getStringValue(null));
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myMyTunesRssComForm, myWebInterfaceForm, myProxyForm, mySmtpForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }
}