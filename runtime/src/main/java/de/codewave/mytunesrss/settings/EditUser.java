package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.components.PasswordHashField;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2006<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 16.11.2006
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class EditUser implements MyTunesRssEventListener {
    private static final Log LOG = LogFactory.getLog(EditUser.class);
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
    private JPanel myRestrictionsPanel;
    private JCheckBox myPermSpecialPlaylists;
    private JPanel myPermissionsPanel;
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
    private JLabel myLastFmUsernameLabel;
    private JLabel myLastFmPasswordLabel;
    private User myUser;
    private Timer myTimer = new Timer("EditUserRefreshTimer");


    public void display(final JFrame parent, User user) {
        myUser = user;
        JDialog dialog = new JDialog(parent,
                                     MyTunesRssUtils.getBundleString(user != null ? "editUser.editUserTitle" : "editUser.newUserTitle"),
                                     true);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                myTimer.cancel();
            }
        });
        dialog.add(myRootPanel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        init(dialog);
        myTimer.schedule(new RefreshTask(), 1000);
        MyTunesRssEventManager.getInstance().addListener(this);
        SwingUtils.packAndShowRelativeTo(dialog, parent);
        MyTunesRssEventManager.getInstance().removeListener(this);
    }

    private void init(JDialog dialog) {
        initRegistration();
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
        myCancelButton.addActionListener(new SupportContact.CancelButtonActionListener(dialog));
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
        JTextFieldValidation.setValidation(new CompositeTextFieldValidation(myUserNameInput,
                                                                            new NotEmptyTextFieldValidation(myUserNameInput,
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
        initValues();
    }

    private void initValues() {
        fillPlaylistSelect();
        myUserNameInput.setText(myUser != null ? myUser.getName() : "");
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
        } else {
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
        }
        if (myQuotaTypeInput.getSelectedItem() == User.QuotaType.None) {
            SwingUtils.enableElementAndLabel(myBytesQuotaInput, false);
        }
    }

    private void fillPlaylistSelect() {
        DataStoreQuery.QueryResult<Playlist> playlists = null;
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            playlists = session.executeQuery(new FindPlaylistQuery(null, null, true));
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

    private void initRegistration() {
        myPermUploadInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myPermChangePasswordInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myPermPlayerInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myRestrictionsPanel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myQuotaInfoPanel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myPermTranscoderInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myBandwidthLimit.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myPermEditLastFMAccountInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myLastFmUsernameLabel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myLastFmUsernameInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myLastFmPasswordLabel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myLastFmPasswordInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
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
                    myUser.setPlaylist(myPermPlaylistInput.isSelected());
                    myUser.setDownload(myPermDownloadInput.isSelected());
                    myUser.setUpload(myPermUploadInput.isSelected());
                    myUser.setPlayer(myPermPlayerInput.isSelected());
                    myUser.setChangePassword(myPermChangePasswordInput.isSelected());
                    myUser.setSpecialPlaylists(myPermSpecialPlaylists.isSelected());
                    myUser.setCreatePlaylists(myPermCreatePlaylists.isSelected());
                    myUser.setEditWebSettings(myPermEditSettings.isSelected());
                    myUser.setQuotaType((User.QuotaType)myQuotaTypeInput.getSelectedItem());
                    myUser.setBytesQuota(MyTunesRssUtils.getTextFieldInteger(myBytesQuotaInput, 0) * MEGABYTE);
                    myUser.setMaximumZipEntries(MyTunesRssUtils.getTextFieldInteger(myMaxZipEntriesInput, 0));
                    myUser.setFileTypes(myFileTypesInput.getText());
                    myUser.setSessionTimeout(MyTunesRssUtils.getTextFieldInteger(mySessionTimeoutInput, 10));
                    myUser.setTranscoder(myPermTranscoderInput.isSelected());
                    myUser.setBandwidthLimit(MyTunesRssUtils.getTextFieldInteger(myBandwidthLimit, 0));
                    if (myRestrictionPlaylistInput.getSelectedItem() != null) {
                        myUser.setPlaylistId(((Playlist)myRestrictionPlaylistInput.getSelectedItem()).getId());
                    } else {
                        myUser.setPlaylistId(null);
                    }
                    myUser.setSaveWebSettings(mySaveUserSettingsInput.isSelected());
                    myUser.setLastFmUsername(myLastFmUsernameInput.getText());
                    myUser.setLastFmPasswordHash(myLastFmPasswordInput.getPasswordHash());
                    myUser.setEditLastFmAccount(myPermEditLastFMAccountInput.isSelected());
                    MyTunesRss.CONFIG.addUser(myUser);
                    if (myClose) {
                        myDialog.dispose();
                    }
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
                                              MyTunesRssUtils.getBundleString("confirm.cancelEditUser"),
                                              MyTunesRssUtils.getBundleString("confirm.cancelEditUserTitle"),
                                              JOptionPane.YES_NO_OPTION) == JOptionPane
                    .YES_OPTION) {
                myDialog.dispose();
            }
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



