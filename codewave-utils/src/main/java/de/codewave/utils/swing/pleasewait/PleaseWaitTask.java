package de.codewave.utils.swing.pleasewait;

import de.codewave.utils.swing.Task;

import javax.swing.*;

/**
 de.codewave.utils.swing.pleasewait.PleaseWaitTasksk
 */
public abstract class PleaseWaitTask extends Task {
    private JProgressBar myProgressBar;

    void setProgressBar(JProgressBar progressBar) {
        myProgressBar = progressBar;
    }

    protected final void setPercentage(final int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage value (found " + percentage + ") must not be less than 0 or greater than 100.");
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                myProgressBar.setValue(percentage);
                myProgressBar.getRootPane().validate();

            }
        });
    }

    protected void cancel() {
        // intentionally left blank
    }
}