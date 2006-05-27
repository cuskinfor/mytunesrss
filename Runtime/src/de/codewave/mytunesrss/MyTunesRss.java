/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.*;
import de.codewave.utils.moduleinfo.*;
import org.apache.catalina.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    private static final Log LOG = LogFactory.getLog(MyTunesRss.class);
    public static String VERSION;
    public static Map<OperatingSystem, URL> UPDATE_URLS;
    public static DataStore STORE = new DataStore();

    static {
        UPDATE_URLS = new HashMap<OperatingSystem, URL>();
        String base = "http://www.codewave.de/download/versions/mytunesrss_";
        try {
            UPDATE_URLS.put(OperatingSystem.MacOSX, new URL(base + "macosx.txt"));
            UPDATE_URLS.put(OperatingSystem.Windows, new URL(base + "windows.txt"));
            UPDATE_URLS.put(OperatingSystem.Unknown, new URL(base + "generic.txt"));
        } catch (MalformedURLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create update url.", e);
            }
        }
    }

    public static void main(String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException,
            ClassNotFoundException, IOException, SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Operating system: " + ProgramUtils.guessOperatingSystem().name());
        }
        STORE.init();
        if (ProgramUtils.getCommandLineArguments(args).containsKey("debug")) {
            Logger.getLogger("de.codewave").setLevel(Level.DEBUG);
        }
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ResourceBundle mainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        VERSION = modulesInfo != null ? modulesInfo.getVersion() : "0.0.0";
        System.setProperty("mytunesrss.version", VERSION);
        final JFrame frame = new JFrame(mainBundle.getString("gui.title") + " v" + VERSION);
        frame.setIconImage(ImageIO.read(MyTunesRss.class.getResource("WindowIcon.png")));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        final Settings settings = new Settings(frame);
        frame.addWindowListener(new MyTunesRssMainWindowListener(settings));
        frame.getContentPane().add(settings.getRootPanel());
        frame.setResizable(false);
        int x = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_x", frame.getLocation().x);
        int y = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_y", frame.getLocation().y);
        frame.setLocation(x, y);
        frame.setVisible(true);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler(frame));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                if (settings.isUpdateCheckOnStartup()) {
                    settings.checkForUpdate(true);
                }
                CheckNeedsCreationTask checkNeedsCreationTask = new CheckNeedsCreationTask();
                PleaseWait.start(frame, null, "Checking database... please wait.", false, false, checkNeedsCreationTask);
                if (!checkNeedsCreationTask.isExistent()) {
                    PleaseWait.start(frame,
                                     null,
                                     DatabaseBuilderTask.BuildType.Update.getVerb() + " database... please wait.",
                                     false,
                                     false,
                                     new CreateAllTablesTask());
                }
                MyTunesRssConfig data = new MyTunesRssConfig();
                data.load();
                if (data.isAutoStartServer()) {
                    settings.doStartServer();
                }
            }
        });
    }

    public static class MyTunesRssMainWindowListener extends WindowAdapter {
        private Settings mySettingsForm;

        public MyTunesRssMainWindowListener(Settings settingsForm) {
            mySettingsForm = settingsForm;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            mySettingsForm.doQuitApplication();
        }
    }

    public static class CheckNeedsCreationTask extends PleaseWait.Task {
        private boolean myExistent;

        public void execute() {
            try {
                MyTunesRss.STORE.executeQuery(new DataStoreQuery<Boolean>() {
                    public Collection<Boolean> execute(Connection connection) throws SQLException {
                        ResultSet resultSet = connection.createStatement().executeQuery(
                                "SELECT COUNT(*) FROM information_schema.system_tables WHERE table_schem = 'PUBLIC' AND table_name = 'TRACK'");
                        if (resultSet.next() && resultSet.getInt(1) == 1) {
                            myExistent = true;
                            return null;
                        }
                        myExistent = false;
                        return null;
                    }
                });
            } catch (SQLException e) {
                myExistent = false;
            }
        }

        protected void cancel() {
            // intentionally left blank
        }

        public boolean isExistent() {
            return myExistent;
        }
    }

    public static class CreateAllTablesTask extends PleaseWait.Task {
        public void execute() {
            DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
            storeSession.begin();
            try {
                storeSession.executeStatement(new CreateAllTablesStatement());
                storeSession.commit();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create tables.", e);
                }
                try {
                    storeSession.rollback();
                } catch (SQLException e1) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not rollback transaction.", e1);
                    }
                }
            }
        }

        protected void cancel() {
            // intentionally left blank
        }
    }

    public static class UncaughtHandler implements Thread.UncaughtExceptionHandler {
        private JDialog myDialog;

        public UncaughtHandler(JFrame parent) {
            JOptionPane pane = new JOptionPane() {
                @Override
                public int getMaxCharactersPerLineCount() {
                    return 100;
                }
            };
            pane.setMessageType(JOptionPane.ERROR_MESSAGE);
            pane.setMessage(
                    "The application has failed because it has run out of memory. Please raise the available memory on the first settings tab and restart MyTunesRSS to activate the changes.");
            String okButton = "Ok";
            pane.setInitialValue(okButton);
            myDialog = pane.createDialog(parent, "Fatal error");
            myDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        }

        public void uncaughtException(Thread t, Throwable e) {
            if (e instanceof OutOfMemoryError) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        myDialog.setLocationRelativeTo(myDialog.getParent());
                        myDialog.setVisible(true);
                    }
                });
            }
        }
    }
}