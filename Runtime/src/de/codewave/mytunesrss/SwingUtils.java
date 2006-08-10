/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import javax.swing.*;
import java.awt.*;

/**
 * de.codewave.mytunesrss.SwingUtils
 */
public class SwingUtils {
    public static void showErrorMessage(JFrame parent, String message) {
        showMessage(parent, JOptionPane.ERROR_MESSAGE, MyTunesRss.BUNDLE.getString("error.title"), message);
    }

    public static void showInfoMessage(JFrame parent, String message) {
        showMessage(parent, JOptionPane.INFORMATION_MESSAGE, MyTunesRss.BUNDLE.getString("info.title"), message);
    }

    private static void showMessage(JFrame parent, int type, String title, String message) {
        JOptionPane pane = createPane(type, message);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.setVisible(true);
    }

    private static JOptionPane createPane(int type, String message) {
        JOptionPane pane = new JOptionPane() {
            @Override
            public int getMaxCharactersPerLineCount() {
                return 100;
            }
        };
        pane.setMessageType(type);
        pane.setMessage(message);
        return pane;
    }

    public static Object showOptionsMessage(JFrame parent, int type, String title, String message, Object[] options) {
        JOptionPane pane = createPane(type, message);
        pane.setOptions(options);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.setVisible(true);
        return pane.getValue();
    }

    public static void enableElementAndLabel(JComponent element, boolean enabled) {
        element.setEnabled(enabled);
        Component[] components = element.getParent().getComponents();
        if (components != null && components.length > 0) {
            for (int i = 0; i < components.length; i++) {
                if (components[i]instanceof JLabel && ((JLabel)components[i]).getLabelFor() == element) {
                    components[i].setEnabled(enabled);
                }
            }
        }
    }
}