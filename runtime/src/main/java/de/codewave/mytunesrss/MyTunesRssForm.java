/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.desktop.DesktopWrapper;
import de.codewave.mytunesrss.desktop.DesktopWrapperFactory;
import de.codewave.mytunesrss.task.SendSupportRequestRunnable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventListener;
import java.util.Locale;

public class MyTunesRssForm {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssForm.class);

    private JFrame frame;
    private JButton myStartAdminBrowser;
    private JTextField myAdminUrl;
    private JButton myQuit;
    private JTextField mySupportName;
    private JButton mySendSupport;
    private JTextField mySupportEmail;
    private JTextArea mySupportDescription;
    private JPanel myRootPanel;
    private JButton myStartUserBrowser;
    private JTextField myUserUrl;

    public MyTunesRssForm() throws AWTException {
        myStartAdminBrowser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                DesktopWrapper desktopWrapper = DesktopWrapperFactory.createDesktopWrapper();
                if (desktopWrapper.isSupported()) {
                    try {
                        desktopWrapper.openBrowser(new URI(myAdminUrl.getText()));
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("Could not open admin interface in browser.", e);
                    }
                } else {
                    JOptionPane.showMessageDialog(myRootPanel, MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.needJava6ForBrowserText"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.needJava6ForBrowserTitle"), JOptionPane.OK_OPTION);
                }
            }
        });
        myStartUserBrowser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                DesktopWrapper desktopWrapper = DesktopWrapperFactory.createDesktopWrapper();
                if (desktopWrapper.isSupported()) {
                    try {
                        desktopWrapper.openBrowser(new URI(myUserUrl.getText()));
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("Could not open user interface in browser.", e);
                    }
                } else {
                    JOptionPane.showMessageDialog(myRootPanel, MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.needJava6ForBrowserText"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.needJava6ForBrowserTitle"), JOptionPane.OK_OPTION);
                }
            }
        });
        mySendSupport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (StringUtils.isNotBlank(mySupportName.getText()) && StringUtils.isNotBlank(mySupportEmail.getText()) && StringUtils.isNotBlank(mySupportDescription.getText())) {
                    SendSupportRequestRunnable runnable = new SendSupportRequestRunnable(mySupportName.getText(), mySupportEmail.getText(), mySupportDescription.getText() + "\n\n\n", false);
                    runnable.run();
                    if (runnable.isSuccess()) {
                        JOptionPane.showMessageDialog(myRootPanel, MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.supportSentText"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.supportSentTitle"), JOptionPane.OK_OPTION);
                    } else {
                        JOptionPane.showMessageDialog(myRootPanel, MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.supportErrorText"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.supportErrorTitle"), JOptionPane.OK_OPTION);
                    }
                } else {
                    JOptionPane.showMessageDialog(myRootPanel, MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.missingSupportFieldsText"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.missingSupportFieldsTitle"), JOptionPane.OK_OPTION);
                }
            }
        });
        myQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                for (Component c : new Component[] {myStartAdminBrowser, myAdminUrl, myStartUserBrowser, myUserUrl, mySupportName, mySupportEmail, mySupportDescription, mySendSupport, myQuit}) {
                    c.setEnabled(false);
                }
                MyTunesRssUtils.shutdownGracefully();
            }
        });
        refreshSupportConfig();
        frame = new JFrame(MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.title", MyTunesRss.VERSION));
        final MyTunesRssSystray systray = SystemUtils.IS_OS_WINDOWS ? new MyTunesRssSystray(frame) : null;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (SystemUtils.IS_OS_MAC_OSX) {
                    LOGGER.debug("Window is being closed on Mac OS X, so the window is hidden now.");
                    frame.setVisible(false);
                } else {
                    LOGGER.debug("Window is being closed, so the application is shut down now.");
                    MyTunesRssUtils.shutdownGracefully();
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {
                if (SystemUtils.IS_OS_WINDOWS && systray.isAvailable()) {
                    LOGGER.debug("Window has been iconified (state is " + frame.getExtendedState() + ") and systray is available, so we hide the window now!");
                    frame.setVisible(false);
                }
            }
        });
        frame.getContentPane().add(myRootPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        executeApple();
    }


    public void setAdminUrl(int port) {
        myAdminUrl.setText(port > 0 ? "http://127.0.0.1:" + port : "");
        myStartAdminBrowser.setEnabled(port > 0 && myQuit.isEnabled());
        if (port > 0 && myQuit.isEnabled() && MyTunesRss.CONFIG.isInitialWizard()) {
            DesktopWrapper desktopWrapper = DesktopWrapperFactory.createDesktopWrapper();
            if (desktopWrapper.isSupported()) {
                try {
                    desktopWrapper.openBrowser(new URI(myAdminUrl.getText()));
                } catch (URISyntaxException e) {
                    LOGGER.error("Could not open admin interface in browser.", e);
                }
            }
        }
    }

    public void setUserUrl(int port) {
        myUserUrl.setText(port > 0 ? "http://127.0.0.1:" + port + StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebappContext()) : "");
        myStartUserBrowser.setEnabled(port > 0 && myQuit.isEnabled());
    }

    public void refreshSupportConfig() {
        mySupportName.setText(MyTunesRss.CONFIG.getSupportName());
        mySupportEmail.setText(MyTunesRss.CONFIG.getSupportEmail());
    }

    public void executeApple() {
        LOGGER.debug("Trying to execute apple specific code.");
        if (SystemUtils.IS_OS_MAC_OSX) {
            try {
                LOGGER.debug("Executing apple specific code.");
                Class appleExtensionsClass = Class.forName("de.codewave.apple.AppleExtensions");
                Method activateMethod = appleExtensionsClass.getMethod("activate", EventListener.class);
                activateMethod.invoke(null, new AppleExtensionsEventListener(frame));
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not activate apple extensions.", e);
                }
            }
        }
    }

    public static class AppleExtensionsEventListener implements EventListener {

        private JFrame myFrame;

        public AppleExtensionsEventListener(JFrame frame) {
            myFrame = frame;
        }

        public void handleQuit() {
            LOGGER.debug("Apple extension: handleQuit.");
            MyTunesRssUtils.shutdownGracefully();
        }

        public void handleReOpenApplication() {
            LOGGER.debug("Apple extension: handleReOpenApplication.");
            if (!myFrame.isVisible()) {
                LOGGER.debug("Frame not visible, setting to visible.");
                myFrame.setVisible(true);
            } else {
                LOGGER.debug("Frame already visible, nothing to do here.");
            }
        }
    }
}
