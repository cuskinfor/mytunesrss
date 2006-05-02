/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.DownloadProgress
 */
public class DownloadProgress {
    private JProgressBar myProgressBar;
    private JButton myCancelButton;
    private JPanel myRootPanel;
    private JLabel myLabel;

    public JButton getCancelButton() {
        return myCancelButton;
    }

    public JLabel getLabel() {
        return myLabel;
    }

    public JProgressBar getProgressBar() {
        return myProgressBar;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }
}