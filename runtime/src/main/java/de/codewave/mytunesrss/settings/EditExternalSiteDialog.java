package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.ExternalSiteDefinition;
import de.codewave.mytunesrss.MyTunesRssUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * de.codewave.mytunesrss.settings.EditExternalSiteDialog
 */
public class EditExternalSiteDialog extends JDialog {
    private JButton myCancelButton;
    private JButton myOkButton;
    private JTextField myUrlInput;
    private JTextField myNameInput;
    private JComboBox myTypeInput;
    private JPanel myRootPanel;
    private boolean myCancelled;

    public EditExternalSiteDialog() {
        myTypeInput.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setText(MyTunesRssUtils.getBundleString("settings.editExternalSites.type." + value.toString()));
                return label;
            }
        });
        myTypeInput.addItem("album");
        myTypeInput.addItem("artist");
        myTypeInput.addItem("title");
        setContentPane(myRootPanel);
        setModal(true);
        getRootPane().setDefaultButton(myOkButton);

        myOkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        myCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        myRootPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        myCancelled = false;
        dispose();
    }

    private void onCancel() {
        myCancelled = true;
        dispose();
    }

    public boolean isCancelled() {
        return myCancelled;
    }

    public ExternalSiteDefinition getDefinition() {
        return new ExternalSiteDefinition(myTypeInput.getSelectedItem().toString(), myNameInput.getText(), myUrlInput.getText());
    }

    public void setExternalSiteDefinition(ExternalSiteDefinition definition) {
        myTypeInput.setSelectedItem(definition.getType());
        myNameInput.setText(definition.getName());
        myUrlInput.setText(definition.getUrl());
    }
}