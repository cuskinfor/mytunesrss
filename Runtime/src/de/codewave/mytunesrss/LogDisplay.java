/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.LogDisplay
 */
public class LogDisplay extends AppenderSkeleton {
    private JPanel myRootPanel;
    private JButton myClearButton;
    private JButton myCloseButton;
    private JTextArea myTextArea;

    public LogDisplay() {
        restartLog();
        myClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartLog();
            }
        });
    }

    private void restartLog() {
        myTextArea.setText("Operating system: " + System.getProperty("os.name") + System.getProperty("line.separator"));
    }

    protected synchronized void append(final LoggingEvent loggingEvent) {
        final String text = getText(loggingEvent);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                myTextArea.append(text);
            }
        });
    }

    private String getText(LoggingEvent loggingEvent) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(loggingEvent.getRenderedMessage()).append(System.getProperty("line.separator"));
        ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
        String[] throwableStrRep = throwableInformation != null ? throwableInformation.getThrowableStrRep() : null;
        if (throwableStrRep != null && throwableStrRep.length > 0) {
            for (int i = 0; i < throwableStrRep.length; i++) {
                buffer.append(throwableStrRep[i]).append(System.getProperty("line.separator"));
            }
        }
        return buffer.toString();
    }

    public void close() {
        // intentionally left blank
    }

    public boolean requiresLayout() {
        return false;
    }

    public void show(JFrame frame, final JButton openButton) {
        final JDialog dialog = new JDialog(frame,
                                           PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss").getString("gui.logfile.title"),
                                           false);
        myCloseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openButton.setEnabled(true);
                dialog.dispose();
            }
        });
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                openButton.setEnabled(true);
                dialog.dispose();
            }
        });
        dialog.add(myRootPanel);
        dialog.setVisible(true);
        dialog.pack();
        final Dimension minSize = dialog.getSize();
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = dialog.getSize();
                dialog.setSize(new Dimension(Math.max(size.width, minSize.width), Math.max(size.height, minSize.height)));
            }
        });
    }
}