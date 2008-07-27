package de.codewave.mytunesrss.settings;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.settings.SettingsForm
 */
public interface SettingsForm {
    void init();
    //void setGuiMode(GuiMode mode);
    String updateConfigFromGui();
    JPanel getRootPanel();
}