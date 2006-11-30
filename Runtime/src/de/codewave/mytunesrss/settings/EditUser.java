package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.components.*;
import org.apache.commons.lang.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2006<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 16.11.2006
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class EditUser {
    private JTextField myUserNameInput;
    private PasswordHashField myPasswordInput;
    private JPanel myRootPanel;
    private JButton mySaveButton;
    private JButton myCancelButton;
    private JCheckBox myPermRssInput;
    private JCheckBox myPermM3uInput;
    private JCheckBox myPermDownloadInput;
    private JCheckBox myPermUploadInput;
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
        if (myUser != null) {
            myPasswordInput.setPasswordHash(myUser.getPasswordHash());
        }
        if (myUser != null) {
            myPermRssInput.setSelected(myUser.isRss());
            myPermM3uInput.setSelected(myUser.isM3u());
            myPermDownloadInput.setSelected(myUser.isDownload());
            myPermUploadInput.setSelected(myUser.isUpload());
        }
        mySaveButton.addActionListener(new SaveButtonActionListener(dialog));
        myCancelButton.addActionListener(new CancelButtonActionListener(dialog));
    }

    private void createUIComponents() {
        myPasswordInput = new PasswordHashField(MyTunesRss.BUNDLE.getString("passwordHasBeenSet"), MyTunesRss.MESSAGE_DIGEST);
    }

    public class SaveButtonActionListener implements ActionListener {
        private JDialog myDialog;

        public SaveButtonActionListener(JDialog dialog) {
            myDialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            if (StringUtils.isEmpty(myUserNameInput.getText())) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.missingUserName"));
            } else if (myUser == null && myPasswordInput.getPasswordHash() == null) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.missingUserPassword"));
            } else if ((myUser == null || (!myUser.getName().equals(myUserNameInput.getText()))) && MyTunesRss.CONFIG.getUsers()
                    .contains(new User(myUserNameInput.getText()))) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.duplicateUserName", myUserNameInput.getText()));
            } else {
                if (myUser != null) {
                    if (myPasswordInput.getPasswordHash() != null) {
                        myUser.setPasswordHash(myPasswordInput.getPasswordHash());
                    }
                    // name change => remove user with old name
                    if (!myUser.getName().equals(myUserNameInput.getText())) {
                        MyTunesRss.CONFIG.removeUser(myUser.getName());
                    }
                } else {
                    myUser = new User("");
                    myUser.setPasswordHash(myPasswordInput.getPasswordHash());
                }
                myUser.setName(myUserNameInput.getText());
                myUser.setRss(myPermRssInput.isSelected());
                myUser.setM3u(myPermM3uInput.isSelected());
                myUser.setDownload(myPermDownloadInput.isSelected());
                myUser.setUpload(myPermUploadInput.isSelected());
                MyTunesRss.CONFIG.addUser(myUser);
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
            if (JOptionPane.showConfirmDialog(MyTunesRss.ROOT_FRAME,
                                              MyTunesRss.BUNDLE.getString("confirm.cancelEditUser"),
                                              MyTunesRss.BUNDLE.getString("confirm.cancelEditUserTitle"),
                                              JOptionPane.YES_NO_OPTION) == JOptionPane
                    .YES_OPTION) {
                myDialog.dispose();
            }
        }
    }
}



