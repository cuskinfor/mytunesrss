/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.LdapAuthMethod;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserConfigPanel extends MyTunesRssConfigPanel implements ItemClickEvent.ItemClickListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserConfigPanel.class);

    private Panel myUserTreePanel;
    private Tree myUserTree;
    private Button myAddUser;
    private Form myLdapForm;
    private SmartTextField myLdapHost;
    private SmartTextField myLdapPort;
    private Select myLdapAuthMethod;
    private SmartTextField myLdapAuthPrincipal;
    private SmartTextField myLdapSearchRoot;
    private SmartTextField myLdapSearchExpression;
    private SmartTextField myLdapSearchTimeout;
    private SmartTextField myLdapEmailAttribute;
    private Select myTemplateUser;

    public UserConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("userConfigPanel.caption"), componentFactory.createGridLayout(1, 3, true, true), componentFactory);
    }

    protected void init(Application application) {
        myUserTreePanel = new Panel(getBundleString("userConfigPanel.caption.themes"), getComponentFactory().createVerticalLayout(true, true));
        myUserTree = new Tree();
        myUserTree.addListener(this);
        myUserTreePanel.addComponent(myUserTree);
        myAddUser = getComponentFactory().createButton("userConfigPanel.addUser", this);
        myUserTreePanel.addComponent(myAddUser);
        addComponent(myUserTreePanel);
        myLdapForm = getComponentFactory().createForm(null, true);
        myLdapHost = getComponentFactory().createTextField("userConfigPanel.ldapHost");
        myLdapPort = getComponentFactory().createTextField("userConfigPanel.ldapPort");
        myLdapAuthMethod = getComponentFactory().createSelect("userConfigPanel.ldapAuthMethod", Arrays.asList(LdapAuthMethod.SIMPLE));
        myLdapAuthPrincipal = getComponentFactory().createTextField("userConfigPanel.ldapAuthPrincipal");
        myLdapSearchRoot = getComponentFactory().createTextField("userConfigPanel.ldapSearchRoot");
        myLdapSearchExpression = getComponentFactory().createTextField("userConfigPanel.ldapSearchExpression");
        myLdapSearchTimeout = getComponentFactory().createTextField("userConfigPanel.ldapSearchTimeout");
        myLdapEmailAttribute = getComponentFactory().createTextField("userConfigPanel.ldapEmailAttribute");
        List<User> users = new ArrayList<User>(MyTunesRss.CONFIG.getUsers());
        Collections.sort(users);
        myTemplateUser = getComponentFactory().createSelect("userConfigPanel.templateUser", users);
        myLdapForm.addField("ldapHost", myLdapHost);
        myLdapForm.addField("ldapPort", myLdapPort);
        myLdapForm.addField("ldapAuthMethod", myLdapAuthMethod);
        myLdapForm.addField("ldapAuthPrincipal", myLdapAuthPrincipal);
        myLdapForm.addField("ldapSearchRoot", myLdapSearchRoot);
        myLdapForm.addField("ldapSearchExpression", myLdapSearchExpression);
        myLdapForm.addField("ldapSearchTimeout", myLdapSearchTimeout);
        myLdapForm.addField("ldapEmailAttribute", myLdapEmailAttribute);
        myLdapForm.addField("templateUser", myTemplateUser);
        addComponent(getComponentFactory().surroundWithPanel(myLdapForm, FORM_PANEL_MARGIN_INFO, getBundleString("userConfigPanel.caption.ldap")));
        addMainButtons(0, 2, 0, 2);

    }

    protected void initFromConfig(Application application) {
        initUserTree();
        myLdapHost.setValue(MyTunesRss.CONFIG.getLdapConfig().getHost());
        myLdapPort.setValue(MyTunesRss.CONFIG.getLdapConfig().getPort(), 1, 65535, "");
        myLdapAuthMethod.setValue(MyTunesRss.CONFIG.getLdapConfig().getAuthMethod());
        myLdapAuthPrincipal.setValue(MyTunesRss.CONFIG.getLdapConfig().getAuthPrincipal());
        myLdapSearchRoot.setValue(MyTunesRss.CONFIG.getLdapConfig().getSearchRoot());
        myLdapSearchExpression.setValue(MyTunesRss.CONFIG.getLdapConfig().getSearchExpression());
        myLdapSearchTimeout.setValue(MyTunesRss.CONFIG.getLdapConfig().getSearchTimeout(), 1, 60000, "");
        myLdapEmailAttribute.setValue(MyTunesRss.CONFIG.getLdapConfig().getMailAttributeName());
        String templateUserName = MyTunesRss.CONFIG.getLdapConfig().getTemplateUser();
        if (StringUtils.isNotBlank(templateUserName)) {
            myTemplateUser.setValue(MyTunesRss.CONFIG.getUser(templateUserName));
        }
    }

    private void initUserTree() {
        myUserTree.removeAllItems();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            myUserTree.addItem(user);
            myUserTree.setChildrenAllowed(user, false);
        }
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            if (user.getParent() != null) {
                myUserTree.setChildrenAllowed(user.getParent(), true);
                myUserTree.setParent(user, user.getParent());
            }
        }
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.getLdapConfig().setHost(myLdapHost.getStringValue(null));
        MyTunesRss.CONFIG.getLdapConfig().setPort(myLdapPort.getIntegerValue(-1));
        MyTunesRss.CONFIG.getLdapConfig().setAuthMethod((LdapAuthMethod) myLdapAuthMethod.getValue());
        MyTunesRss.CONFIG.getLdapConfig().setAuthPrincipal(myLdapAuthPrincipal.getStringValue(null));
        MyTunesRss.CONFIG.getLdapConfig().setSearchRoot(myLdapSearchRoot.getStringValue(null));
        MyTunesRss.CONFIG.getLdapConfig().setSearchExpression(myLdapSearchExpression.getStringValue(null));
        MyTunesRss.CONFIG.getLdapConfig().setSearchTimeout(myLdapSearchTimeout.getIntegerValue(0));
        MyTunesRss.CONFIG.getLdapConfig().setMailAttributeName(myLdapEmailAttribute.getStringValue(null));
    }

    @Override
    protected boolean beforeSave() {
        return VaadinUtils.isValid(myLdapForm);
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddUser) {
            editUser(new User("new user"), true);
        } else {
            super.buttonClick(clickEvent);
        }
    }

    public void itemClick(ItemClickEvent itemClickEvent) {
        editUser((User) itemClickEvent.getItemId(), false);
    }

    private void editUser(User user, boolean newUser) {
        getApplication().setMainComponent(new EditUserConfigPanel(getApplication(), getComponentFactory(), this, user, newUser));
    }
}