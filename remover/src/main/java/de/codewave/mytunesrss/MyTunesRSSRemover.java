/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.swing.*;
import de.codewave.utils.swing.pleasewait.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.MyTunesRSSRemover
 */
public class MyTunesRSSRemover {
    public static void main(String[] args) throws IOException {
        RemoverTask task = new RemoverTask();
        try {
            if (GraphicsEnvironment.isHeadless()) {
                task.execute();
                System.out.println("All MyTunesRSS data successfully removed from this computer.");
            } else {
                PleaseWaitUtils.executeAndWait(null, null, "MyTunesRSS Remover", "Removing MyTunesRSS data... please wait.", null, false, task);
                SwingUtils.showMessage(null,
                                       JOptionPane.INFORMATION_MESSAGE,
                                       "Success",
                                       "All MyTunesRSS data successfully removed from this computer.",
                                       50);
            }
        } catch (Exception e) {
            if (!GraphicsEnvironment.isHeadless()) {
                SwingUtils.showMessage(null, JOptionPane.ERROR_MESSAGE, "Error", "Could not remove all MyTunesRSS data from this computer.", 50);
            } else {
                System.err.println("Could not remove all MyTunesRSS data from this computer.");
            }
            e.printStackTrace(new PrintWriter(new FileWriter("MyTunesRSSRemover-error.log")));
        } finally {
            System.exit(0);
        }
    }
}