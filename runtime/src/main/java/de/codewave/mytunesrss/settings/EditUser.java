package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.components.PasswordHashField;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2006<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 16.11.2006
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class EditUser implements MyTunesRssEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(EditUser.class);
    private static final int MEGABYTE = 1024 * 1024;

    private JTextField myUserNameInput;
    private PasswordHashField myPasswordInput;
    private JPanel myRootPanel;
    private JButton mySaveButton;
    private JButton myCancelButton;
    private JCheckBox myPermRssInput;
    private JCheckBox myPermPlaylistInput;
    private JCheckBox myPermDownloadInput;
    private JCheckBox myPermUploadInput;
    private JComboBox myQuotaTypeInput;
    private JTextField myBytesQuotaInput;
    private JTextField myMaxZipEntriesInput;
    private JButton myResetHistoryButton;
    private JLabel myInfoReset;
    private JLabel myInfoDownBytes;
    private JLabel myInfoRemainBytes;
    private JPanel myInformationPanel;
    private JPanel myQuotaInfoPanel;
    private JButton myApplyButton;
    private JCheckBox myPermChangePasswordInput;
    private JTextField myFileTypesInput;
    private JTextField mySessionTimeoutInput;
    private JCheckBox myPermPlayerInput;
    private JCheckBox myPermSpecialPlaylists;
    private JScrollPane myScrollPane;
    private JCheckBox myPermTranscoderInput;
    private JTextField myBandwidthLimit;
    private JComboBox myRestrictionPlaylistInput;
    private JScrollPane myScrollPane2;
    private JCheckBox mySaveUserSettingsInput;
    private JCheckBox myPermEditSettings;
    private JCheckBox myPermCreatePlaylists;
    private JTextField myLastFmUsernameInput;
    private PasswordHashField myLastFmPasswordInput;
    private JCheckBox myPermEditLastFMAccountInput;
    private JCheckBox myUrlEncryptionInput;
    private JTextField myEmailInput;
    private JCheckBox myPermChangeEmail;
    private JButton myRemoveUserSettingFromProfileButton;
    private JCheckBox myPermRemoteControlnput;
    private JComboBox myParentUserInput;
    private JCheckBox myPermExternalSitesInput;
    private User myUser;
    private Timer myTimer = new Timer("EditUserRefreshTimer");


    public void display(final JFrame parent, User user) {
        myUser = user;
        DialogLayout layout = MyTunesRss.CONFIG.getDialogLayout(EditUser.class);
        final JDialog dialog = new JDialog(parent, MyTunesRssUtils.getBundleString(user != null ? "editUser.editUserTitle" : "editUser.newUserTitle"), true);
        dialog.getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                cancelDialog(dialog);
            }
        });
        dialog.add(myRootPanel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        init(dialog);
        myTimer.schedule(new RefreshTask(), 1000);
        MyTunesRssEventManager.getInstance().addListener(this);
        if (layout != null && layout.isValid()) {
            dialog.setLocation(layout.getX(), layout.getY());
            dialog.setSize(layout.getWidth(), layout.getHeight());
            dialog.setVisible(true);
        } else {
            SwingUtils.packAndShowRelativeTo(dialog, parent);
        }
        MyTunesRssEventManager.getInstance().removeListener(this);
    }

    private void closeDialog(JDialog dialog) {
        myTimer.cancel();
        DialogLayout layout = MyTunesRss.CONFIG.getDialogLayout(EditUser.class);
        if (layout == null) {
            layout = MyTunesRss.CONFIG.createDialogLayout(EditUser.class);
        }
        layout.setX((int) dialog.getLocation().getX());
        layout.setY((int) dialog.getLocation().getY());
        layout.setWidth((int) dialog.getSize().getWidth());
        layout.setHeight((int) dialog.getSize().getHeight());
    }

    private void init(JDialog dialog) {
        myScrollPane.getViewport().setOpaque(false);
        myScrollPane2.getViewport().setOpaque(false);
        myInformationPanel.setVisible(myUser != null);
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
        mySaveButton.addActionListener(new SaveButtonActionListener(dialog, true));
        myApplyButton.addActionListener(new SaveButtonActionListener(dialog, false));
        myCancelButton.addActionListener(new CancelButtonActionListener(dialog));
        myRemoveUserSettingFromProfileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myUser.setWebSettings(null);
                MyTunesRssUtils.showInfoMessage(MyTunesRssUtils.getBundleString("info.userSettingsRemovedFromProfile"));
            }
        });
        myResetHistoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myUser.setDownBytes(0);
                myUser.setResetTime(System.currentTimeMillis());
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
        } else {
            myApplyButton.setVisible(false);
        }
        JTextFieldValidation.setValidation(new CompositeTextFieldValidation(myUserNameInput, new NotEmptyTextFieldValidation(myUserNameInput,
                MyTunesRssUtils.getBundleString(
                        "error.missingUserName")),
                new MaxLengthTextFieldValidation(myUserNameInput,
                        30,
                        MyTunesRssUtils.getBundleString(
                                "error.userNameTooLong",
                                30))));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myPasswordInput, MyTunesRssUtils.getBundleString(
                "error.missingUserPassword")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myBytesQuotaInput,
                1,
                Long.MAX_VALUE,
                false,
                MyTunesRssUtils.getBundleString("error.illegalBytesQuota")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myMaxZipEntriesInput,
                1,
                Integer.MAX_VALUE,
                true,
                MyTunesRssUtils.getBundleString("error.illegalMaxZipEntries")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(mySessionTimeoutInput, 1, 1440, true, MyTunesRssUtils.getBundleString(
                "error.illegalSessionTimeout")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myBandwidthLimit, 10, 1024, true, MyTunesRssUtils.getBundleString(
                "error.illegalBandwidthLimit")));
        JTextFieldValidation.validateAll(myRootPanel);
        myParentUserInput.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (myParentUserInput.getSelectedIndex() > 0) {
                    initFromParent();
                } else {
                    setParentUser(false);
                }
            }
        });
        createParentUserList();
        initValues();
    }

    private void initFromParent() {
        User parent = MyTunesRss.CONFIG.getUser(myParentUserInput.getSelectedItem().toString());
        myPermRssInput.setSelected(parent.isRss());
        myPermPlaylistInput.setSelected(parent.isPlaylist());
        myPermDownloadInput.setSelected(parent.isDownload());
        myPermUploadInput.setSelected(parent.isUpload());
        myPermPlayerInput.setSelected(parent.isPlayer());
        myPermChangePasswordInput.setSelected(parent.isChangePassword());
        myPermSpecialPlaylists.setSelected(parent.isSpecialPlaylists());
        myPermEditSettings.setSelected(parent.isEditWebSettings());
        myPermCreatePlaylists.setSelected(parent.isCreatePlaylists());
        myQuotaTypeInput.setSelectedItem(parent.getQuotaType());
        myBytesQuotaInput.setText(parent.getBytesQuota() > 0 ? Long.toString(parent.getBytesQuota() / MEGABYTE) : "");
        myMaxZipEntriesInput.setText(parent.getMaximumZipEntries() > 0 ? Integer.toString(parent.getMaximumZipEntries()) : "");
        myFileTypesInput.setText(parent.getFileTypes());
        mySessionTimeoutInput.setText(Integer.toString(parent.getSessionTimeout()));
        myPermTranscoderInput.setSelected(parent.isTranscoder());
        myBandwidthLimit.setText(parent.getBandwidthLimit() > 0 ? Integer.toString(parent.getBandwidthLimit()) : "");
        myPermEditLastFMAccountInput.setSelected(parent.isEditLastFmAccount());
        myUrlEncryptionInput.setSelected(parent.isUrlEncryption());
        myPermChangeEmail.setSelected(parent.isChangeEmail());
        myPermRemoteControlnput.setSelected(parent.isRemoteControl());
        mySaveUserSettingsInput.setSelected(parent.isSaveWebSettings());
        myPermExternalSitesInput.setSelected(parent.isExternalSites());
        if (parent.getPlaylistId() == null) {
            myRestrictionPlaylistInput.setSelectedIndex(0);
        } else {
            for (int i = 0; i < myRestrictionPlaylistInput.getItemCount(); i++) {
                if (parent.getPlaylistId().equals(((Playlist) myRestrictionPlaylistInput.getItemAt(i)).getId())) {
                    myRestrictionPlaylistInput.setSelectedIndex(i);
                    break;
                }
            }
        }
        setParentUser(true);
    }

    private void setParentUser(boolean parentUser) {
        SwingUtils.enableElementAndLabel(myPermRssInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermPlaylistInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermDownloadInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermUploadInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermPlayerInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermChangePasswordInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermSpecialPlaylists, !parentUser);
        SwingUtils.enableElementAndLabel(myPermEditSettings, !parentUser);
        SwingUtils.enableElementAndLabel(myPermCreatePlaylists, !parentUser);
        SwingUtils.enableElementAndLabel(myQuotaTypeInput, !parentUser);
        SwingUtils.enableElementAndLabel(myBytesQuotaInput, !parentUser && myQuotaTypeInput.getSelectedItem() != User.QuotaType.None);
        SwingUtils.enableElementAndLabel(myMaxZipEntriesInput, !parentUser);
        SwingUtils.enableElementAndLabel(myFileTypesInput, !parentUser);
        SwingUtils.enableElementAndLabel(mySessionTimeoutInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermTranscoderInput, !parentUser);
        SwingUtils.enableElementAndLabel(myBandwidthLimit, !parentUser);
        SwingUtils.enableElementAndLabel(myPermEditLastFMAccountInput, !parentUser);
        SwingUtils.enableElementAndLabel(myUrlEncryptionInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermChangeEmail, !parentUser);
        SwingUtils.enableElementAndLabel(myPermRemoteControlnput, !parentUser);
        SwingUtils.enableElementAndLabel(mySaveUserSettingsInput, !parentUser);
        SwingUtils.enableElementAndLabel(myRestrictionPlaylistInput, !parentUser);
        SwingUtils.enableElementAndLabel(myPermExternalSitesInput, !parentUser);
    }

    private void createParentUserList() {
        myParentUserInput.removeAllItems();
        myParentUserInput.addItem(MyTunesRssUtils.getBundleString("settings.noParentUser"));
        List<String> usernames = new ArrayList<String>();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            if (user != myUser) {
                usernames.add(user.getName());
            }
        }
        Collections.sort(usernames);
        for (String username : usernames) {
            myParentUserInput.addItem(username);
        }
    }

    private void initValues() {
        fillPlaylistSelect();
        myUserNameInput.setText(myUser != null ? myUser.getName() : "");
        myParentUserInput.setSelectedIndex(0);
        if (myUser != null) {
            myPasswordInput.setPasswordHash(myUser.getPasswordHash());
            myLastFmPasswordInput.setText(myUser.getLastFmUsername());
            myPermRssInput.setSelected(myUser.isRss());
            myPermPlaylistInput.setSelected(myUser.isPlaylist());
            myPermDownloadInput.setSelected(myUser.isDownload());
            myPermUploadInput.setSelected(myUser.isUpload());
            myPermPlayerInput.setSelected(myUser.isPlayer());
            myPermChangePasswordInput.setSelected(myUser.isChangePassword());
            myPermSpecialPlaylists.setSelected(myUser.isSpecialPlaylists());
            myPermEditSettings.setSelected(myUser.isEditWebSettings());
            myPermCreatePlaylists.setSelected(myUser.isCreatePlaylists());
            myQuotaTypeInput.setSelectedItem(myUser.getQuotaType());
            myBytesQuotaInput.setText(myUser.getBytesQuota() > 0 ? Long.toString(myUser.getBytesQuota() / MEGABYTE) : "");
            myMaxZipEntriesInput.setText(myUser.getMaximumZipEntries() > 0 ? Integer.toString(myUser.getMaximumZipEntries()) : "");
            myFileTypesInput.setText(myUser.getFileTypes());
            mySessionTimeoutInput.setText(Integer.toString(myUser.getSessionTimeout()));
            myPermTranscoderInput.setSelected(myUser.isTranscoder());
            myBandwidthLimit.setText(myUser.getBandwidthLimit() > 0 ? Integer.toString(myUser.getBandwidthLimit()) : "");
            mySaveUserSettingsInput.setSelected(myUser.isSaveWebSettings());
            myLastFmUsernameInput.setText(myUser.getLastFmUsername());
            myLastFmPasswordInput.setPasswordHash(myUser.getLastFmPasswordHash());
            myPermEditLastFMAccountInput.setSelected(myUser.isEditLastFmAccount());
            myUrlEncryptionInput.setSelected(myUser.isUrlEncryption());
            myEmailInput.setText(myUser.getEmail());
            myPermChangeEmail.setSelected(myUser.isChangeEmail());
            myPermRemoteControlnput.setSelected(myUser.isRemoteControl());
            myPermExternalSitesInput.setSelected(myUser.isExternalSites());
            if (StringUtils.isNotEmpty(myUser.getParentUserName())) {
                myParentUserInput.setSelectedItem(myUser.getParentUserName());
                initFromParent();
            } else {
                setParentUser(false);
            }
        } else {
            setParentUser(false);
            myQuotaTypeInput.setSelectedItem(User.QuotaType.None);
            myPermRssInput.setSelected(true);
            myPermPlaylistInput.setSelected(true);
            myPermPlayerInput.setSelected(true);
            myPermChangePasswordInput.setSelected(true);
            myPermSpecialPlaylists.setSelected(true);
            myPermEditSettings.setSelected(true);
            myPermCreatePlaylists.setSelected(true);
            mySessionTimeoutInput.setText("10");
            myPermEditLastFMAccountInput.setSelected(true);
            myUrlEncryptionInput.setSelected(true);
        }
        if (myQuotaTypeInput.getSelectedItem() == User.QuotaType.None) {
            SwingUtils.enableElementAndLabel(myBytesQuotaInput, false);
        }
    }

    private void fillPlaylistSelect() {
        DataStoreQuery.QueryResult<Playlist> playlists = null;
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            playlists = session.executeQuery(new FindPlaylistQuery(null, null, null, true));
            myRestrictionPlaylistInput.removeAllItems();
            myRestrictionPlaylistInput.addItem(new Playlist() {
                @Override
                public String toString() {
                    return MyTunesRssUtils.getBundleString("editUser.noPlaylist");
                }
            });
            int index = 1;
            if (playlists != null) {
                for (Playlist playlist = playlists.nextResult(); playlist != null; playlist = playlists.nextResult()) {
                    myRestrictionPlaylistInput.addItem(playlist);
                    if (myUser != null && StringUtils.isNotEmpty(myUser.getPlaylistId()) && myUser.getPlaylistId().equals(playlist.getId())) {
                        myRestrictionPlaylistInput.setSelectedIndex(index);
                    }
                    index++;
                }
            }
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not query playlists from database.", e);
            }
        } finally {
            session.commit();
        }
    }

    private void refreshInfo() {
        myInfoReset.setText(new SimpleDateFormat(MyTunesRssUtils.getBundleString("common.dateFormat")).format(new Date(myUser.getResetTime())));
        myInfoDownBytes.setText(MyTunesRssUtils.getMemorySizeForDisplay(myUser.getDownBytes()));
        myInfoDownBytes.setVisible(true);
        if (myUser.getQuotaType() != User.QuotaType.None) {
            myInfoRemainBytes.setText(MyTunesRssUtils.getMemorySizeForDisplay(Math.max(myUser.getBytesQuota() - myUser.getQuotaDownBytes(), 0)));
            myQuotaInfoPanel.setVisible(true);
        } else {
            myQuotaInfoPanel.setVisible(false);
        }
        myRootPanel.validate();
    }

    private void createUIComponents() {
        myPasswordInput = new PasswordHashField(MyTunesRssUtils.getBundleString("passwordHasBeenSet"), MyTunesRss.SHA1_DIGEST);
        myLastFmPasswordInput = new PasswordHashField(MyTunesRssUtils.getBundleString("passwordHasBeenSet"), MyTunesRss.MD5_DIGEST);
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event) {
                    case CONFIGURATION_CHANGED:
                        createParentUserList();
                        initValues();
                        break;
                }
            }
        });
    }

    public class SaveButtonActionListener implements ActionListener {
        private JDialog myDialog;
        private boolean myClose;

        public SaveButtonActionListener(JDialog dialog, boolean closeAfterSave) {
            myDialog = dialog;
            myClose = closeAfterSave;
        }

        public void actionPerformed(ActionEvent e) {
            String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
            if (messages != null) {
                MyTunesRssUtils.showErrorMessage(messages);
            } else {
                if ((myUser == null || (!myUser.getName().equals(myUserNameInput.getText()))) && MyTunesRss.CONFIG.getUsers().contains(new User(
                        myUserNameInput.getText()))) {
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
                    myUser.setPlaylist(myPermPlaylistInput.isSelected());
                    myUser.setDownload(myPermDownloadInput.isSelected());
                    myUser.setUpload(myPermUploadInput.isSelected());
                    myUser.setPlayer(myPermPlayerInput.isSelected());
                    myUser.setChangePassword(myPermChangePasswordInput.isSelected());
                    myUser.setSpecialPlaylists(myPermSpecialPlaylists.isSelected());
                    myUser.setCreatePlaylists(myPermCreatePlaylists.isSelected());
                    myUser.setEditWebSettings(myPermEditSettings.isSelected());
                    myUser.setQuotaType((User.QuotaType) myQuotaTypeInput.getSelectedItem());
                    myUser.setBytesQuota(MyTunesRssUtils.getTextFieldInteger(myBytesQuotaInput, 0) * MEGABYTE);
                    myUser.setMaximumZipEntries(MyTunesRssUtils.getTextFieldInteger(myMaxZipEntriesInput, 0));
                    myUser.setFileTypes(myFileTypesInput.getText());
                    myUser.setSessionTimeout(MyTunesRssUtils.getTextFieldInteger(mySessionTimeoutInput, 10));
                    myUser.setTranscoder(myPermTranscoderInput.isSelected());
                    myUser.setBandwidthLimit(MyTunesRssUtils.getTextFieldInteger(myBandwidthLimit, 0));
                    myUser.setEmail(myEmailInput.getText());
                    myUser.setChangeEmail(myPermChangeEmail.isSelected());
                    if (myRestrictionPlaylistInput.getSelectedItem() != null) {
                        myUser.setPlaylistId(((Playlist) myRestrictionPlaylistInput.getSelectedItem()).getId());
                    } else {
                        myUser.setPlaylistId(null);
                    }
                    myUser.setSaveWebSettings(mySaveUserSettingsInput.isSelected());
                    myUser.setLastFmUsername(myLastFmUsernameInput.getText());
                    myUser.setLastFmPasswordHash(myLastFmPasswordInput.getPasswordHash());
                    myUser.setEditLastFmAccount(myPermEditLastFMAccountInput.isSelected());
                    myUser.setUrlEncryption(myUrlEncryptionInput.isSelected());
                    myUser.setRemoteControl(myPermRemoteControlnput.isSelected());
                    myUser.setParentUserName(myParentUserInput.getSelectedIndex() == 0 ? null : myParentUserInput.getSelectedItem().toString());
                    myUser.setExternalSites(myPermExternalSitesInput.isSelected());
                    MyTunesRss.CONFIG.addUser(myUser);
                    if (myClose) {
                        myDialog.dispose();
                    }
                }
            }
            closeDialog(myDialog);
        }
    }

    private void cancelDialog(JDialog dialog) {
        if (JOptionPane.showConfirmDialog(MyTunesRss.ROOT_FRAME,
                MyTunesRssUtils.getBundleString("confirm.cancelEditUser"),
                MyTunesRssUtils.getBundleString("confirm.cancelEditUserTitle"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            closeDialog(dialog);
            dialog.dispose();
        }
    }

    public class CancelButtonActionListener implements ActionListener {
        private JDialog myDialog;

        public CancelButtonActionListener(JDialog dialog) {
            myDialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            cancelDialog(myDialog);
        }
    }

    public class RefreshTask extends TimerTask {
        public void run() {
            if (myUser != null) {
                refreshInfo();
            }
            try {
                myTimer.schedule(new RefreshTask(), 1000);
            } catch (IllegalStateException e) {
                // timer was cancelled, so we just don't schedule any further tasks
            }
        }
    }
}



