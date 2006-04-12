/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * de.codewave.mytunesrss.About
 */
public class About {
    public static void displayAbout(JFrame parent) {
        About about = new About();
        JDialog dialog = new JDialog(parent, parent.getTitle(), true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(about.myRootPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private JButton myOkButton;
    private JPanel myRootPanel;

    public About() {
        myOkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((Window)myRootPanel.getTopLevelAncestor()).dispose();
            }
        });
    }
}