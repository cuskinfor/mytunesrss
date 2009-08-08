/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * de.codewave.mytunesrss.settings.UserManagement
 */
public class UserManagement implements MyTunesRssEventListener, SettingsForm, DragSourceListener, DragGestureListener, DropTargetListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagement.class);
    private static final DataFlavor TRANSFERABLE_FLAVOR = new DataFlavor(User.class, "User");

    private JPanel myRootPanel;
    private JTree myUserTree;
    private JScrollPane myTreeScroller;
    private JScrollPane myEditScroller;
    private EditUser myEditUserForm;
    private JPopupMenu myUserPopupMenu = new JPopupMenu();
    private DragSource myDragSource;
    private ImageIcon myUserIcon = new ImageIcon(getClass().getResource("user.png"));
    private ImageIcon myGroupIcon = new ImageIcon(getClass().getResource("group.png"));

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
        myUserTree.addKeyListener(new UserTreeKeyListener());
        myUserTree.putClientProperty("JTree.lineStyle", "Angled");
        myDragSource = DragSource.getDefaultDragSource();
        DragGestureRecognizer dgr = myDragSource.createDefaultDragGestureRecognizer(myUserTree, DnDConstants.ACTION_MOVE, this);
        dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
        new DropTarget(myUserTree, this);
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
        myEditUserForm.save();
        List<User> userList = new ArrayList<User>();
        for (Enumeration<DefaultMutableTreeNode> nodeEnum = ((DefaultMutableTreeNode) myUserTree.getModel().getRoot()).preorderEnumeration(); nodeEnum.hasMoreElements();) {
            DefaultMutableTreeNode node = nodeEnum.nextElement();
            if (node.getUserObject() != null) {
                userList.add((User) node.getUserObject());
            }
        }
        List<User> validUserList = new ArrayList<User>();
        List<User> invalidUserList = new ArrayList<User>();
        while (!userList.isEmpty()) {
            User user = userList.remove(0);
            if (StringUtils.isBlank(user.getName())) {
                // missing user name
                invalidUserList.add(user);
            } else if (user.getPasswordHash() == null) {
                // missing password
                invalidUserList.add(user);
            } else if (userList.contains(user)) {
                // duplicate user name
                invalidUserList.add(user);
            } else if (StringUtils.length(user.getName()) > 30) {
                // user name too long
                invalidUserList.add(user);
            } else if (!MyTunesRssUtils.isNumberRange(user.getSessionTimeout(), 1, 1440)) {
                // illegal session timeout
                invalidUserList.add(user);
            } else if (user.getBandwidthLimit() != 0 && !MyTunesRssUtils.isNumberRange(user.getBandwidthLimit(), 10, 1024)) {
                // illegal bandwidth
                invalidUserList.add(user);
            } else if (user.getMaximumZipEntries() < 0) {
                // illegal max zip entries
                invalidUserList.add(user);
            } else if (user.getBytesQuota() < 0) {
                // illegal bytes quota
                invalidUserList.add(user);
            } else {
                // user valid
                validUserList.add(user);
            }
        }
        if (invalidUserList.isEmpty()) {
            MyTunesRss.CONFIG.replaceUsers(validUserList);
            return null;
        } else {
            return "TODO: errors"; // TODO
        }
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

    public void dragEnter(DragSourceDragEvent dsde) {
        // intentionally left blank
    }

    public void dragOver(DragSourceDragEvent dsde) {
        // intentionally left blank
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
        // intentionally left blank
    }

    public void dragExit(DragSourceEvent dse) {
        // intentionally left blank
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        // intentionally left blank
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        TreePath pathForLocation = myUserTree.getPathForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
        if (pathForLocation != null) {
            myEditUserForm.save();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
            Cursor cursor = DragSource.DefaultMoveNoDrop;
            myDragSource.startDrag(dge, cursor, new TransferableUser((User) node.getUserObject(), new TreePath(node.getPath())), this);
        }
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        // intentionally left blank
    }

    public void dragOver(DropTargetDragEvent dtde) {
        TreePath pathForLocation = myUserTree.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
        try {
            TransferableUser transferableUser = (TransferableUser) dtde.getTransferable().getTransferData(TRANSFERABLE_FLAVOR);
            if (pathForLocation == null || (!transferableUser.getTreePath().isDescendant(pathForLocation) && !pathForLocation.equals(transferableUser.getTreePath().getParentPath()))) {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                myUserTree.setCursor(DragSource.DefaultMoveDrop);
                return; // early return
            }
        } catch (UnsupportedFlavorException e) {
            LOGGER.error("Could not get transfer data from transferable.", e);
        } catch (IOException e) {
            LOGGER.error("Could not get transfer data from transferable.", e);
        }
        dtde.rejectDrag();
        myUserTree.setCursor(DragSource.DefaultMoveNoDrop);
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        // intentionally left blank
    }

    public void dragExit(DropTargetEvent dte) {
        // intentionally left blank
    }

    public void drop(DropTargetDropEvent dtde) {
        TreePath pathForLocation = myUserTree.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
        DefaultMutableTreeNode targetNode = pathForLocation != null ? (DefaultMutableTreeNode) pathForLocation.getLastPathComponent() : (DefaultMutableTreeNode) myUserTree.getModel().getRoot();
        DefaultTreeModel treeModel = (DefaultTreeModel) myUserTree.getModel();
        try {
            TransferableUser transferableUser = (TransferableUser) dtde.getTransferable().getTransferData(TRANSFERABLE_FLAVOR);
            DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode) transferableUser.getTreePath().getLastPathComponent();
            treeModel.removeNodeFromParent(sourceNode);
            treeModel.insertNodeInto(sourceNode, targetNode, targetNode.getChildCount());
            ((User) sourceNode.getUserObject()).setParent((User) targetNode.getUserObject());
            TreePath treePath = new TreePath(sourceNode.getPath());
            myUserTree.scrollPathToVisible(treePath);
            myUserTree.setSelectionPath(treePath);
            myEditUserForm.init(sourceNode, transferableUser.getUser());
        } catch (UnsupportedFlavorException e) {
            LOGGER.error("Could not drop tree node.", e);
        } catch (IOException e) {
            LOGGER.error("Could not drop tree node.", e);
        }
        dtde.getDropTargetContext().dropComplete(true);
    }

    private class UserTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            if (node.isRoot()) {
                setText("");
            } else {
                setIcon(node.isLeaf() ? myUserIcon : myGroupIcon);
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

    private class UserTreeKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            TreePath selectedPath = myUserTree.getSelectionPath();
            if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedPath != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                new RemoveUserActionListener(node).actionPerformed(null);
            }
        }
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
            myEditUserForm.getUserNameInput().requestFocusInWindow();
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

    private class TransferableUser implements Transferable {

        private User myUser;
        private TreePath myTreePath;

        private TransferableUser(User user, TreePath treePath) {
            myUser = user;
            myTreePath = treePath;
        }

        public User getUser() {
            return myUser;
        }

        public TreePath getTreePath() {
            return myTreePath;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{TRANSFERABLE_FLAVOR};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return TRANSFERABLE_FLAVOR.equals(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (TRANSFERABLE_FLAVOR.equals(flavor)) {
                return this;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
