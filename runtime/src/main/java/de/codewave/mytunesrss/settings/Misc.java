/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jmx.MyTunesRssJmxUtils;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.MinMaxValueTextFieldValidation;
import de.codewave.utils.swing.NotEmptyTextFieldValidation;
import de.codewave.utils.swing.SwingUtils;
import de.codewave.utils.swing.components.PasswordHashField;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * de.codewave.mytunesrss.settings.Misc
 */
public class Misc implements MyTunesRssEventListener, SettingsForm {
    private static final Logger LOGGER = LoggerFactory.getLogger(Misc.class);

    private JPanel myRootPanel;
    private JTextField myUsernameInput;
    private PasswordHashField myPasswordInput;
    private JTextField myProxyHostInput;
    private JTextField myProxyPortInput;
    private JCheckBox myUseProxyInput;
    private JLabel myPasswordLabel;
    private JCheckBox myQuitConfirmationInput;
    private JCheckBox myUpdateOnStartInput;
    private JButton myProgramUpdateButton;
    private JLabel myMyTunesRssComStatus;
    private JTextField myWelcomeMessageInput;
    private JTextField myMailHostInput;
    private JTextField myMailPortInput;
    private JTextField myMailLoginInput;
    private JPasswordField myMailPasswordInput;
    private JTextField myMailSenderInput;
    private JTextField myJmxHostInput;
    private JTextField myJmxPortInput;
    private JTextField myJmxUserNameInput;
    private JPasswordField myJmxPasswordInput;
    private JCheckBox myMyTunesRssComSsl;
    private boolean myUpdateOnStartInputCache;
    private boolean myAutoStartServer;

