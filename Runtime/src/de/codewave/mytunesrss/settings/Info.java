/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import javax.swing.*;
import java.awt.*;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info {
    private JPanel myRootPanel;
    private JTextField myRegistrationNameInput;
    private JTextField myExpirationInput;
    private JTextArea myUnregisteredTextArea;
    private JLabel myRigistrationNameLabel;
    private JLabel myExpirationLabel;
    private JButton MyRegisterButton;

    private void createUIComponents() {
        myUnregisteredTextArea = new JTextArea() {
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(0, 0);
            }
        };
    }
}