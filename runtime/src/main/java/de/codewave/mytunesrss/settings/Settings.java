package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.Timer;

/**
 * de.codewave.mytunesrss.settings.Settings
 */
public class Settings implements MyTunesRssEventListener {
    private JPanel myRootPanel;
    private Server myServerForm;
    private Directories myDirectoriesForm;
    private UserManagement myUserManagementForm;
    private Misc myMiscForm;
    private JButton myStartServerButton;
    private JButton myStopServerButton;
    private JButton myQuitButton;
    private JTabbedPane myTabbedPane;
    private Database myDatabaseForm;
    private Info myInfoForm;
    private Addons myAddonsForm;
    private JPanel myAddonsPanel;
    private Streaming myStreamingForm;
    private Content myContentForm;
    private JPanel myStreamingPanel;
    private JPanel myContentPanel;

    public Database getDatabaseForm() {
        return myDatabaseForm;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void init() {
        initRegistration();
        myStartServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStartServer();
            }
        });
        myStopServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStopServer();
            }
        });
        myQuitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doQuitApplication();
            }
        });
        myServerForm.init();
        myDatabaseForm.init();
        myDirectoriesForm.init();
        myContentForm.init();
        myMiscForm.init();
        myInfoForm.init();
        myUserManagementForm.init();
        myStreamingForm.init();
        myAddonsForm.init();
        myTabbedPane.addChangeListener(new TabSwitchListener());
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    private void initRegistration() {
        if (!MyTunesRss.REGISTRATION.isRegistered()) {
            myTabbedPane.remove(myAddonsPanel);
            myTabbedPane.remove(myStreamingPanel);
            myTabbedPane.remove(myContentPanel);
        }
    }

    public String updateConfigFromGui() {
        StringBuffer messages = new StringBuffer();
        String message = myServerForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myDatabaseForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myDirectoriesForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myMiscForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myStreamingForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myAddonsForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myInfoForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        String returnValue = messages.toString().trim();
        return returnValue.length() > 0 ? returnValue : null;
    }

    public void handleEvent(MyTunesRssEvent event) {
        switch (event) {
            case SERVER_STARTED:
                if (MyTunesRss.SYSTRAYMENU != null) {
                    MyTunesRss.SYSTRAYMENU.setServerRunning();
                }
                myStartServerButton.setEnabled(false);
                myStopServerButton.setEnabled(true);
                break;
            case SERVER_STOPPED:
                if (MyTunesRss.SYSTRAYMENU != null) {
                    MyTunesRss.SYSTRAYMENU.setServerStopped();
                }
                myStartServerButton.setEnabled(true);
                myStopServerButton.setEnabled(false);
                break;
        }
    }

    /**
     * @deprecated The methods called by this method are correctly called by events
     * now. Remove this method as soon as possible.
     */
    public void setGuiMode(GuiMode mode) {
        myServerForm.setGuiMode(mode);
        myDatabaseForm.setGuiMode(mode);
        myDirectoriesForm.setGuiMode(mode);
        myMiscForm.setGuiMode(mode);
        myStreamingForm.setGuiMode(mode);
        myAddonsForm.setGuiMode(mode);
    }

    public void doStartServer() {
        String messages = updateConfigFromGui();
        if (messages == null) {
            MyTunesRss.startWebserver();
        } else {
            MyTunesRssUtils.showErrorMessage(messages);
        }
    }

    public void doStopServer() {
        MyTunesRss.stopWebserver();
    }

    public void doQuitApplication() {
        String messages = updateConfigFromGui();
        if (messages == null) {
            MyTunesRssUtils.shutdownGracefully();
        } else {
            MyTunesRssUtils.showErrorMessage(messages);
        }
    }

    public class TabSwitchListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            updateConfigFromGui();
        }
    }

}