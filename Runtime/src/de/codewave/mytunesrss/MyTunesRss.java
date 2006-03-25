/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.catalina.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    public static void main(String[] args) throws LifecycleException {
        JFrame frame = new JFrame("Codewave MyTunesRSS Feeder");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Settings settingsForm = new Settings();
        frame.getContentPane().add(settingsForm.getRootPanel());
        frame.pack();
        frame.setVisible(true);
    }
}