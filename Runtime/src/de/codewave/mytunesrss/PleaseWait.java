/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import javax.swing.*;
import java.awt.event.*;

/**
 * de.codewave.mytunesrss.PleaseWait
 */
public class PleaseWait {
    public static void start(JFrame parent, String title, String text, boolean progressBar, boolean cancelButton, final Task task) {
        final JDialog dialog = new JDialog(parent, title, true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        PleaseWait pleaseWait = new PleaseWait(text, progressBar, cancelButton, task);
        task.setProgressBar(pleaseWait.myProgressBar);
        dialog.add(pleaseWait.myRootPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        Thread taskThread = new Thread(new Runnable() {
            public void run() {
                try {
                    task.execute();
                    dialog.dispose();
                } catch (Exception e) {
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        });
        taskThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                dialog.dispose();
                t.getThreadGroup().uncaughtException(t, e);// forward further
            }
        });
        taskThread.start();
        dialog.setVisible(true);
    }

    private JPanel myRootPanel;
    private JLabel myInfoLabel;
    private JProgressBar myProgressBar;
    private JButton myCancelButton;

    private PleaseWait(String text, boolean progressBar, boolean cancelButton, final Task task) {
        myInfoLabel.setText(text);
        myProgressBar.setVisible(progressBar);
        myCancelButton.setVisible(cancelButton);
        myCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                task.cancel();
            }
        });
    }

    public abstract static class Task {
        private JProgressBar myProgressBar;

        void setProgressBar(JProgressBar progressBar) {
            myProgressBar = progressBar;
        }

        protected final void setPercentage(int percentage) {
            if (percentage < 0 || percentage > 100) {
                throw new IllegalArgumentException("Percentage value (found " + percentage + ") must not be less than 0 or greater than 100.");
            }
            myProgressBar.setValue(percentage);
        }

        public abstract void execute() throws Exception;

        protected abstract void cancel();
    }

    public abstract static class NoCancelTask extends Task {
        protected final void cancel() {
            // intentionally left blank
        }
    }
}