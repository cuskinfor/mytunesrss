package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.components.*;

import javax.swing.*;
import javax.swing.event.*;
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
    private static final int MEGABYTE = 1024 * 1024;

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
    private JTextField myBytesQuotaInput;
    private JTextField myMaxZipEntriesInput;
    private JScrollPane myPermissionScrollPane;
    private JButton myResetHistoryButton;
    private JLabel myInfoReset;
    private JLabel myInfoDownBytes;
    private JLabel myInfoRemainBytes;
    private JLabel myInfoLimitHeading;
    private JButton myRefreshButton;
    private JPanel myInformationPanel;
    private JPanel myQuotaInfoPanel;
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
        myInformationPanel.setVisible(myUser != null);
        myPermissionScrollPane.getViewport().setPreferredSize(new Dimension(300, 150));
        myQuotaTypeInput.addItem(User.QuotaType.None);
        myQuotaTypeInput.addItem(User.QuotaType.Day);
        myQuotaTypeInput.addItem(User.QuotaType.Week);
        myQuotaTypeInput.addItem(User.QuotaType.Month);
        myQuotaTypeInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtils.enableElementAndLabel(myBytesQuotaInput, myQuotaTypeInput.getSelectedItem() != User.QuotaType.None);
                if (myUser != null) {
                    refreshInfo();
                }
            }
        });
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
            myBytesQuotaInput.setText(myUser.getBytesQuota() > 0 ? Long.toString(myUser.getBytesQuota() / MEGABYTE) : "");
            myMaxZipEntriesInput.setText(myUser.getMaximumZipEntries() > 0 ? Integer.toString(myUser.getMaximumZipEntries()) : "");
        } else {
            myQuotaTypeInput.setSelectedItem(User.QuotaType.None);
        }
        if (myQuotaTypeInput.getSelectedItem() == User.QuotaType.None) {
            SwingUtils.enableElementAndLabel(myBytesQuotaInput, false);
        }
        mySaveButton.addActionListener(new SaveButtonActionListener(dialog));
        myCancelButton.addActionListener(new SupportContact.CancelButtonActionListener(dialog));
        myResetHistoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myUser.setDownBytes(0);
                myUser.setResetTime(System.currentTimeMillis());
                refreshInfo();
            }
        });
        myRefreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshInfo();
            }
        });
        if (myUser != null) {
            refreshInfo();
            myBytesQuotaInput.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    refreshInfo();
                }

                public void removeUpdate(DocumentEvent e) {
                    refreshInfo();
                }

                public void changedUpdate(DocumentEvent e) {
                    // intentionally left blank
                }
            });
        }
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myUserNameInput, MyTunesRss.BUNDLE.getString("error.missingUserName")));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myPasswordInput,
                                                                           MyTunesRss.BUNDLE.getString("error.missingUserPassword")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myBytesQuotaInput,
                                                                              1,
                                                                              Long.MAX_VALUE,
                                                                              false,
                                                                              MyTunesRss.BUNDLE.getString("error.illegalBytesQuota")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myMaxZipEntriesInput,
                                                                              1,
                                                                              Integer.MAX_VALUE,
                                                                              true,
                                                                              MyTunesRss.BUNDLE.getString("error.illegalMaxZipEntries")));
        JTextFieldValidation.validateAll(myRootPanel);
    }

    private void refreshInfo() {
        myInfoReset.setText(new SimpleDateFormat(MyTunesRss.BUNDLE.getString("common.dateFormat")).format(new Date(myUser.getResetTime())));
        myInfoDownBytes.setText(MyTunesRssUtils.getMemorySizeForDisplay(myUser.getDownBytes()));
        myInfoDownBytes.setVisible(true);
        if (myQuotaTypeInput.getSelectedItem() != User.QuotaType.None && MyTunesRssUtils.getTextFieldInteger(myBytesQuotaInput, 0) > 0) {
            myInfoLimitHeading.setText(MyTunesRss.BUNDLE.getString("editUser.info.limitHeading"));
            myInfoRemainBytes.setText(MyTunesRssUtils.getMemorySizeForDisplay(Math.max(myUser.getBytesQuota() - myUser.getQuotaDownBytes(), 0)));
            myQuotaInfoPanel.setVisible(true);
        } else {
            myQuotaInfoPanel.setVisible(false);
        }
        myRootPanel.validate();
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
            String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
            if (messages != null) {
                MyTunesRssUtils.showErrorMessage(messages);
            } else {
                if ((myUser == null || (!myUser.getName().equals(myUserNameInput.getText()))) && MyTunesRss.CONFIG.getUsers()
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
                    myUser.setBytesQuota(MyTunesRssUtils.getTextFieldInteger(myBytesQuotaInput, 0) * MEGABYTE);
                    myUser.setMaximumZipEntries(MyTunesRssUtils.getTextFieldInteger(myMaxZipEntriesInput, 0));
                    MyTunesRss.CONFIG.addUser(myUser);
                    myDialog.dispose();
                }
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



