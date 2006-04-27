/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import javax.swing.*;
import java.awt.event.*;

/**
 * de.codewave.mytunesrss.LogDisplay
 */
public class LogDisplay {
    private JPanel myRootPanel;
    private JButton myRefreshButton;
    private JButton myCloseButton;
    private JTextArea myTextArea;

    private JButton myOpenButton;
    private StringBufferAppender myStringBufferAppender;

    public LogDisplay(final JDialog dialog, JButton openButton, StringBufferAppender stringBufferAppender) {
        myOpenButton = openButton;
        myStringBufferAppender = stringBufferAppender;
        myRefreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myTextArea.setText(myStringBufferAppender.getText());
            }
        });
        myCloseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myOpenButton.setEnabled(true);
                dialog.dispose();
            }
        });
        myTextArea.setText(stringBufferAppender.getText());
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }
}