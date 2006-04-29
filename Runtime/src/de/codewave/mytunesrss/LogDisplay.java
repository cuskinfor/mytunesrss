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
    private boolean myLoggingEnabled;
    boolean myOutOfMemoryError;

    public LogDisplay() {
        myClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartLog();
            }
        });
    }

    public boolean isLoggingEnabled() {
        return myLoggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        myLoggingEnabled = loggingEnabled;
        restartLog();
    }

    public void restartLog() {
        if (myLoggingEnabled) {
            myTextArea.setText("Operating system: " + System.getProperty("os.name") + System.getProperty("line.separator"));
        } else {
            myTextArea.setText(null);
        }
        myOutOfMemoryError = false;
    }

    protected synchronized void append(final LoggingEvent loggingEvent) {
        final String text = getText(loggingEvent);
        if (text.contains("OutOfMemoryError")) {
            myOutOfMemoryError = true;
        }
        if (myLoggingEnabled) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    myTextArea.append(text);
                }
            });
        }
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

    public boolean containsText(String text) {
        return myTextArea.getText().contains(text);
    }

    public boolean isOutOfMemoryError() {
        return myOutOfMemoryError;
    }

    public void show(JFrame frame, final JButton openButton) {
        final JDialog dialog = new JDialog(frame,
                                           PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss").getString("gui.logfile.title"),
                                           false);
        myCloseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openButton.setEnabled(isLoggingEnabled());
                dialog.dispose();
            }
        });
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                openButton.setEnabled(isLoggingEnabled());
                dialog.dispose();
            }
        });
        dialog.add(myRootPanel);
        dialog.pack();
        final Dimension minSize = dialog.getSize();
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = dialog.getSize();
                dialog.setSize(new Dimension(Math.max(size.width, minSize.width), Math.max(size.height, minSize.height)));
            }
        });
        dialog.setVisible(true);
    }

}