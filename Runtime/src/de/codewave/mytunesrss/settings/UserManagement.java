/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import com.intellij.uiDesigner.core.*;
import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.settings.UserManagement
 */
public class UserManagement {
    private JPanel myRootPanel;
    private JButton myCreateButton;
    private JPanel myUserPanel;
    private JScrollPane myScrollPane;
    private EditUserActionListener myEditUserActionListener = new EditUserActionListener();
    private DeleteUserActionListener myDeleteUserActionListener = new DeleteUserActionListener();

    public void init() {
        myScrollPane.getViewport().setOpaque(false);
        myCreateButton.addActionListener(new CreateUserActionListener());
        refreshUserList();
    }

    private void refreshUserList() {
        myUserPanel.removeAll();
        List<User> users = new ArrayList<User>(MyTunesRss.CONFIG.getUsers());
        Collections.sort(users, new Comparator<User>() {
            public int compare(User o1, User o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (User user : users) {
            addUser(user);
        }
        addPanelComponent(new JLabel(""), new GridBagConstraints(GridBagConstraints.RELATIVE,
                                                                 GridBagConstraints.RELATIVE,
                                                                 3,
                                                                 1,
                                                                 1.0,
                                                                 1.0,
                                                                 GridBagConstraints.WEST,
                                                                 GridBagConstraints.BOTH,
                                                                 new Insets(0, 0, 0, 0),
                                                                 0,
                                                                 0));
        myUserPanel.validate();
    }

    private void addUser(final User user) {
        GridBagConstraints gbcActive = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0, 0, GridBagConstraints.WEST,
                                                              GridBagConstraints.HORIZONTAL,
                                                              new Insets(5, 5, 0, 0),
                                                              0,
                                                              0);
        GridBagConstraints gbcName = new GridBagConstraints(GridBagConstraints.RELATIVE,
                                                            GridBagConstraints.RELATIVE,
                                                            1,
                                                            1,
                                                            1.0,
                                                            0,
                                                            GridBagConstraints.WEST,
                                                            GridBagConstraints.HORIZONTAL,
                                                            new Insets(5, 5, 0, 0),
                                                            0,
                                                            0);
        GridBagConstraints gbcEdit = new GridBagConstraints(GridBagConstraints.RELATIVE,
                                                            GridBagConstraints.RELATIVE,
                                                            1,
                                                            1,
                                                            0,
                                                            0,
                                                            GridBagConstraints.WEST,
                                                            GridBagConstraints.HORIZONTAL,
                                                            new Insets(5, 5, 0, 0),
                                                            0,
                                                            0);
        GridBagConstraints gbcDelete = new GridBagConstraints(GridBagConstraints.RELATIVE,
                                                              GridBagConstraints.RELATIVE,
                                                              GridBagConstraints.REMAINDER,
                                                              1,
                                                              0,
                                                              0,
                                                              GridBagConstraints.WEST,
                                                              GridBagConstraints.HORIZONTAL,
                                                              new Insets(5, 5, 0, 5),
                                                              0,
                                                              0);
        final JCheckBox active = new JCheckBox();
        active.setOpaque(false);
        active.setSelected(user.isActive());
        active.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                user.setActive(active.isSelected());
            }
        });
        active.setToolTipText(MyTunesRssUtils.getBundleString("settings.activateUserTooltip", user.getName()));
        addPanelComponent(active, gbcActive);
        JLabel name = new JLabel(user.getName());
        name.setOpaque(false);
        addPanelComponent(name, gbcName);
        JButton edit = new JButton(MyTunesRssUtils.getBundleString("settings.editUser"));
        edit.setToolTipText(MyTunesRssUtils.getBundleString("settings.editUserTooltip", user.getName()));
        edit.addActionListener(myEditUserActionListener);
        edit.setActionCommand(user.getName());
        edit.setOpaque(false);
        addPanelComponent(edit, gbcEdit);
        JButton delete = new JButton(MyTunesRssUtils.getBundleString("settings.deleteUser"));
        delete.setToolTipText(MyTunesRssUtils.getBundleString("settings.deleteUserTooltip", user.getName()));
        delete.addActionListener(myDeleteUserActionListener);
        delete.setActionCommand(user.getName());
        delete.setOpaque(false);
        addPanelComponent(delete, gbcDelete);
    }

    private void addPanelComponent(JComponent component, GridBagConstraints gbcName) {
        myUserPanel.add(component);
        ((GridBagLayout)myUserPanel.getLayout()).setConstraints(component, gbcName);
    }

    public class CreateUserActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (MyTunesRss.REGISTRATION.isRegistered() || MyTunesRss.CONFIG.getUsers().size() < MyTunesRssRegistration.UNREGISTERED_MAX_USERS) {
            new EditUser().display(MyTunesRss.ROOT_FRAME, null);
            refreshUserList();} else {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.unregisteredMaxUsers", MyTunesRssRegistration.UNREGISTERED_MAX_USERS));
            }
        }
    }

    public class EditUserActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            User user = MyTunesRss.CONFIG.getUser(e.getActionCommand());
            new EditUser().display(MyTunesRss.ROOT_FRAME, user);
            refreshUserList();
        }
    }

    public class DeleteUserActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            User user = MyTunesRss.CONFIG.getUser(e.getActionCommand());
            int result = JOptionPane.showConfirmDialog(myRootPanel,
                                                       MyTunesRssUtils.getBundleString("confirmation.deleteUser", user.getName()),
                                                       MyTunesRssUtils.getBundleString("confirmation.titleDeleteUser"),
                                                       JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                MyTunesRss.CONFIG.removeUser(user.getName());
            }
            refreshUserList();
        }
    }
}
