/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.LdapAuthMethod;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.SelectWindow;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
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
        super.attach();
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
            myUserTable.addContainerProperty("active", Embedded.class, null, "", null, null);
            myUserTable.addContainerProperty("name", String.class, null, getBundleString("userConfigPanel.users.name"), null, null);
            myUserTable.addContainerProperty("group", Select.class, null, getBundleString("userConfigPanel.users.group"), null, null);
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
            myTemplateUser = getComponentFactory().createSelect("userConfigPanel.templateUser", getUsersSortedByName());
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
            addDefaultComponents(0, 3, 0, 3, false);
            initFromConfig();
            myInitialized = true;
        } else {
            initUsersAndGroupsTable();
            Object selectedValue = myTemplateUser.getValue();
            myTemplateUser = getComponentFactory().createSelect("userConfigPanel.templateUser", getUsersSortedByName());
            String templateUserName = MyTunesRss.CONFIG.getLdapConfig().getTemplateUser();
            if (selectedValue != null) {
                myTemplateUser.setValue(selectedValue);
            }
        }
    }

    private List<User> getUsersSortedByName() {
        List<User> users = new ArrayList<User>();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            if (!user.isGroup()) {
                users.add(user);
            }
        }
        Collections.sort(users);
        return users;
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
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            if (user.isGroup()) {
                addGroup(user);
            } else {
                addUser(user);
            }
        }
        myGroupTable.sort();
        myUserTable.sort();
        setTablePageLengths();
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
        User templateUser = (User) myTemplateUser.getValue();
        MyTunesRss.CONFIG.getLdapConfig().setTemplateUser(templateUser != null ? templateUser.getName() : null);
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
            final User user = (User) findTableItemWithObject(myUserTable, clickEvent.getSource());
            Item item = myUserTable.getItem(user);
            if (item.getItemProperty("edit").getValue() == clickEvent.getSource()) {
                editUser(user, false);
            } else if (item.getItemProperty("delete").getValue() == clickEvent.getSource()) {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("userConfigPanel.optionWindowDeleteUser.caption"), getBundleString("userConfigPanel.optionWindowDeleteUser.message", user.getName()), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            MyTunesRss.CONFIG.removeUser(user);
                            MyTunesRss.CONFIG.save();
                            initUsersAndGroupsTable();
                        }
                    }
                }.show(getWindow());
            }
        } else if (findTableItemWithObject(myGroupTable, clickEvent.getSource()) != null) {
            final User group = (User) findTableItemWithObject(myGroupTable, clickEvent.getSource());
            Item item = myGroupTable.getItem(group);
            if (item.getItemProperty("edit").getValue() == clickEvent.getSource()) {
                editUser(group, false);
            } else if (item.getItemProperty("delete").getValue() == clickEvent.getSource()) {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("userConfigPanel.optionWindowDeleteGroup.caption"), getBundleString("userConfigPanel.optionWindowDeleteGroup.message", group.getName()), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            MyTunesRss.CONFIG.removeUser(group);
                            MyTunesRss.CONFIG.save();
                            initUsersAndGroupsTable();
                        }
                    }
                }.show(getWindow());
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private void createUser(boolean group) {
        if (!group) {
            List<User> users = getUsersSortedByName();
            if (users.size() > 0) {
                users.add(0, myNoTemplateUser);
                new SelectWindow<User>(50, Sizeable.UNITS_EM, users, users.get(0), null, getBundleString("userConfigPanel.selectTemplateUser.caption"), getBundleString("userConfigPanel.selectTemplateUser.caption"), getBundleString("userConfigPanel.selectTemplateUser.buttonCreate"), getBundleString("button.cancel")) {
                    @Override
                    protected void onOk(User template) {
                        getParent().removeWindow(this);
                        User user = (User) template.clone();
                        user.setName(getBundleString("userConfigPanel.newUserName"));
                        editUser(user, true);
                    }
                }.show(getWindow());
            } else {
                User user = (User) myNoTemplateUser.clone();
                user.setName(getBundleString("userConfigPanel.newUserName"));
                editUser(user, true);
            }
        } else {
            User user = (User) myNoTemplateUser.clone();
            user.setName(getBundleString("userConfigPanel.newGroupName"));
            user.setGroup(true);
            editUser(user, true);
        }
    }

    private void editUser(User user, boolean newUser) {
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new EditUserConfigPanel(this, user, newUser));
    }

    private void addUser(User user) {
        Embedded icon = null;
        if (!user.isActive()) {
            icon = new Embedded("", new ClassResource("inactive_user.png", getApplication()));
            icon.setDescription(getBundleString("userConfigPanel.userExpired", new SimpleDateFormat(getBundleString("common.dateFormat")).format(new Date(user.getExpiration()))));
        } else if (user.getExpiration() > 0) {
            icon = new Embedded("", new ClassResource("expiring_user.png", getApplication()));
            icon.setDescription(getBundleString("userConfigPanel.userWillExpire", new SimpleDateFormat(getBundleString("common.dateFormat")).format(new Date(user.getExpiration()))));
        }
        myUserTable.addItem(new Object[]{icon, user.getName(), createGroupSelect(user), getComponentFactory().createButton("button.edit", this), getComponentFactory().createButton("button.delete", this)},
                user);
        setTablePageLengths();
    }

    private Select createGroupSelect(final User user) {
        List<User> groups = new ArrayList<User>();
        for (User group : MyTunesRss.CONFIG.getUsers()) {
            if (group.isGroup()) {
                groups.add(group);
            }
        }
        Collections.sort(groups);
        Select select = getComponentFactory().createSelect(null, groups);
        select.setNullSelectionAllowed(true);
        select.setValue(user.getParent());
        select.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                user.setParent((User)event.getProperty().getValue());
                MyTunesRss.CONFIG.save();
            }
        });
        return select;
    }

    private void addGroup(User group) {
        myGroupTable.addItem(new Object[]{group.getName(), getComponentFactory().createButton("button.edit", this), getComponentFactory().createButton("button.delete", this)}, group);
        setTablePageLengths();
    }

    private void setTablePageLengths() {
        myGroupTable.setPageLength(Math.min(myGroupTable.getItemIds().size(), 10));
        myUserTable.setPageLength(Math.min(myUserTable.getItemIds().size(), 10));
    }
}