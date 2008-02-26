package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.*;
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
public class Addons implements MyTunesRssEventListener {
    private JPanel myRootPanel;
    private JButton myAddThemeButton;
    private JButton myDeleteThemeButton;
    private JScrollPane myThemesScrollPane;
    private JList myThemesList;
    private JButton myAddLanguageButton;
    private JButton myDeleteLanguageButton;
    private JScrollPane myLanguagesScrollPane;
    private JList myLanguagesList;
    private JTextField myWelcomeMessageInput;
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
        myAddThemeButton.addActionListener(new AddButtonListener(MyTunesRssUtils.getBundleString("dialog.lookupTheme"),
                                                                 MyTunesRssUtils.getBundleString("pleaseWait.addingTheme")) {
            protected String add(File theme) {
                return AddonsUtils.addTheme(theme);
            }
        });
        myDeleteThemeButton.addActionListener(new DeleteButtonListener(myThemesListModel, myThemesList) {
            protected String delete(String theme) {
                return AddonsUtils.deleteTheme(theme);
            }
        });
        myAddLanguageButton.addActionListener(new AddButtonListener(MyTunesRssUtils.getBundleString("dialog.lookupLanguage"),
                                                                    MyTunesRssUtils.getBundleString("pleaseWait.addingLanguage")) {
            protected String add(File language) {
                return AddonsUtils.addLanguage(language);
            }
        });
        myDeleteLanguageButton.addActionListener(new DeleteButtonListener(myLanguagesListModel, myLanguagesList) {
            protected String delete(String language) {
                return AddonsUtils.deleteLanguage(language);
            }
        });
        initValues();
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event) {
                    case CONFIGURATION_CHANGED:
                        initValues();
                        break;
                    case DATABASE_UPDATE_STATE_CHANGED:
                        setGuiMode(GuiMode.DatabaseUpdating);
                        break;
                    case DATABASE_UPDATE_FINISHED:
                    case DATABASE_UPDATE_FINISHED_NOT_RUN:
                        setGuiMode(GuiMode.DatabaseIdle);
                        break;
                    case SERVER_STARTED:
                        setGuiMode(GuiMode.ServerRunning);
                        break;
                    case SERVER_STOPPED:
                        setGuiMode(GuiMode.ServerIdle);
                        break;
                }
            }
        });
    }

    private void initValues() {
        initListModels();
        myWelcomeMessageInput.setText(MyTunesRss.CONFIG.getWebWelcomeMessage());
    }

    private void initListModels() {
        myThemesListModel.clear();
        for (String theme : AddonsUtils.getThemes()) {
            myThemesListModel.addElement(theme);
        }
        myLanguagesListModel.clear();
        for (String language : AddonsUtils.getLanguages()) {
            myLanguagesListModel.addElement(language);
        }
    }

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        myDeleteThemeButton.setEnabled(!serverActive);
        myDeleteLanguageButton.setEnabled(!serverActive);
    }

    public String updateConfigFromGui() {
        MyTunesRss.CONFIG.setWebWelcomeMessage(myWelcomeMessageInput.getText());
        return null;
    }

    public abstract class AddButtonListener implements ActionListener {
        private String myTitle;
        private String myPleaseWaitText;

        protected AddButtonListener(String title, String pleaseWaitText) {
            myTitle = title;
            myPleaseWaitText = pleaseWaitText;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(myTitle);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory() || file.isFile() && "zip".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
                }

                public String getDescription() {
                    return MyTunesRssUtils.getBundleString("filechooser.filter.addons");
                }
            });
            int result = fileChooser.showDialog(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("filechooser.approve.addons"));
            if (result == JFileChooser.APPROVE_OPTION) {
                final File selectedFile = fileChooser.getSelectedFile();
                MyTunesRssUtils.executeTask(null, myPleaseWaitText, null, false, new MyTunesRssTask() {
                    public void execute() throws Exception {
                        try {
                            String error = add(selectedFile);
                            if (error != null) {
                                MyTunesRssUtils.showErrorMessage(error);
                            } else {
                                initListModels();
                            }
                        } catch (Exception ex) {
                            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.lookupFile", ex.getMessage()));
                        }
                    }
                });
            }
        }

        protected abstract String add(File file) throws Exception;
    }

    public abstract class DeleteButtonListener implements ActionListener {
        private DefaultListModel myListModel;
        private JList myList;

        public DeleteButtonListener(DefaultListModel listModel, JList list) {
            myListModel = listModel;
            myList = list;
        }

        public void actionPerformed(ActionEvent e) {
            int index = myList.getSelectedIndex();
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