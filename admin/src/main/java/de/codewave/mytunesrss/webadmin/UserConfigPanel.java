/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Item;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.LdapAuthMethod;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.SelectWindow;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class UserConfigPanel extends MyTunesRssConfigPanel {

    private Panel myUserPanel;
    private Panel myGroupsPanel;
    private Table myGroupTable;
    private Table myUserTable;
    private Button myAddGroup;
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
    private boolean myInitialized;
    private User myNoTemplateUser;

    public void attach() {
        if (!myInitialized) {
            init(getBundleString("userConfigPanel.caption"), getComponentFactory().createGridLayout(1, 4, true, true));
            myNoTemplateUser = new User(getBundleString("userConfigPanel.selectTemplateUser.noTemplateOption"));
            myGroupsPanel = new Panel(getBundleString("userConfigPanel.caption.groups"), getComponentFactory().createVerticalLayout(true, true));
            myGroupTable = new Table();
            myGroupTable.addContainerProperty("name", String.class, null, getBundleString("userConfigPanel.groups.name"), null, null);
            myGroupTable.addContainerProperty("edit", Button.class, null, null, null, null);
            myGroupTable.addContainerProperty("delete", Button.class, null, null, null, null);
            myGroupTable.setEditable(false);
            myGroupTable.setSortContainerPropertyId("name");
            myGroupsPanel.addComponent(myGroupTable);
            myAddGroup = getComponentFactory().createButton("userConfigPanel.addGroup", this);
            myGroupsPanel.addComponent(myAddGroup);
            addComponent(myGroupsPanel);
            myUserPanel = new Panel(getBundleString("userConfigPanel.caption.users"), getComponentFactory().createVerticalLayout(true, true));
            myUserTable = new Table();
            myUserTable.addContainerProperty("name", String.class, null, getBundleString("userConfigPanel.users.name"), null, null);
            myUserTable.addContainerProperty("group", String.class, null, getBundleString("userConfigPanel.users.group"), null, null);
            myUserTable.addContainerProperty("edit", Button.class, null, null, null, null);
            myUserTable.addContainerProperty("delete", Button.class, null, null, null, null);
            myUserTable.setEditable(false);
            myUserTable.setSortContainerPropertyId("name");
            myUserPanel.addComponent(myUserTable);
            myAddUser = getComponentFactory().createButton("userConfigPanel.addUser", this);
            myUserPanel.addComponent(myAddUser);
            addComponent(myUserPanel);
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
            attach(0, 3, 0, 3);
            initFromConfig();
            myInitialized = true;
        }
    }

    protected void initFromConfig() {
        initUsersAndGroupsTable();
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

    private void initUsersAndGroupsTable() {
        myGroupTable.removeAllItems();
        myUserTable.removeAllItems();
        for (User user : MyTunesRss.CONFIG.getUserClones()) {
            if (user.isGroup()) {
                addGroup(user, getComponentFactory());
            } else {
                addUser(user, getComponentFactory());
            }
        }
        myGroupTable.sort();
        myUserTable.sort();
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setUsers(getUsers());
        MyTunesRss.CONFIG.getLdapConfig().setHost(myLdapHost.getStringValue(null));
        MyTunesRss.CONFIG.getLdapConfig().setPort(myLdapPort.getIntegerValue(-1));
        MyTunesRss.CONFIG.getLdapConfig().setAuthMethod((LdapAuthMethod) myLdapAuthMethod.getValue());
        MyTunesRss.CONFIG.getLdapConfig().setAuthPrincipal(myLdapAuthPrincipal.getStringValue(null));
        MyTunesRss.CONFIG.getLdapConfig().setSearchRoot(myLdapSearchRoot.getStringValue(null));
        MyTunesRss.CONFIG.getLdapConfig().setSearchExpression(myLdapSearchExpression.getStringValue(null));
        MyTunesRss.CONFIG.getLdapConfig().setSearchTimeout(myLdapSearchTimeout.getIntegerValue(0));
        MyTunesRss.CONFIG.getLdapConfig().setMailAttributeName(myLdapEmailAttribute.getStringValue(null));
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected boolean beforeSave() {
        return VaadinUtils.isValid(myLdapForm);
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddUser) {
            createUser(false);
        } else if (clickEvent.getSource() == myAddGroup) {
            createUser(true);
        } else if (findTableItemWithObject(myUserTable, clickEvent.getSource()) != null) {
            final User user = (User)findTableItemWithObject(myUserTable, clickEvent.getSource());
            Item item = myUserTable.getItem(user);
            if (item.getItemProperty("edit").getValue() == clickEvent.getSource()) {
                editUser(user);
            } else if (item.getItemProperty("delete").getValue() == clickEvent.getSource()) {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("userConfigPanel.optionWindowDeleteUser.caption"), getBundleString("userConfigPanel.optionWindowDeleteUser.message", user.getName()), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            myUserTable.removeItem(user);
                            setTablePageLengths();
                        }
                    }
                }.show(getApplication().getMainWindow());
            }
        } else if (findTableItemWithObject(myGroupTable, clickEvent.getSource()) != null) {
            final User group = (User)findTableItemWithObject(myGroupTable, clickEvent.getSource());
            Item item = myGroupTable.getItem(group);
            if (item.getItemProperty("edit").getValue() == clickEvent.getSource()) {
                editUser(group);
            } else if (item.getItemProperty("delete").getValue() == clickEvent.getSource()) {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("userConfigPanel.optionWindowDeleteGroup.caption"), getBundleString("userConfigPanel.optionWindowDeleteGroup.message", group.getName()), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            myGroupTable.removeItem(group);
                            setTablePageLengths();
                        }
                    }
                }.show(getApplication().getMainWindow());
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private void createUser(final boolean group) {
        List<User> users = new ArrayList<User>(MyTunesRss.CONFIG.getUsers());
        Collections.sort(users);
        users.add(0, myNoTemplateUser);
        new SelectWindow<User>(50, Sizeable.UNITS_EM, users, users.get(0), null, getBundleString("userConfigPanel.selectTemplateUser.caption"), getBundleString("userConfigPanel.selectTemplateUser.caption"), getBundleString("userConfigPanel.selectTemplateUser.buttonCreate"), getBundleString("button.cancel")) {
            @Override
            protected void onOk(User template) {
                getApplication().getMainWindow().removeWindow(this);
                User user = (User) template.clone();
                user.setName(getBundleString("userConfigPanel.newUserName"));
                user.setGroup(group);
                editUser(user);
            }
        }.show(getApplication().getMainWindow());
    }

    private void editUser(User user) {
        getApplication().setMainComponent(new EditUserConfigPanel(this, user));
    }

    Set<User> getUsers() {
        Set<User> users = new HashSet<User>();
        for (Object itemId : myUserTable.getItemIds()) {
            users.add((User) itemId);
        }
        for (Object itemId : myGroupTable.getItemIds()) {
            users.add((User) itemId);
        }
        return users;
    }

    void saveUser(User user, ComponentFactory componentFactory) {
        if (!user.isGroup() && !myUserTable.containsId(user)) {
            addUser(user, componentFactory);
            myUserTable.sort();
        } else if (user.isGroup() && !myGroupTable.containsId(user)) {
            addGroup(user, componentFactory);
            myGroupTable.sort();
        }
    }

    private void addUser(User user, ComponentFactory componentFactory) {
        myUserTable.addItem(new Object[]{user.getName(), user.getParent() != null ? user.getParent().getName() : null, componentFactory.createButton("button.edit", this), componentFactory.createButton("button.delete", this)}, user);
        setTablePageLengths();
    }

    private void addGroup(User group, ComponentFactory componentFactory) {
        myGroupTable.addItem(new Object[]{group.getName(), componentFactory.createButton("button.edit", this), componentFactory.createButton("button.delete", this)}, group);
        setTablePageLengths();
    }

    private void setTablePageLengths() {
        myGroupTable.setPageLength(Math.min(myGroupTable.getItemIds().size(), 10));
        myUserTable.setPageLength(Math.min(myUserTable.getItemIds().size(), 10));
    }
}