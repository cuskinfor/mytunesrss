/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.desktop.DesktopWrapper;
import de.codewave.mytunesrss.desktop.DesktopWrapperFactory;
import de.codewave.mytunesrss.task.SendSupportRequestRunnable;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class MyTunesRssForm {
    private JFrame frame;
    private JButton myStartAdminBrowser;
    private JTextField myAdminPort;
    private JButton myQuit;
    private JTextField mySupportName;
    private JButton mySendSupport;
    private JTextField mySupportEmail;
    private JTextArea mySupportDescription;
    private JPanel myRootPanel;
    private JButton myStartUserBrowser;
    private JTextField myUserPort;

    public MyTunesRssForm() {
        myStartAdminBrowser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                DesktopWrapper desktopWrapper = DesktopWrapperFactory.createDesktopWrapper();
                if (desktopWrapper.isSupported()) {
                    try {
                        desktopWrapper.openBrowser(new URI("http://127.0.0.1:" + Integer.parseInt(myAdminPort.getText())));
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
                        desktopWrapper.openBrowser(new URI("http://127.0.0.1:" + Integer.parseInt(myUserPort.getText()) + StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebappContext())));
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
                for (Component c : new Component[] {myStartAdminBrowser, myAdminPort, myStartUserBrowser, myUserPort, mySupportName, mySupportEmail, mySupportDescription, mySendSupport, myQuit}) {
                    c.setEnabled(false);
                }
                MyTunesRss.QUIT_REQUEST = true;
            }
        });
        refreshSupportConfig();
        frame = new JFrame(MyTunesRssUtils.getBundleString(Locale.getDefault(), "mainForm.title", MyTunesRss.VERSION));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                frame.setState(Frame.ICONIFIED);
            }
        });
        frame.getContentPane().add(myRootPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }

    public void setAdminPort(int port) {
        myAdminPort.setText(port > 0 ? Integer.toString(port) : "");
        myStartAdminBrowser.setEnabled(myQuit.isEnabled());
    }

    public void setUserPort(int port) {
        myUserPort.setText(port > 0 ? Integer.toString(port) : "");
        myStartUserBrowser.setEnabled(myQuit.isEnabled());
    }

    public void refreshSupportConfig() {
        mySupportName.setText(MyTunesRss.CONFIG.getSupportName());
        mySupportEmail.setText(MyTunesRss.CONFIG.getSupportEmail());
    }
}
