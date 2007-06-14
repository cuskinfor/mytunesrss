package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import org.apache.commons.io.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
    private DefaultListModel myThemesListModel = new DefaultListModel();
    private DefaultListModel myLanguagesListModel = new DefaultListModel();

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
        myThemesList.setModel(myThemesListModel);
        myLanguagesList.setModel(myLanguagesListModel);
        for (String theme : AddonsUtils.getThemes()) {
            myThemesListModel.addElement(theme);
        }
        for (String language : AddonsUtils.getLanguages()) {
            myLanguagesListModel.addElement(language);
        }
        myThemesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myDeleteThemeButton.setEnabled(myThemesList.getSelectedIndex() > -1);
            }
        });
        myLanguagesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myDeleteLanguageButton.setEnabled(myLanguagesList.getSelectedIndex() > -1);
            }
        });
        myAddThemeButton.addActionListener(new AddButtonListener(MyTunesRss.BUNDLE.getString("dialog.lookupTheme")) {
            protected void add(File theme) {
                AddonsUtils.addTheme(theme);
            }
        });
        myDeleteThemeButton.addActionListener(new DeleteButtonListener(myThemesListModel) {
            protected String delete(String theme) {
                return AddonsUtils.deleteTheme(theme);
            }
        });
        myAddLanguageButton.addActionListener(new AddButtonListener(MyTunesRss.BUNDLE.getString("dialog.lookupLanguage")) {
            protected void add(File language) {
                AddonsUtils.addLanguage(language);
            }
        });
        myDeleteLanguageButton.addActionListener(new DeleteButtonListener(myLanguagesListModel) {
            protected String delete(String language) {
                return AddonsUtils.deleteLanguage(language);
            }
        });
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                myThemesList.setEnabled(false);
                myAddThemeButton.setEnabled(false);
                myDeleteThemeButton.setEnabled(false);
                myLanguagesList.setEnabled(false);
                myAddLanguageButton.setEnabled(false);
                myDeleteLanguageButton.setEnabled(false);
                break;
            case ServerIdle:
                myThemesList.setEnabled(true);
                myAddThemeButton.setEnabled(true);
                myDeleteThemeButton.setEnabled(myThemesList.getSelectedIndex() > -1);
                myLanguagesList.setEnabled(true);
                myAddLanguageButton.setEnabled(true);
                myDeleteLanguageButton.setEnabled(myLanguagesList.getSelectedIndex() > -1);
                break;
        }
    }

    public abstract class AddButtonListener implements ActionListener {
        private String myTitle;


        protected AddButtonListener(String title) {
            myTitle = title;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(myTitle);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File file) {
                    return file.isFile() && "zip".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
                }

                public String getDescription() {
                    return null;
                }
            });
            int result = fileChooser.showOpenDialog(MyTunesRss.ROOT_FRAME);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    add(fileChooser.getSelectedFile());
                } catch (Exception ex) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.lookupFile") + ex.getMessage());
                }
            }
        }

        protected abstract void add(File file) throws Exception;
    }

    public abstract class DeleteButtonListener implements ActionListener {
        private DefaultListModel myListModel;

        public DeleteButtonListener(DefaultListModel listModel) {
            myListModel = listModel;
        }

        public void actionPerformed(ActionEvent e) {
            int index = ((JList)e.getSource()).getSelectedIndex();
            if (index > -1 && index < myListModel.getSize()) {
                String error = delete(myListModel.get(index).toString());
                if (error == null) {
                    myListModel.remove(index);
                } else {
                    MyTunesRssUtils.showErrorMessage(error);
                }
            }
        }

        protected abstract String delete(String item);
    }
}