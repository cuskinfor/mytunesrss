package de.codewave.utils.swing.pleasewait;

import de.codewave.utils.swing.TaskExecutor;

import javax.swing.*;
import java.awt.*;

/**
 * de.codewave.utils.swing.pleasewait.PleaseWaitUtils
 */
public class PleaseWaitUtils {
    public static void executeAndWait(Frame parent, Icon icon, String title, String text, String cancelButtonText, boolean progressBar, PleaseWaitTask task) {
        PleaseWaitDialog dialog = new PleaseWaitDialog(parent, icon, title, text, cancelButtonText, progressBar, task);
        TaskExecutor.executeAndWait(task, dialog);
    }
}
