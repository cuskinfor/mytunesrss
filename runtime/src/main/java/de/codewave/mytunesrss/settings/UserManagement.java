/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.codewave.mytunesrss.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * de.codewave.mytunesrss.settings.UserManagement
 */
public class UserManagement implements MyTunesRssEventListener, SettingsForm {
    private JPanel myRootPanel;
    private JButton myCreateButton;
    private JPanel myUserPanel;
    private JScrollPane myScrollPane;
    private EditUserActionListener myEditUserActionListener = new EditUserActionListener();
    private DeleteUserActionListener myDeleteUserActionListener = new DeleteUserActionListener();

    public UserManagement() {
        myScrollPane.getViewport().setOpaque(false);
        myCreateButton.addActionListener(new CreateUserActionListener());
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void initValues() {
        refreshUserList();
    }

    public void setGuiMode(GuiMode mode) {
        // intentionally left blank
    }

    public String updateConfigFromGui() {
        // intentionally left blank
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.userManagement.title");
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (event == MyTunesRssEvent.CONFIGURATION_CHANGED) {
                    refreshUserList();
                }
            }
        });
    }

    private void refreshUserList() {
        myUserPanel.removeAll();
        List<User> users = new ArrayList<User>(MyTunesRss.CONFIG.getUsers());
        Collections.sort(users, new Comparator<User>() {
            public int compare(User o1, User o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        myUserPanel.setLayout(new GridLayoutManager(users.size() + 1, 4));
        int row = 0;
        for (User user : users) {
            addUser(user, row++);
        }
        addPanelComponent(new JLabel(""), new GridConstraints(row,
                                                              0,
                                                              1,
                                                              4,
                                                              GridConstraints.ANCHOR_WEST,
                                                              GridConstraints.FILL_BOTH,
                                                              GridConstraints.SIZEPOLICY_WANT_GROW,
                                                              GridConstraints.SIZEPOLICY_WANT_GROW,
                                                              null,
                                                              null,
                                                              null));
        myUserPanel.validate();
    }

    private void addUser(final User user, int row) {
        GridConstraints gbcActive = new GridConstraints(row,
                                                        0,
                                                        1,
                                                        1,
                                                        GridConstraints.ANCHOR_WEST,
                                                        GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_FIXED,
                                                        GridConstraints.SIZEPOLICY_FIXED,
                                                        null,
                                                        null,
                                                        null);
        GridConstraints gbcName = new GridConstraints(row,
                                                      1,
                                                      1,
                                                      1,
                                                      GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_WANT_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED,
                                                      null,
                                                      null,
                                                      null);
        GridConstraints gbcEdit = new GridConstraints(row,
                                                      2,
                                                      1,
                                                      1,
                                                      GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_FIXED,
                                                      GridConstraints.SIZEPOLICY_FIXED,
                                                      null,
                                                      null,
                                                      null);
        GridConstraints gbcDelete = new GridConstraints(row,
                                                        3,
                                                        1,
                                                        1,
                                                        GridConstraints.ANCHOR_WEST,
                                                        GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_FIXED,
                                                        GridConstraints.SIZEPOLICY_FIXED,
                                                        null,
                                                        null,
                                                        null);
        ;
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

    private void addPanelComponent(JComponent component, GridConstraints gridConstraints) {
        myUserPanel.add(component, gridConstraints);
    }

    public class CreateUserActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new EditUser().display(MyTunesRss.ROOT_FRAME, null);
            refreshUserList();
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
