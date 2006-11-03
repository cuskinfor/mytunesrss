/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;

import de.codewave.mytunesrss.*;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info {
    private JButton mySupportContactButton;
    private JPanel myRootPanel;

    public void init() {
        mySupportContactButton.addActionListener(new SupportContactActionListener());
    }

    public static class SupportContactActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            new SupportContact().display(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.supportRequest"), MyTunesRss.BUNDLE.getString(
                    "settings.supportInfo"));
        }
    }
}