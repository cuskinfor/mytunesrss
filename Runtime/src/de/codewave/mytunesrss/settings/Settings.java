package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.util.prefs.*;

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
        myGeneralForm.setGuiMode(mode);
        myOptionsForm.setGuiMode(mode);
        myRootPanel.validate();
    }
}