package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
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
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
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
    private JCheckBox myPermExternalSitesInput;
    private JTextField mySearchFuzzinessInput;
    private JCheckBox myPermEditTagsInput;
    private JPanel myForceTranscodersPanel;
    private JLabel myForceTranscodersLabel;
    private JPanel myInputsPanel;
    private User myUser;
    private DefaultMutableTreeNode myUserNode;
    private Timer myTimer = new Timer("EditUserRefreshTimer");
    private JTextPane myHelpLabel;

    public EditUser() {
        myScrollPane.getViewport().setOpaque(false);
        myScrollPane2.getViewport().setOpaque(false);
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
        myRemoveUserSettingFromProfileButton.setVisible(myUser != null);
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
        myTimer.schedule(new RefreshTask(), 1000);
        myHelpLabel.setVisible(false);
    }

    JPanel getRootPanel() {
        return myRootPanel;
    }

    User getUser() {
        return myUser;
    }

    JTextField getUserNameInput() {
        return myUserNameInput;
    }

    public DefaultMutableTreeNode getUserNode() {
        return myUserNode;
    }

    void init(DefaultMutableTreeNode userNode, User user) {
        myUserNode = userNode;
        myUser = user;
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
        fillPlaylistSelect();
        fillForceTranscoderPanel();
        initValues();
        myInputsPanel.setVisible(myUser != null);
        myHelpLabel.setVisible(myUser == null);
    }

    private void fillForceTranscoderPanel() {
        myForceTranscodersPanel.removeAll();
        List<TranscoderConfig> transcoderConfigs = MyTunesRss.CONFIG.getTranscoderConfigs();
        myForceTranscodersPanel.setVisible(transcoderConfigs != null && !transcoderConfigs.isEmpty());
        myForceTranscodersLabel.setVisible(transcoderConfigs != null && !transcoderConfigs.isEmpty());
        if (transcoderConfigs != null && !transcoderConfigs.isEmpty()) {
            myForceTranscodersPanel.setLayout(new GridLayoutManager(transcoderConfigs.size(), 1));
            Collections.sort(transcoderConfigs, new Comparator<TranscoderConfig>() {
                public int compare(TranscoderConfig o1, TranscoderConfig o2) {
                    return StringUtils.lowerCase(o1.getName()).compareTo(StringUtils.lowerCase(o2.getName()));
                }
            });
            int row = 0;
            for (TranscoderConfig transcoder : transcoderConfigs) {
                GridConstraints gc = new GridConstraints(row++,
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
                myForceTranscodersPanel.add(new JCheckBox(transcoder.getName()), gc);
            }
        }
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
        SwingUtils.enableElementAndLabel(myPermEditTagsInput, !parentUser);
    }

    private void initValues() {
        if (myUser != null) {
            myUserNameInput.setText(myUser.getName());
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
            mySearchFuzzinessInput.setText(myUser.getSearchFuzziness() > -1 ? Integer.toString(myUser.getSearchFuzziness()) : "");
            myPermEditTagsInput.setSelected(myUser.isEditTags());
            selectPlaylist(myUser.getPlaylistId());
            setParentUser(myUser.getParent() != null);
            if (myQuotaTypeInput.getSelectedItem() == User.QuotaType.None) {
                SwingUtils.enableElementAndLabel(myBytesQuotaInput, false);
            }
            Set<String> forceTranscoders = myUser.getForceTranscoders();
            for (Component transcoderCheckbox : myForceTranscodersPanel.getComponents()) {
                if (transcoderCheckbox instanceof JCheckBox) {
                    ((JCheckBox) transcoderCheckbox).setSelected(forceTranscoders.contains(((JCheckBox) transcoderCheckbox).getText()));
                }
            }
        } else {
            myUserNameInput.setText(null);
            myPasswordInput.setPasswordHash(null);
            myLastFmPasswordInput.setText(null);
            myPermRssInput.setSelected(false);
            myPermPlaylistInput.setSelected(false);
            myPermDownloadInput.setSelected(false);
            myPermUploadInput.setSelected(false);
            myPermPlayerInput.setSelected(false);
            myPermChangePasswordInput.setSelected(false);
            myPermSpecialPlaylists.setSelected(false);
            myPermEditSettings.setSelected(false);
            myPermCreatePlaylists.setSelected(false);
            myQuotaTypeInput.setSelectedItem(false);
            myBytesQuotaInput.setText(null);
            myMaxZipEntriesInput.setText(null);
            myFileTypesInput.setText(null);
            mySessionTimeoutInput.setText(null);
            myPermTranscoderInput.setSelected(false);
            myBandwidthLimit.setText(null);
            mySaveUserSettingsInput.setSelected(false);
            myLastFmUsernameInput.setText(null);
            myLastFmPasswordInput.setPasswordHash(null);
            myPermEditLastFMAccountInput.setSelected(false);
            myUrlEncryptionInput.setSelected(false);
            myEmailInput.setText(null);
            myPermChangeEmail.setSelected(false);
            myPermRemoteControlnput.setSelected(false);
            myPermExternalSitesInput.setSelected(false);
            myPermEditTagsInput.setSelected(false);
            setParentUser(false);
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
            if (playlists != null) {
                for (Playlist playlist = playlists.nextResult(); playlist != null; playlist = playlists.nextResult()) {
                    myRestrictionPlaylistInput.addItem(playlist);
                }
            }
            if (myUser != null) {
                selectPlaylist(myUser.getPlaylistId());
            }
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not query playlists from database.", e);
            }
        } finally {
            session.commit();
        }
    }

    private void selectPlaylist(String playlistId) {
        for (int i = 0; i < myRestrictionPlaylistInput.getItemCount(); i++) {
            if (myUser != null && StringUtils.isNotEmpty(myUser.getPlaylistId()) && myUser.getPlaylistId().equals(((Playlist) myRestrictionPlaylistInput.getItemAt(i)).getId())) {
                myRestrictionPlaylistInput.setSelectedIndex(i);
                return;
            }
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
                        initValues();
                        break;
                }
            }
        });
    }

    void save() {
        if (myUser != null) {
            if (myPasswordInput.getPasswordHash() != null) {
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
            myUser.setExternalSites(myPermExternalSitesInput.isSelected());
            myUser.setSearchFuzziness(MyTunesRssUtils.getTextFieldInteger(mySearchFuzzinessInput, -1));
            myUser.setEditTags(myPermEditTagsInput.isSelected());
            myUser.setPlaylistId(((Playlist) myRestrictionPlaylistInput.getSelectedItem()).getId());
            myUser.clearForceTranscoders();
            for (Component transcoderCheckbox : myForceTranscodersPanel.getComponents()) {
                if (transcoderCheckbox instanceof JCheckBox) {
                    if (((JCheckBox) transcoderCheckbox).isSelected()) {
                        myUser.addForceTranscoder(((JCheckBox) transcoderCheckbox).getText());
                    }
                }
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



