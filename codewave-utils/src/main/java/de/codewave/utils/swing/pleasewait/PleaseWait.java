/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.swing.pleasewait;

import javax.swing.*;

/**
 * de.codewave.utils.swing.pleasewait.PleaseWait
 */
public class PleaseWait extends JDialog {
    private JPanel myRootPanel;
    private JLabel myInfoLabel;
    private JProgressBar myProgressBar;
    private JButton myCancelButton;
    private JLabel myIcon;

    PleaseWait(Icon icon, String text, String cancelButtonText, boolean progressBar) {
        myInfoLabel.setText(text);
        myIcon.setIcon(icon);
        myProgressBar.setVisible(progressBar);
        myCancelButton.setText(cancelButtonText);
        myCancelButton.setVisible(cancelButtonText != null);
    }

    JProgressBar getProgressBar() {
        return myProgressBar;
    }

    JPanel getRootPanel() {
        return myRootPanel;
    }

    JButton getCancelButton() {
        return myCancelButton;
    }
}