    public Misc() {
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void init() {
        initValues();
        myUseProxyInput.addActionListener(new UseProxyActionListener());
        myProgramUpdateButton.addActionListener(new ProgramUpdateButtonListener());
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myProxyHostInput,
                                                                           MyTunesRssUtils.getBundleString("error.emptyProxyHost")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myProxyPortInput, 1, 65535, false, MyTunesRssUtils.getBundleString(
                "error.illegalProxyPort")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myJmxPortInput, 1, 65535, false, MyTunesRssUtils.getBundleString(
                "error.illegalJmxPort")));
    }

    private void initValues() {
        myUsernameInput.setText(MyTunesRss.CONFIG.getMyTunesRssComUser());
        myPasswordInput.setPasswordHash(MyTunesRss.CONFIG.getMyTunesRssComPasswordHash());
        myUseProxyInput.setSelected(MyTunesRss.CONFIG.isProxyServer());
        SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
        SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
        myProxyHostInput.setText(MyTunesRss.CONFIG.getProxyHost());
        int port = MyTunesRss.CONFIG.getProxyPort();
        myProxyPortInput.setText(MyTunesRssUtils.getValueString(port, 1, 65535, null));
        myQuitConfirmationInput.setSelected(MyTunesRss.CONFIG.isQuitConfirmation());
        myUpdateOnStartInput.setSelected(MyTunesRss.CONFIG.isCheckUpdateOnStart());
        myUpdateOnStartInputCache = myUpdateOnStartInput.isSelected();
        myMyTunesRssComStatus.setText(MyTunesRssUtils.getBundleString("mytunesrsscom.stateUnknown"));
        myWelcomeMessageInput.setText(MyTunesRss.CONFIG.getWebWelcomeMessage());
        myMailHostInput.setText(MyTunesRss.CONFIG.getMailHost());
        myMailPortInput.setText(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getMailPort(), 1, 65535, ""));
        myMailLoginInput.setText(MyTunesRss.CONFIG.getMailLogin());
        myMailPasswordInput.setText(MyTunesRss.CONFIG.getMailPassword());
        myMailSenderInput.setText(MyTunesRss.CONFIG.getMailSender());
        myJmxHostInput.setText(MyTunesRss.CONFIG.getJmxHost());
        myJmxPortInput.setText(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getJmxPort(), 1, 65535, null));
        myJmxUserNameInput.setText(MyTunesRss.CONFIG.getJmxUser());
        myJmxPasswordInput.setText(MyTunesRss.CONFIG.getJmxPassword());
        myMyTunesRssComSsl.setSelected(MyTunesRss.CONFIG.isMyTunesRssComSsl());
    }

    private void createUIComponents() {
        myPasswordInput = new PasswordHashField(MyTunesRssUtils.getBundleString("passwordHasBeenSet"), MyTunesRss.SHA1_DIGEST);
    }

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        SwingUtils.enableElementAndLabel(myProxyHostInput, !serverActive && myUseProxyInput.isSelected());
        SwingUtils.enableElementAndLabel(myProxyPortInput, !serverActive && myUseProxyInput.isSelected());
        myUseProxyInput.setEnabled(!serverActive);
        SwingUtils.enableElementAndLabel(myUsernameInput, !serverActive);
        myPasswordLabel.setEnabled(!serverActive);
        myPasswordInput.setEnabled(!serverActive);
        myMyTunesRssComSsl.setEnabled(!serverActive);
        myUpdateOnStartInput.setEnabled(!serverActive && !myAutoStartServer);
        myProgramUpdateButton.setEnabled(!serverActive);
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            MyTunesRss.CONFIG.setMyTunesRssComUser(myUsernameInput.getText());
            if (myPasswordInput.getPasswordHash() != null) {
                MyTunesRss.CONFIG.setMyTunesRssComPasswordHash(myPasswordInput.getPasswordHash());
            }
            MyTunesRss.CONFIG.setMyTunesRssComSsl(myMyTunesRssComSsl.isSelected());
            MyTunesRss.CONFIG.setProxyServer(myUseProxyInput.isSelected());
            MyTunesRss.CONFIG.setProxyHost(myProxyHostInput.getText());
            MyTunesRss.CONFIG.setProxyPort(MyTunesRssUtils.getTextFieldInteger(myProxyPortInput, -1));
            MyTunesRss.CONFIG.setQuitConfirmation(myQuitConfirmationInput.isSelected());
            MyTunesRss.CONFIG.setCheckUpdateOnStart(myUpdateOnStartInput.isSelected());
            MyTunesRss.CONFIG.setWebWelcomeMessage(myWelcomeMessageInput.getText());
            MyTunesRss.CONFIG.setMailHost(myMailHostInput.getText());
            MyTunesRss.CONFIG.setMailPort(MyTunesRssUtils.getTextFieldInteger(myMailPortInput, -1));
            MyTunesRss.CONFIG.setMailLogin(myMailLoginInput.getText());
            MyTunesRss.CONFIG.setMailPassword(new String(myMailPasswordInput.getPassword()));
            MyTunesRss.CONFIG.setMailSender(new String(myMailSenderInput.getText()));
            String newJmxHost = MyTunesRssUtils.getTextFieldString(myJmxHostInput, "0.0.0.0", true);
            int newJmxPort = MyTunesRssUtils.getTextFieldInteger(myJmxPortInput, -1);
            String newJmxUser = myJmxUserNameInput.getText();
            String newJmxPassword = new String(myJmxPasswordInput.getPassword());
            boolean jmxChanges = !StringUtils.equalsIgnoreCase(newJmxHost, MyTunesRss.CONFIG.getJmxHost());
            jmxChanges |= !StringUtils.equals(newJmxUser, MyTunesRss.CONFIG.getJmxUser());
            jmxChanges |= !StringUtils.equals(newJmxPassword, MyTunesRss.CONFIG.getJmxPassword());
            jmxChanges |= newJmxPort != MyTunesRss.CONFIG.getJmxPort();
            MyTunesRss.CONFIG.setJmxHost(newJmxHost);
            MyTunesRss.CONFIG.setJmxPort(newJmxPort);
            MyTunesRss.CONFIG.setJmxUser(newJmxUser);
            MyTunesRss.CONFIG.setJmxPassword(newJmxPassword);
            if (jmxChanges) {
                LOGGER.debug("Restarting JMX server because settings have changed.");
                MyTunesRssJmxUtils.stopJmxServer();
                MyTunesRssJmxUtils.startJmxServer();
            }
        }
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event) {
                    case ENABLE_AUTO_START_SERVER:
                        myUpdateOnStartInputCache = myUpdateOnStartInput.isSelected();
                        myUpdateOnStartInput.setSelected(false);
                        myUpdateOnStartInput.setEnabled(false);
                        myAutoStartServer = true;
                        break;
                    case DISABLE_AUTO_START_SERVER:
                        myUpdateOnStartInput.setSelected(myUpdateOnStartInputCache);
                        myUpdateOnStartInput.setEnabled(true);
                        myAutoStartServer = false;
                        break;
                    case MYTUNESRSS_COM_UPDATED:
                        myMyTunesRssComStatus.setText(MyTunesRssUtils.getBundleString(event.getMessageKey(), event.getMessageParams()));
                        break;
                    case CONFIGURATION_CHANGED:
                        initValues();
                        break;
                    case DATABASE_UPDATE_STATE_CHANGED:
                        setGuiMode(GuiMode.DatabaseUpdating);
                        break;
                    case DATABASE_UPDATE_FINISHED:
                    case DATABASE_UPDATE_FINISHED_NOT_RUN:
                        setGuiMode(GuiMode.DatabaseIdle);
                        break;
                    case SERVER_STARTED:
                        setGuiMode(GuiMode.ServerRunning);
                        break;
                    case SERVER_STOPPED:
                        setGuiMode(GuiMode.ServerIdle);
                        break;
                }
            }
        });
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.misc.title");
    }

    public class UseProxyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
            SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
        }
    }

    public class ProgramUpdateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            UpdateUtils.checkForUpdate(false);
        }
    }
}
