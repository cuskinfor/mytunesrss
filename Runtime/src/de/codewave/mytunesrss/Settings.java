package de.codewave.mytunesrss;

import org.apache.catalina.*;
import org.apache.catalina.session.*;
import org.apache.catalina.startup.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;
import java.net.*;

import de.codewave.utils.serialnumber.*;

/**
 * de.codewave.mytunesrss.Settings
 */
public class Settings {
    private static final Log LOG = LogFactory.getLog(Settings.class);
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String LIBRARY_XML_FILE_NAME = "iTunes Music Library.xml";
    private static final String SER_NUM_RANDOM = "myTUNESrss4eeeever!";

    private final ResourceBundle myMainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");

    private JFrame myFrame;
    private JTextField myPort;
    private JTextField myTunesXmlPath;
    private JPanel myRootPanel;
    private JLabel myStatusText;
    private JButton myStartStopButton;
    private JButton myQuitButton;
    private JButton myLookupButton;
    private JCheckBox myUseAuthCheck;
    private JPasswordField myPassword;
    private JTextField myFakeMp3Suffix;
    private JTextField myFakeM4aSuffix;
    private JLabel myProductIcon;
    private JTabbedPane myTabbedPane;
    private JPanel mySecurityPanel;
    private JPanel myFileTypesPanel;
    private JTextField myRegisterName;
    private JButton myRegisterButton;
    private JTextField myRegisterCode;
    private JPanel myRegisterPanel;
    private JEditorPane myHeadHeadBodyPEditorPane;
    private Embedded myServer;

