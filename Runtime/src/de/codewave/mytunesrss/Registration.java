/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.serialnumber.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.prefs.*;
import java.util.*;
import java.io.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.Registration
 */
public class Registration {
    private static final Log LOG = LogFactory.getLog(Registration.class);

    public static void displayRegistration(JFrame parent) {
        Registration registration = new Registration();
        JDialog dialog = new JDialog(parent, parent.getTitle(), true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(registration.myRootPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private final ResourceBundle myMainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
    private JPanel myRootPanel;
    private JTextField myCode;
    private JButton myRegisterButton;
    private JButton myNoRegisterButton;
    private JTextField myName;
    private JPanel myElementPanel;

    public Registration() {
        myRegisterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String regName = myName.getText();
                String regCode = myCode.getText();
                try {
                    MyTunesRss.REGISTERED = SerialNumberUtils.isValid(regName, regCode, MyTunesRss.SER_NUM_RANDOM);
                } catch (UnsupportedEncodingException e1) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not validate registration code.", e1);
                    }
                }
                if (MyTunesRss.REGISTERED) {
                    Preferences.userRoot().node("/de/codewave/mytunesrss").put("regname", regName);
                    Preferences.userRoot().node("/de/codewave/mytunesrss").put("regcode", regCode);
                    ((Window)myRootPanel.getTopLevelAncestor()).dispose();
                } else {
                    showErrorMessage(myMainBundle.getString("error.registration.failure"));
                }
            }
        });
        myNoRegisterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((Window)myRootPanel.getTopLevelAncestor()).dispose();
            }
        });
        myElementPanel.doLayout();
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(myRootPanel.getTopLevelAncestor(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}