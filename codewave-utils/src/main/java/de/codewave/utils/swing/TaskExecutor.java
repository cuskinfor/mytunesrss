package de.codewave.utils.swing;

import javax.swing.*;

/**
 * de.codewave.utils.swing.TaskExecutor
 */
public class TaskExecutor {
    public static void executeAndWait(final Task task, final JDialog dialog) {
        dialog.setModal(true);
        new Thread(new Runnable() {
            public void run() {
                waitForDialog(dialog);
                try {
                    task.setExecutionThread(Thread.currentThread());
                    task.execute();
                    dialog.dispose();
                } catch (Exception e) {
                    try {
                        task.handleException(e);
                    } finally {
                        dialog.dispose();
                    }
                }
            }
        }, "TaskExecutor/sync:" + task.getClass().getName()).start();
        SwingUtils.packAndShowRelativeTo(dialog, dialog.getParent());
    }

    private static void waitForDialog(JDialog dialog) {
        // wait for dialog to show before starting to prevnt very short tasks to finish before the dialog comes up
        while (!dialog.isShowing()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // intentionally left blank
            }
        }
    }

    public static void execute(final Task task, final TaskFinishedListener listener) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    task.setExecutionThread(Thread.currentThread());
                    task.execute();
                } catch (Exception e) {
                    try {
                        task.handleException(e);
                    } finally {
                        listener.taskFinished(task);
                    }
                }
                listener.taskFinished(task);
            }
        }, "TaskExecutor/async:" + task.getClass().getName()).start();
    }
}