    public Settings(JFrame frame) throws UnsupportedEncodingException {
        myFrame = frame;
        String regName = Preferences.userRoot().node("/de/codewave/mytunesrss").get("regname", "");
        String regCode = Preferences.userRoot().node("/de/codewave/mytunesrss").get("regcode", "");
        MyTunesRss.REGISTERED = SerialNumberUtils.isValid(regName, regCode, SER_NUM_RANDOM);
        setStatus(myMainBundle.getString("info.server.idle"));
        MyTunesRssConfig data = new MyTunesRssConfig();
        data.load();
        myPort.setText(data.getPort());
        myTunesXmlPath.setText(data.getLibraryXml());
        myUseAuthCheck.setSelected(data.isAuth());
        myPassword.setText(data.getPassword());
        myFakeMp3Suffix.setText(data.getFakeMp3Suffix());
        myFakeM4aSuffix.setText(data.getFakeM4aSuffix());
        myRegisterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doRegister();
            }
        });
        myStartStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStartStopServer();
            }
        });
        myQuitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doQuitApplication();
            }
        });
        myLookupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLookupLibraryFile();
            }
        });
        if (!MyTunesRss.REGISTERED) {
            myTabbedPane.remove(mySecurityPanel);
            myTabbedPane.remove(myFileTypesPanel);
        } else {
            myTabbedPane.remove(myRegisterPanel);
        }
        myRootPanel.doLayout();
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void doRegister() {
        String regName = myRegisterName.getText();
        String regCode = myRegisterCode.getText();
        try {
            MyTunesRss.REGISTERED = SerialNumberUtils.isValid(regName, regCode, SER_NUM_RANDOM);
        } catch (UnsupportedEncodingException e1) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not validate registration code.", e1);
            }
        }
        if (MyTunesRss.REGISTERED) {
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("regname", regName);
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("regcode", regCode);
            myTabbedPane.remove(myRegisterPanel);
            myTabbedPane.addTab(myMainBundle.getString("gui.settings.tab.security"), mySecurityPanel);
            myTabbedPane.addTab(myMainBundle.getString("gui.settings.tab.filetypes"), myFileTypesPanel);
        } else {
            showErrorMessage(myMainBundle.getString("error.registration.failure"));
        }
    }

    public void doLookupLibraryFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ITunesLibraryFileFilter(true));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(myMainBundle.getString("dialog.lookupLibraryXml.title"));
        if (fileChooser.showDialog(getRootPanel().getTopLevelAncestor(), null) == JFileChooser.APPROVE_OPTION) {
            try {
                myTunesXmlPath.setText(fileChooser.getSelectedFile().getCanonicalPath());
            } catch (IOException e) {
                showErrorMessage(myMainBundle.getString("error.lookupLibraryXml.failure") + e.getMessage());
            }
        }
    }

    public void doStartStopServer() {
        if (myServer == null) {
            doStartServer();
        } else {
            doStopServer();
        }
    }

    public void doStartServer() {
        int port;
        try {
            port = Integer.parseInt(myPort.getText().trim());
        } catch (NumberFormatException e) {
            port = MIN_PORT - 1;
        }
        final int serverPort = port;
        final File library = new File(myTunesXmlPath.getText().trim());
        if (port < MIN_PORT || port > MAX_PORT) {
            showErrorMessage(myMainBundle.getString("error.startServer.port"));
        } else if (!new ITunesLibraryFileFilter(false).accept(library)) {
            showErrorMessage(myMainBundle.getString("error.startServer.libraryXmlFile"));
        } else if (myUseAuthCheck.isSelected() && new String(myPassword.getPassword()).trim().length() == 0) {
            showErrorMessage(myMainBundle.getString("error.startServer.useAuthButNoPassword"));
        } else {
            enableButtons(false);
            enableConfig(false);
            myRootPanel.validate();
            setStatus(myMainBundle.getString("info.server.starting"));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        myServer = createServer("mytunesrss", null, serverPort, new File("."), "ROOT", "");
                        myServer.start();
                        myStartStopButton.setText(myMainBundle.getString("gui.settings.button.stopServer"));
                        myStartStopButton.setToolTipText(myMainBundle.getString("gui.settings.tooltip.stopServer"));
                    } catch (LifecycleException e) {
                        if (e.getMessage().contains("BindException")) {
                            showErrorMessage(myMainBundle.getString("error.server.startFailureBindException"));
                        } else {
                            showErrorMessage(myMainBundle.getString("error.server.startFailure") + e.getMessage());
                        }
                        setStatus(myMainBundle.getString("info.server.idle"));
                        enableConfig(true);
                    } catch (IOException e) {
                        showErrorMessage(myMainBundle.getString("error.server.startFailure") + e.getMessage());
                        setStatus(myMainBundle.getString("info.server.idle"));
                        enableConfig(true);
                    }
                    enableButtons(true);
                    myRootPanel.validate();
                    setStatus(myMainBundle.getString("info.server.running"));
                }
            }).start();
        }
    }

    private Embedded createServer(String name, InetAddress listenAddress, int listenPort, File catalinaBasePath, String webAppName,
            String webAppContext) throws IOException {
        Embedded server = new Embedded();
        server.setCatalinaBase(catalinaBasePath.getCanonicalPath());
        Engine engine = server.createEngine();
        engine.setName("engine." + name);
        engine.setDefaultHost("host." + name);
        Host host = server.createHost("host." + name, new File(catalinaBasePath, "webapps").getCanonicalPath());
        engine.addChild(host);
        Context context = server.createContext(webAppContext, webAppName);
        StandardManager sessionManager = new StandardManager();
        sessionManager.setPathname("");
        context.setManager(sessionManager);
        host.addChild(context);
        server.addEngine(engine);
        server.addConnector(server.createConnector(listenAddress, listenPort, false));
        MyTunesRssConfig config = createPrefDataFromGUI();
        context.getServletContext().setAttribute(MyTunesRssConfig.class.getName(), config);
        return server;
    }

    public void doStopServer() {
        enableButtons(false);
        myRootPanel.validate();
        new Thread(new Runnable() {
            public void run() {
                try {
                    setStatus(myMainBundle.getString("info.server.stopping"));
                    myServer.stop();
                    myServer = null;
                    setStatus(myMainBundle.getString("info.server.idle"));
                    enableConfig(true);
                    myStartStopButton.setText(myMainBundle.getString("gui.settings.button.startServer"));
                    myStartStopButton.setToolTipText(myMainBundle.getString("gui.settings.tooltip.startServer"));
                } catch (LifecycleException e) {
                    showErrorMessage(myMainBundle.getString("error.server.stopFailure") + e.getMessage());
                    setStatus(myMainBundle.getString("info.server.running"));
                }
                enableButtons(true);
                myRootPanel.validate();
            }
        }).start();
    }

    private void setStatus(String text) {
        myStatusText.setText(text);
    }

    public void doQuitApplication() {
        if (myQuitButton.isEnabled()) {
            if (myServer != null) {
                doStopServer();
            }
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("window_x", myFrame.getLocation().x);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("window_y", myFrame.getLocation().y);
            MyTunesRssConfig config = createPrefDataFromGUI();
            if (config.isDiffenrentFromSaved()) {
                if (JOptionPane.showOptionDialog(myRootPanel.getTopLevelAncestor(),
                                                 myMainBundle.getString("dialog.saveSettingsQuestion.message"),
                                                 myMainBundle.getString("dialog.saveSettingsQuestion.title"),
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 null,
                                                 null) == JOptionPane.YES_OPTION) {
                    config.save();
                }
            }
            System.exit(0);
        } else {
            showErrorMessage(myMainBundle.getString("error.quitWhileStartingOrStopping"));
        }
    }

    private MyTunesRssConfig createPrefDataFromGUI() {
        MyTunesRssConfig config = new MyTunesRssConfig();
        config.setPort(myPort.getText().trim());
        config.setLibraryXml(myTunesXmlPath.getText().trim());
        config.setAuth(myUseAuthCheck.isSelected());
        config.setPassword(new String(myPassword.getPassword()).trim());
        config.setFakeMp3Suffix(myFakeMp3Suffix.getText().trim());
        config.setFakeM4aSuffix(myFakeM4aSuffix.getText().trim());
        return config;
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(getRootPanel().getTopLevelAncestor(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(boolean enabled) {
        myStartStopButton.setEnabled(enabled);
        myQuitButton.setEnabled(enabled);
    }

    private void enableConfig(boolean enabled) {
        myLookupButton.setEnabled(enabled);
        myPort.setEnabled(enabled);
        myTunesXmlPath.setEnabled(enabled);
        myUseAuthCheck.setEnabled(enabled);
        myPassword.setEnabled(enabled);
        myFakeMp3Suffix.setEnabled(enabled);
        myFakeM4aSuffix.setEnabled(enabled);
    }

    public static class ITunesLibraryFileFilter extends javax.swing.filechooser.FileFilter {
        private boolean myAllowDirectories;


        public ITunesLibraryFileFilter(boolean allowDirectories) {
            myAllowDirectories = allowDirectories;
        }

        public boolean accept(File f) {
            return f != null && f.exists() &&
                    ((f.isDirectory() && myAllowDirectories) || (f.isFile() && LIBRARY_XML_FILE_NAME.equalsIgnoreCase(f.getName())));
        }

        public String getDescription() {
            return "iTunes Library";
        }
    }
}