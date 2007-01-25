package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.components.*;
import org.apache.commons.lang.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

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
    private JComboBox myQuotaTypeInput;
    private JTextField myFileQuotaInput;
    private JTextField myBytesQuotaInput;
    private JTextField myMaxZipEntriesInput;
    private JScrollPane myPermissionScrollPane;
    private JButton myResetHistoryButton;
    private JLabel myInfoReset;
    private JLabel myInfoDownFiles;
    private JLabel myInfoDownBytes;
    private JLabel myInfoRemainFiles;
    private JLabel myInfoRemainBytes;
    private JLabel myInfoLimitHeading;
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
        myPermissionScrollPane.getViewport().setPreferredSize(new Dimension(300, 150));
        myQuotaTypeInput.addItem(User.QuotaType.None);
        myQuotaTypeInput.addItem(User.QuotaType.Day);
        myQuotaTypeInput.addItem(User.QuotaType.Week);
        myQuotaTypeInput.addItem(User.QuotaType.Month);
        myUserNameInput.setText(myUser != null ? myUser.getName() : "");
        if (myUser != null) {
            myPasswordInput.setPasswordHash(myUser.getPasswordHash());
        }
        if (myUser != null) {
            myPermRssInput.setSelected(myUser.isRss());
            myPermM3uInput.setSelected(myUser.isM3u());
            myPermDownloadInput.setSelected(myUser.isDownload());
            myPermUploadInput.setSelected(myUser.isUpload());
            myQuotaTypeInput.setSelectedItem(myUser.getQuotaType());
            myBytesQuotaInput.setText(myUser.getBytesQuota() > 0 ? Long.toString(myUser.getBytesQuota()) : "");
            myFileQuotaInput.setText(myUser.getFileQuota() > 0 ? Integer.toString(myUser.getFileQuota()) : "");
            myMaxZipEntriesInput.setText(myUser.getMaximumZipEntries() > 0 ? Integer.toString(myUser.getMaximumZipEntries()) : "");
        } else {
            myQuotaTypeInput.setSelectedItem(User.QuotaType.None);
        }
        if (myQuotaTypeInput.getSelectedItem() == User.QuotaType.None) {
            SwingUtils.enableElementAndLabel(myFileQuotaInput, false);
            SwingUtils.enableElementAndLabel(myBytesQuotaInput, false);
        }
        mySaveButton.addActionListener(new SaveButtonActionListener(dialog));
        myCancelButton.addActionListener(new CancelButtonActionListener(dialog));
        myInfoReset.setText(new SimpleDateFormat(MyTunesRss.BUNDLE.getString("common.dateFormat")).format(new Date(myUser.getResetTime())));
        myInfoDownBytes.setText(MyTunesRssUtils.getMemorySizeForDisplay(myUser.getDownBytes()));
        myInfoDownFiles.setText(DecimalFormat.getIntegerInstance().format(myUser.getDownFiles()));
        if (myUser.getQuotaType() != User.QuotaType.None && (myUser.getBytesQuota() > 0  || myUser.getFileQuota() > 0)) {
            myInfoLimitHeading.setText(MyTunesRss.BUNDLE.getString("editUser.info.limitHeading"));
            myInfoLimitHeading.setVisible(true);
        } else {
            myInfoLimitHeading.setVisible(false);
        }
        if (myUser.getBytesQuota() > 0) {
            myInfoRemainBytes.setText(MyTunesRssUtils.getMemorySizeForDisplay(myUser.getBytesQuota() - myUser.getQuotaDownBytes()));
            myInfoRemainBytes.setVisible(true);
        } else {
            myInfoRemainBytes.setVisible(false);
        }
        if (myUser.getFileQuota() > 0) {
            myInfoRemainFiles.setText(DecimalFormat.getIntegerInstance().format(myUser.getFileQuota() - myUser.getQuotaDownFiles()));
            myInfoRemainFiles.setVisible(true);
        } else {
            myInfoRemainFiles.setVisible(false);
        }
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
                myUser.setQuotaType((User.QuotaType)myQuotaTypeInput.getSelectedItem());
                try {
                    myUser.setBytesQuota(Long.parseLong(myBytesQuotaInput.getText()));
                    myUser.setQuotaDownBytes(Math.min(myUser.getQuotaDownBytes(), myUser.getBytesQuota()));
                } catch (NumberFormatException exception) {
                    myUser.setBytesQuota(0);
                }
                try {
                    myUser.setFileQuota(Integer.parseInt(myFileQuotaInput.getText()));
                    myUser.setQuotaDownFiles(Math.min(myUser.getQuotaDownFiles(), myUser.getFileQuota()));
                } catch (NumberFormatException exception) {
                    myUser.setFileQuota(0);
                }
                try {
                    myUser.setMaximumZipEntries(Integer.parseInt(myMaxZipEntriesInput.getText()));
                } catch (NumberFormatException exception) {
                    myUser.setMaximumZipEntries(0);
                }
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



