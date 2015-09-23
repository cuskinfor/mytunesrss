package de.codewave.utils.swing.pleasewait;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * de.codewave.utils.swing.pleasewait.PleaseWaitDialog
 */
public class PleaseWaitDialog extends JDialog {
    public PleaseWaitDialog(Frame parent, Icon icon, String title, String text, String cancelButtonText, boolean progressBar,
            final PleaseWaitTask task) {
        super(parent, title, true);
        PleaseWait pleaseWait = new PleaseWait(icon, text, cancelButtonText, progressBar);
        task.setProgressBar(pleaseWait.getProgressBar());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        add(pleaseWait.getRootPanel());
        pleaseWait.getCancelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        task.cancel();
                    }
                }, "PleaseWaitDialogCancel").start();
            }
        });
    }
}