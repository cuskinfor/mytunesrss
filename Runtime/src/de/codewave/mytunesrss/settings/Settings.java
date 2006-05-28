package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.settings.Settings
 */
public class Settings {
    private static final Log LOG = LogFactory.getLog(Settings.class);

    private JFrame myFrame;
    private JPanel myRootPanel;
    private JLabel myStatusLabel;
    private General myGeneralForm;
    private Options myOptionsForm;

    public JFrame getFrame() {
        return myFrame;
    }

    public General getGeneralForm() {
        return myGeneralForm;
    }

    public Options getOptionsForm() {
        return myOptionsForm;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void init(JFrame frame) {
        myGeneralForm.init(this);
        myOptionsForm.init(this);
        myFrame = frame;
        setStatus(MyTunesRss.BUNDLE.getString("info.server.idle"), null);
    }

    public void setStatus(String text, String tooltipText) {
        if (text != null) {
            myStatusLabel.setText(text);
        }
        if (tooltipText != null) {
            myStatusLabel.setToolTipText(tooltipText);
        }
    }

    public void updateConfigFromGui() {
        myGeneralForm.updateConfigFromGui();
        myOptionsForm.updateConfigFromGui();
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                if (MyTunesRss.SYSTRAYMENU != null) {
                    MyTunesRss.SYSTRAYMENU.setServerRunning();
                }
                break;
            case ServerIdle:
                if (MyTunesRss.SYSTRAYMENU != null) {
                    MyTunesRss.SYSTRAYMENU.setServerStopped();
                }
        }
        myGeneralForm.setGuiMode(mode);
        myOptionsForm.setGuiMode(mode);
        myRootPanel.validate();
    }
}