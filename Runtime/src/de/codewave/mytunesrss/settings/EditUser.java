package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.swing.SwingUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2006<br>
 * <b>Company:</b>       daGama Business Travel GmbH<br>
 * <b>Creation Date:</b> 16.11.2006
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class EditUser {
    private JTextField myUserNameInput;
    private JPasswordField myPasswordInput;
    private JPanel myRootPanel;
    private JButton mySaveButton;
    private JButton myCancelButton;
    private JCheckBox myPermRssInput;
    private JCheckBox myPermM3uInput;
    private JCheckBox myPermDownloadInput;
    private String myPassword;
    private User myUser;

    public void display(final JFrame parent, User user) {
        myUser = user;
        JDialog dialog = new JDialog(parent, MyTunesRss.BUNDLE.getString(user != null ? "editUser.editUserTitle" : "editUser.newUserTitle"), true);
        dialog.add(myRootPanel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        init(dialog);
        SwingUtils.packAndShowRelativeTo(dialog, parent);
    }

    private void init(JDialog dialog) {
        myUserNameInput.setText(myUser != null ? myUser.getName() : "");
        myPasswordInput.addFocusListener(new PasswordInputListener());
        if (myUser != null && myUser.getPasswordHash() != null && myUser.getPasswordHash().length > 0) {
            setPasswordVisible();
        } else {
            setPasswordHidden();
        }
        if (myUser != null) {
            myPermRssInput.setSelected(myUser.isRss());
            myPermM3uInput.setSelected(myUser.isM3u());
            myPermDownloadInput.setSelected(myUser.isDownload());
        }
        mySaveButton.addActionListener(new SaveButtonActionListener(dialog));
        myCancelButton.addActionListener(new CancelButtonActionListener(dialog));
    }

    private void setPasswordVisible() {
        myPasswordInput.setText("");
        myPasswordInput.setEchoChar((char)0);
        Font font = myPasswordInput.getFont();
        myPasswordInput.setFont(new Font(font.getName(), font.getStyle() | Font.ITALIC, font.getSize()));
        myPasswordInput.setForeground(Color.LIGHT_GRAY);
        myPasswordInput.setText(MyTunesRss.BUNDLE.getString("editUser.passwordHasBeenSet"));
    }

    private void setPasswordHidden() {
        myPasswordInput.setText("");
        myPasswordInput.setEchoChar('*');
        Font font = myPasswordInput.getFont();
        myPasswordInput.setFont(new Font(font.getName(), font.getStyle() & (Integer.MAX_VALUE - Font.ITALIC), font.getSize()));
        myPasswordInput.setForeground(Color.BLACK);
    }

    public class PasswordInputListener implements FocusListener {
        private boolean myPreviousPasswordSet;

        public void focusGained(FocusEvent focusEvent) {
            myPreviousPasswordSet = myPasswordInput.getPassword().length > 0;
            setPasswordHidden();
        }

        public void focusLost(FocusEvent focusEvent) {
            String password = new String(myPasswordInput.getPassword()).trim();
            if (StringUtils.isNotEmpty(password)) {
                myPassword = password;
                setPasswordVisible();
            } else if (myPreviousPasswordSet) {
                setPasswordVisible();
            }
        }
    }

    public class SaveButtonActionListener implements ActionListener {
        private JDialog myDialog;

        public SaveButtonActionListener(JDialog dialog) {
            myDialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            if (StringUtils.isEmpty(myUserNameInput.getText())) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.missingUserName"));
            } else if (myUser == null && StringUtils.isEmpty(myPassword)) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.missingUserPassword"));
            } else if ((myUser == null || (!myUser.getName().equals(myUserNameInput.getText()))) && MyTunesRss.CONFIG.getUsers()
                .contains(new User(myUserNameInput.getText()))) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.duplicateUserName", myUserNameInput.getText()));
            } else {
                if (myUser != null) {
                    if (StringUtils.isNotEmpty(myPassword)) {
                        myUser.setPassword(myPassword);
                    }
                    // name change => remove/change/add because of hash set in config
                    if (!myUser.getName().equals(myUserNameInput.getText())) {
                        MyTunesRss.CONFIG.removeUser(myUser.getName());
                        myUser.setName(myUserNameInput.getText());
                        MyTunesRss.CONFIG.addUser(myUser);
                    }
                } else {
                    myUser = new User(myUserNameInput.getText());
                    MyTunesRss.CONFIG.addUser(myUser);
                    myUser.setPassword(myPassword);
                }
                myUser.setRss(myPermRssInput.isSelected());
                myUser.setM3u(myPermM3uInput.isSelected());
                myUser.setDownload(myPermDownloadInput.isSelected());
                myDialog.dispose();
            }
        }
    }

    public class CancelButtonActionListener implements ActionListener {
        private JDialog myDialog;

        public CancelButtonActionListener(JDialog dialog) {
            myDialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("confirm.cancelEditUser"),
                                              MyTunesRss.BUNDLE.getString("confirm.cancelEditUserTitle"), JOptionPane.YES_NO_OPTION) == JOptionPane
                .YES_OPTION) {
                myDialog.dispose();
            }
        }
    }
}



