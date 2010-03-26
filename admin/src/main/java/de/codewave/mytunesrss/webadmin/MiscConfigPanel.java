/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.SmtpProtocol;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.VaadinUtils;

import java.util.Arrays;

public class MiscConfigPanel extends MyTunesRssConfigPanel {

    private Form myMyTunesRssComForm;
    private Form myWebInterfaceForm;
    private Form myProxyForm;
    private Form mySmtpForm;
    private TextField myMyTunesRssComUser;
    private TextField myMyTunesRssComPassword;
    private CheckBox myMyTunesRssComSsl;
    private TextField myWebLoginMessage;
    private TextField myWebWelcomeMessage;
    private CheckBox myServerBrowserActive;
    private TextField myProxyHost;
    private TextField myProxyPort;
    private TextField myMailHost;
    private TextField myMailPort;
    private Select mySmtpProtocol;
    private TextField myMailLogin;
    private TextField myMailPassword;
    private TextField myMailSender;

    public MiscConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("miscConfigPanel.caption"), componentFactory.createGridLayout(1, 5, true, true), componentFactory);
    }

    protected void init(Application application) {
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
        myProxyPort = getComponentFactory().createTextField("miscConfigPanel.proxyPort", ValidatorFactory.createPortValidator());
        myMailHost = getComponentFactory().createTextField("miscConfigPanel.mailHost");
        myMailPort = getComponentFactory().createTextField("miscConfigPanel.mailPort", ValidatorFactory.createPortValidator());
        mySmtpProtocol = getComponentFactory().createSelect("miscConfigPanel.smtpProtocol", Arrays.asList(SmtpProtocol.PLAINTEXT, SmtpProtocol.STARTTLS, SmtpProtocol.SSL));
        myMailLogin = getComponentFactory().createTextField("miscConfigPanel.mailLogin");
        myMailPassword = getComponentFactory().createPasswordTextField("miscConfigPanel.mailPassword");
        myMailSender = getComponentFactory().createTextField("miscConfigPanel.mailSender", ValidatorFactory.createEmailValidator());
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

        addMainButtons(0, 4, 0, 4);
    }

    protected void initFromConfig(Application application) {
        myMyTunesRssComUser.setValue(MyTunesRss.CONFIG.getMyTunesRssComUser());
        myMyTunesRssComPassword.setValue(MyTunesRss.CONFIG.getMyTunesRssComPasswordHash()); // TODO
        myMyTunesRssComSsl.setValue(MyTunesRss.CONFIG.isMyTunesRssComSsl());
        myWebLoginMessage.setValue(MyTunesRss.CONFIG.getWebLoginMessage());
        myWebWelcomeMessage.setValue(MyTunesRss.CONFIG.getWebWelcomeMessage());
        myServerBrowserActive.setValue(MyTunesRss.CONFIG.isServerBrowserActive());
        myProxyHost.setValue(MyTunesRss.CONFIG.getProxyHost());
        myProxyPort.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getProxyPort(), 1, 65535, ""));
        myMailHost.setValue(MyTunesRss.CONFIG.getMailHost());
        myMailPort.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getMailPort(), 1, 65535, ""));
        mySmtpProtocol.setValue(MyTunesRss.CONFIG.getSmtpProtocol());
        myMailLogin.setValue(MyTunesRss.CONFIG.getMailLogin());
        myMailPassword.setValue(MyTunesRss.CONFIG.getMailPassword());
        myMailSender.setValue(MyTunesRss.CONFIG.getMailSender());
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setMyTunesRssComUser((String) myMyTunesRssComUser.getValue());
        MyTunesRss.CONFIG.setMyTunesRssComPasswordHash((byte[]) myMyTunesRssComPassword.getValue()); // TODO
        MyTunesRss.CONFIG.setMyTunesRssComSsl((Boolean) myMyTunesRssComSsl.getValue());
        MyTunesRss.CONFIG.setWebLoginMessage((String) myWebLoginMessage.getValue());
        MyTunesRss.CONFIG.setWebWelcomeMessage((String) myWebWelcomeMessage.getValue());
        MyTunesRss.CONFIG.setServerBrowserActive((Boolean) myServerBrowserActive.getValue());
        MyTunesRss.CONFIG.setProxyHost((String) myProxyHost.getValue());
        MyTunesRss.CONFIG.setProxyPort((Integer) myProxyPort.getValue());
        MyTunesRss.CONFIG.setMailHost((String) myMailHost.getValue());
        MyTunesRss.CONFIG.setMailPort((Integer) myMailPort.getValue());
        MyTunesRss.CONFIG.setSmtpProtocol((SmtpProtocol) mySmtpProtocol.getValue());
        MyTunesRss.CONFIG.setMailLogin((String) myMailLogin.getValue());
        MyTunesRss.CONFIG.setMailPassword((String) myMailPassword.getValue());
        MyTunesRss.CONFIG.setMailSender((String) myMailSender.getValue());
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myMyTunesRssComForm, myWebInterfaceForm, myProxyForm, mySmtpForm);
        if (!valid) {
            getApplication().showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }
}