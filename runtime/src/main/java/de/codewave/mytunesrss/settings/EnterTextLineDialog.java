package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class EnterTextLineDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField myTextLineInput;
    private boolean myCancelled;

    public EnterTextLineDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
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
        contentPane.registerKeyboardAction(new ActionListener() {
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

    public String getTextLine() {
        return myTextLineInput.getText();
    }
}
