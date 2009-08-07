/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * de.codewave.mytunesrss.settings.UserManagement
 */
public class UserManagement implements MyTunesRssEventListener, SettingsForm {
    private JPanel myRootPanel;
    private JTree myUserTree;
    private JScrollPane myTreeScroller;
    private JScrollPane myEditScroller;
    private EditUser myEditUserForm;
    private JPopupMenu myUserPopupMenu = new JPopupMenu();

    public UserManagement() {
        myTreeScroller.getViewport().setOpaque(false);
        myEditScroller.getViewport().setOpaque(false);
        MyTunesRssEventManager.getInstance().addListener(this);
        myUserTree.setRootVisible(false);
        DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        myUserTree.setSelectionModel(selectionModel);
        myUserTree.setCellRenderer(new UserTreeCellRenderer());
        myUserTree.addMouseListener(new UserTreeMouseListener());
    }

    public void initValues() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        addUsers(rootNode, MyTunesRss.CONFIG.getUsers(), null);
        myUserTree.setModel(new DefaultTreeModel(rootNode));
    }

    private void addUsers(DefaultMutableTreeNode node, Collection<User> users, User parent) {
        List<User> children = new ArrayList<User>();
        for (User user : users) {
            if ((parent == null && user.getParent() == null) || (parent != null && parent.equals(user.getParent()))) {
                children.add(user);
            }
        }
        Collections.sort(children, new Comparator<User>() {
            public int compare(User u1, User u2) {
                return StringUtils.trimToEmpty(u1.getName()).compareTo(StringUtils.trimToEmpty(u2.getName()));
            }
        });
        for (User child : children) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.clone());
            addUsers(childNode, users, child);
            node.add(childNode);
        }
    }

    public void setGuiMode(GuiMode mode) {
        // intentionally left blank
    }

    public String updateConfigFromGui() {
        // TODO validate and save complete user tree
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
                    initValues();
                }
            }
        });
    }

    private class UserTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (((DefaultMutableTreeNode) value).isRoot()) {
                setText("TODO: MyTunesRSS Users");
            } else {
                setText(((User) userObject).getName());
            }
            return this;
        }
    }

    private class UserTreeMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            handlePopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            handlePopup(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            handleSelectUser(e);
        }

        private void handleSelectUser(MouseEvent e) {
            TreePath pathForLocation = myUserTree.getPathForLocation(e.getX(), e.getY());
            String nameBefore = myEditUserForm.getUser() != null ? myEditUserForm.getUser().getName() : null;
            myEditUserForm.save();
            String nameAfter = myEditUserForm.getUser() != null ? myEditUserForm.getUser().getName() : null;
            if (!StringUtils.equals(nameBefore, nameAfter)) {
                ((DefaultTreeModel) myUserTree.getModel()).nodeChanged(myEditUserForm.getUserNode());
            }
            if (pathForLocation != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                myEditUserForm.init(node, (User) node.getUserObject());
            }
        }

        private void handlePopup(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                handleSelectUser(e);
                TreePath pathForLocation = myUserTree.getPathForLocation(e.getX(), e.getY());
                if (pathForLocation != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                    User user = (User) node.getUserObject();
                    if (user != null) {
                        myUserTree.setSelectionPath(pathForLocation);
                        myUserPopupMenu.removeAll();
                        myUserPopupMenu.add(createMenuItem("add child to " + user.getName(), new AddUserActionListener(node))); // TODO i18n
                        myUserPopupMenu.add(createMenuItem("remove " + user.getName(), new RemoveUserActionListener(node))); // TODO i18n
                        myUserPopupMenu.setLocation(myUserTree.getLocationOnScreen().x + e.getX(), myUserTree.getLocationOnScreen().y + e.getY());
                        myUserPopupMenu.setInvoker(myUserTree);
                        myUserPopupMenu.setVisible(true);
                    }
                } else {
                    myUserPopupMenu.removeAll();
                    myUserPopupMenu.add(createMenuItem("add new user", new AddUserActionListener(null))); // TODO i18n
                    myUserPopupMenu.setLocation(myUserTree.getLocationOnScreen().x + e.getX(), myUserTree.getLocationOnScreen().y + e.getY());
                    myUserPopupMenu.setInvoker(myUserTree);
                    myUserPopupMenu.setVisible(true);
                }
            }
        }
    }

    private JMenuItem createMenuItem(String text, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }

    private class AddUserActionListener implements ActionListener {
        private DefaultMutableTreeNode myParentNode;

        private AddUserActionListener(DefaultMutableTreeNode parentNode) {
            myParentNode = parentNode != null ? parentNode : (DefaultMutableTreeNode) myUserTree.getModel().getRoot();
        }

        public void actionPerformed(ActionEvent e) {
            User user = new User("new user"); // TODO i18n
            User parentUser = myParentNode != null ? (User) myParentNode.getUserObject() : null;
            user.setParent(parentUser);
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(user);
            ((DefaultTreeModel) myUserTree.getModel()).insertNodeInto(newNode, myParentNode, myParentNode.getChildCount());
            TreePath treePath = new TreePath(newNode.getPath());
            myUserTree.scrollPathToVisible(treePath);
            myUserTree.setSelectionPath(treePath);
            myEditUserForm.init(newNode, user);
        }
    }

    private class RemoveUserActionListener implements ActionListener {
        private DefaultMutableTreeNode myNode;

        private RemoveUserActionListener(DefaultMutableTreeNode node) {
            myNode = node;
        }

        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) myNode.getParent();
            ((DefaultTreeModel) myUserTree.getModel()).removeNodeFromParent(myNode);
            if (!parentNode.isRoot()) {
                TreePath treePath = new TreePath(parentNode.getPath());
                myUserTree.scrollPathToVisible(treePath);
                myUserTree.setSelectionPath(treePath);
            }
            myEditUserForm.init(parentNode, (User) parentNode.getUserObject());
        }
    }
}
