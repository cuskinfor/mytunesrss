package de.codewave.mytunesrss.settings;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.settings.SettingsForm
 */
public interface SettingsForm {
    void init();
    String updateConfigFromGui();
    JPanel getRootPanel();
    String getDialogTitle();
}