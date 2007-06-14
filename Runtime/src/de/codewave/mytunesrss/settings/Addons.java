package de.codewave.mytunesrss.settings;

import javax.swing.*;
import java.awt.*;

/**
 * de.codewave.mytunesrss.settings.Addons
 */
public class Addons {
    private JPanel myRootPanel;
    private JButton myAddThemeButton;
    private JButton myDeleteThemeButton;
    private JScrollPane myThemesScrollPane;
    private JList myThemesList;
    private JButton myAddLanguageButton;
    private JButton myDeleteLanguageButton;
    private JScrollPane myLanguagesScrollPane;
    private JList myLanguagesList;

    private void createUIComponents() {
        myThemesList = new JList() {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(0, 0);
            }
        };
        myLanguagesList = new JList() {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(0, 0);
            }
        };
    }

    public void init() {
        myThemesScrollPane.setMaximumSize(myThemesScrollPane.getPreferredSize());
        myThemesScrollPane.getViewport().setOpaque(false);
        myLanguagesScrollPane.setMaximumSize(myLanguagesScrollPane.getPreferredSize());
        myLanguagesScrollPane.getViewport().setOpaque(false);
    }
}