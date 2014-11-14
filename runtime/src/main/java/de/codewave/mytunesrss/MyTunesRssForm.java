/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.desktop.DesktopWrapper;
import de.codewave.mytunesrss.desktop.DesktopWrapperFactory;
import de.codewave.mytunesrss.server.WebServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
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

    private JFrame myFrame;
    private JButton myStartAdminBrowser;
    private JTextField myAdminUrl;
    private JButton myQuit;
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
        myQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                for (Component c : new Component[]{myStartAdminBrowser, myAdminUrl, myStartUserBrowser, myUserUrl, myQuit}) {
                    c.setEnabled(false);
                }
                new Thread(new Runnable() {
                    public void run() {
                        MyTunesRssUtils.shutdownGracefully();
                    }
                }, "AsyncSwingFormShutdown").start();
            }
        });
        myFrame = new JFrame(MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.title", MyTunesRss.VERSION));
        final MyTunesRssSystray systray = SystemUtils.IS_OS_WINDOWS ? new MyTunesRssSystray(myFrame) : null;
        myFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (SystemUtils.IS_OS_MAC_OSX) {
                    LOGGER.debug("Window is being closed on Mac OS X, so the window is hidden now.");
                    myFrame.setVisible(false);
                } else {
                    LOGGER.debug("Window is being closed, so the application is shut down now.");
                    MyTunesRssUtils.shutdownGracefully();
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {
                if (SystemUtils.IS_OS_WINDOWS && systray.isAvailable()) {
                    LOGGER.debug("Window has been iconified (state is " + myFrame.getExtendedState() + ") and systray is available, so we hide the window now!");
                    myFrame.setVisible(false);
                }
            }
        });
        myFrame.getContentPane().add(myRootPanel);
        myFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        myFrame.setResizable(false);
        myFrame.pack();
        myFrame.setLocationRelativeTo(null);
        myFrame.setVisible(true);
        executeApple();
    }


    public void setAdminUrl(String host, int port) {
        myAdminUrl.setText(port > 0 ? "http://" + ("0.0.0.0".equals(host) ? "127.0.0.1" : host) + ":" + port : "");
        myAdminUrl.setToolTipText(myAdminUrl.getText());
        myStartAdminBrowser.setEnabled(port > 0 && myQuit.isEnabled());
        if (port > 0 && myQuit.isEnabled() && MyTunesRss.CONFIG.isShowInitialWizard()) {
            DesktopWrapper desktopWrapper = DesktopWrapperFactory.createDesktopWrapper();
            if (desktopWrapper.isSupported()) {
                try {
                    desktopWrapper.openBrowser(new URI(myAdminUrl.getText()));
                } catch (URISyntaxException e) {
                    LOGGER.error("Could not open admin interface in browser.", e);
                }
            }
        } else if (MyTunesRss.CONFIG.isInitialWizard()) {
            MyTunesRss.CONFIG.setInitialWizard(false);
            MyTunesRss.CONFIG.save();
        }
    }

    public void setAdminUrl(Exception e) {
        myAdminUrl.setText(e != null ? e.getMessage() : "");
        myAdminUrl.setToolTipText(myAdminUrl.getText());
        myStartAdminBrowser.setEnabled(false);
    }


    public void setUserUrl(int port) {
        myUserUrl.setText(port > 0 ? "http://" + StringUtils.defaultIfBlank(MyTunesRss.CONFIG.getHost(), "127.0.0.1") + ":" + port + WebServer.getContext() : "");
        myUserUrl.setToolTipText(myUserUrl.getText());
        myStartUserBrowser.setEnabled(port > 0 && myQuit.isEnabled());
    }

    public void setUserUrl(Exception e) {
        myUserUrl.setText(e != null ? e.getMessage() : "");
        myUserUrl.setToolTipText(myUserUrl.getText());
        myStartUserBrowser.setEnabled(false);
    }

    public void executeApple() {
        LOGGER.debug("Trying to execute apple specific code.");
        if (SystemUtils.IS_OS_MAC_OSX) {
            try {
                LOGGER.debug("Executing apple specific code.");
                Class appleExtensionsClass = Class.forName("de.codewave.apple.AppleExtensions");
                Method activateMethod = appleExtensionsClass.getMethod("activate", EventListener.class);
                activateMethod.invoke(null, new AppleExtensionsEventListener(myFrame));
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not activate apple extensions.", e);
                }
            }
        }
    }

    public void hide() {
        myFrame.setVisible(false);
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
