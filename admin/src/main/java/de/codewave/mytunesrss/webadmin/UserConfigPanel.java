/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class UserConfigPanel extends MyTunesRssConfigPanel implements ItemClickEvent.ItemClickListener, Action.Handler {

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
    private boolean myInitialized;
    private User myNoTemplateUser;

    public void attach() {
        if (!myInitialized) {
            init(getBundleString("userConfigPanel.caption"), getComponentFactory().createGridLayout(1, 3, true, true));
            myNoTemplateUser = new User(getBundleString("userConfigPanel.selectTemplateUser.noTemplateOption"));
            myUserTreePanel = new Panel(getBundleString("userConfigPanel.caption.themes"), getComponentFactory().createVerticalLayout(true, true));
            myUserTree = new Tree();
            myUserTree.addListener(this);
            myUserTree.addActionHandler(this);
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
            attach(0, 2, 0, 2);
            initFromConfig();
            myInitialized = true;
        }
    }

    protected void initFromConfig() {
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
        Collection<User> clones = MyTunesRss.CONFIG.getUserClones();
        for (User user : clones) {
            myUserTree.addItem(user);
            myUserTree.setChildrenAllowed(user, false);
        }
        for (User user : clones) {
            if (user.getParent() != null) {
                myUserTree.setChildrenAllowed(user.getParent(), true);
                myUserTree.setParent(user, user.getParent());
            }
        }
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
    }

    @Override
    protected boolean beforeSave() {
        return VaadinUtils.isValid(myLdapForm);
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddUser) {
            createUser(null);
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private void createUser(final User parentUser) {
        List<User> users = new ArrayList<User>(MyTunesRss.CONFIG.getUsers());
        Collections.sort(users);
        users.add(0, myNoTemplateUser);
        new SelectWindow<User>(50, Sizeable.UNITS_EM, users, users.get(0), null, getBundleString("userConfigPanel.selectTemplateUser.caption"), getBundleString("userConfigPanel.selectTemplateUser.caption"), getBundleString("userConfigPanel.selectTemplateUser.buttonCreate"), getBundleString("button.cancel")) {
            @Override
            protected void onOk(User template) {
                getApplication().getMainWindow().removeWindow(this);
                User user = (User) template.clone();
                user.setName(getBundleString("userConfigPanel.newUserName"));
                if (parentUser != null) {
                    user.setParent(parentUser);
                }
                editUser(user);
            }
        }.show(getApplication().getMainWindow());


    }

    public void itemClick(ItemClickEvent itemClickEvent) {
        if (itemClickEvent.getButton() == ItemClickEvent.BUTTON_LEFT) {
            editUser((User) itemClickEvent.getItemId());
        }
    }

    private void editUser(User user) {
        getApplication().setMainComponent(new EditUserConfigPanel(this, user));
    }

    public Action[] getActions(Object target, Object sender) {
        if (getMoveTargetUsers((User) target).isEmpty()) {
            return new Action[]{new AddUserAction((User) target), new RemoveUserAction((User) target)};
        } else {
            return new Action[]{new AddUserAction((User) target), new MoveUserAction((User) target), new RemoveUserAction((User) target)};
        }
    }

    public void handleAction(Action action, Object sender, final Object target) {
        if (action instanceof AddUserAction) {
            createUser((User) target);
        } else if (action instanceof MoveUserAction) {
            final User user = (User) target;
            final List<User> targetUsers = getMoveTargetUsers(user);
            final User makeRootUserItem = new User(getBundleString("userConfigPanel.moveUser.makeRoot"));
            if (user.getParent() != null) {
                targetUsers.add(0, makeRootUserItem);
            }
            new SelectWindow<User>(50, Sizeable.UNITS_EM, targetUsers, null, null, getBundleString("userConfigPanel.moveUser.caption"), getBundleString("userConfigPanel.moveUser.message", user.getName()), getBundleString("button.ok"), getBundleString("button.cancel")) {
                @Override
                protected void onOk(User targetUser) {
                    User oldParent = user.getParent();
                    if (targetUser == makeRootUserItem) {
                        user.setParent(null);
                        myUserTree.setParent(user, null);
                    } else {
                        user.setParent(targetUser);
                        myUserTree.setChildrenAllowed(targetUser, true);
                        myUserTree.setParent(user, targetUser);
                        myUserTree.expandItem(targetUser);
                        myUserTree.select(user);
                    }
                    if (oldParent != null) {
                        myUserTree.setChildrenAllowed(oldParent, !myUserTree.getChildren(oldParent).isEmpty());
                    }
                    getApplication().getMainWindow().removeWindow(this);
                }
            }.show(getApplication().getMainWindow());
        } else if (action instanceof RemoveUserAction) {
            final User user = (User) target;
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("userConfigPanel.optionWindowDeleteUser.caption"), getBundleString("userConfigPanel.optionWindowDeleteUser.message", user.getName()), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myUserTree.removeItem(user);
                        if (user.getParent() != null) {
                            myUserTree.setChildrenAllowed(user.getParent(), !myUserTree.getChildren(user.getParent()).isEmpty());
                            myUserTree.select(user.getParent());
                        }
                    }
                }
            }.show(getApplication().getMainWindow());
        } else {
            throw new IllegalArgumentException("Illegal action of type \"" + action.getClass() + "\".");
        }
    }

    private List<User> getMoveTargetUsers(User user) {
        final List<User> targetUsers = new ArrayList<User>();
        for (User otherUser : getUsers()) {
            if (otherUser != user && !isChildUser(user, otherUser) && otherUser != user.getParent()) {
                targetUsers.add(otherUser);
            }
        }
        Collections.sort(targetUsers);
        return targetUsers;
    }

    /**
     * Check if the other user is a child user of the user.
     *
     * @param user      Parent user.
     * @param otherUser Other user.
     * @return TRUE if the other user is a child of the parent user or FALSE otherwise.
     */
    private boolean isChildUser(User user, User otherUser) {
        for (; otherUser != null && otherUser != user; otherUser = otherUser.getParent()) ;
        return otherUser == user;
    }

    public Set<User> getUsers() {
        Set<User> users = new HashSet<User>();
        for (Object itemId : myUserTree.getItemIds()) {
            users.add((User) itemId);
        }
        return users;
    }

    public class AddUserAction extends Action {
        public AddUserAction(User user) {
            super(getBundleString("userConfigPanel.action.addUser", user.getName()));
        }
    }

    public class RemoveUserAction extends Action {
        public RemoveUserAction(User user) {
            super(getBundleString("userConfigPanel.action.removeUser", user.getName()));
        }
    }

    public class MoveUserAction extends Action {
        public MoveUserAction(User user) {
            super(getBundleString("userConfigPanel.action.moveUser", user.getName()));
        }
    }

    void saveUser(User user) {
        if (!myUserTree.containsId(user)) {
            myUserTree.addItem(user);
            myUserTree.setChildrenAllowed(user, false);
            if (user.getParent() != null) {
                myUserTree.setChildrenAllowed(user.getParent(), true);
                myUserTree.setParent(user, user.getParent());
                myUserTree.expandItem(user.getParent());
            }
            myUserTree.select(user);
        }
    }
}