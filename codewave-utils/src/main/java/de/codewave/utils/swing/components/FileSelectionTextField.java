package de.codewave.utils.swing.components;

import de.codewave.utils.swing.JTextFieldValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.utils.swing.components.FileSelectionTextField
 */
public class FileSelectionTextField extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSelectionTextField.class);

    private JTextField myTextField;
    private JButton mySearchButton;
    private File myStartDirectory;
    private String myDialogTitle;
    private FileSelectionTextFieldSupport mySupport;
    private String mySelectLabel;
    private Component myParent;
    private File myFile;

    public FileSelectionTextField(Component parent, int hgap, File startDirectory, String dialogTitle, String selectLabel,
            final FileSelectionTextFieldSupport support) {
        myTextField = new JTextField();
        mySearchButton = new JButton("...");
        myParent = parent;
        myStartDirectory = startDirectory;
        myDialogTitle = dialogTitle;
        mySelectLabel = selectLabel;
        mySupport = support;
        setLayout(new BorderLayout(hgap, 0));
        add(BorderLayout.CENTER, myTextField);
        add(BorderLayout.EAST, mySearchButton);
        mySearchButton.addActionListener(new SearchButtonActionListener());
        JTextFieldValidation.setValidation(new JTextFieldValidation(myTextField, support.getValidationFailureMessage()) {
            protected boolean isValid(String text) {
                return support.isValid(text);
            }
        });
    }

    public class SearchButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setCurrentDirectory(myStartDirectory);
            fileChooser.setDialogTitle(myDialogTitle);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File file) {
                    return mySupport.accept(file);
                }

                public String getDescription() {
                    return mySupport.getFileFilterDescription();
                }
            });
            int result = fileChooser.showDialog(myParent, mySelectLabel);
            if (result == JFileChooser.APPROVE_OPTION) {
                myFile = fileChooser.getSelectedFile();
                try {
                    myTextField.setText(fileChooser.getSelectedFile().getCanonicalPath());
                } catch (IOException e) {
                    LOGGER.warn("Could not get canonical path.", e);
                }
                myStartDirectory = fileChooser.getCurrentDirectory();
                mySupport.handleSelect(fileChooser.getSelectedFile());
            }
        }
    }

    public void setFile(File file) {
        try {
            myFile = file;
            myTextField.setText(file.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.warn("Could not get canonical path.", e);
        }
    }

    public File getFile() {
        return myFile;
    }

    public static interface FileSelectionTextFieldSupport {
        boolean accept(File file);

        String getFileFilterDescription();

        void handleSelect(File file);

        boolean isValid(String text);

        String getValidationFailureMessage();
    }
}