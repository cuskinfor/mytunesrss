package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * de.codewave.mytunesrss.settings.Addons
 */
public class Addons implements MyTunesRssEventListener, SettingsForm {
    private JPanel myRootPanel;
    private JButton myAddThemeButton;
    private JButton myDeleteThemeButton;
    private JScrollPane myThemesScrollPane;
    private JList myThemesList;
    private JButton myAddLanguageButton;
    private JButton myDeleteLanguageButton;
    private JScrollPane myLanguagesScrollPane;
    private JList myLanguagesList;
    private JButton myAddExternalSiteButton;
    private JButton myRemoveExternalSiteButton;
    private JList myExternalSitesList;
    private JScrollPane myExternalSitesScrollPane;
    private DefaultListModel myThemesListModel = new DefaultListModel();
    private DefaultListModel myLanguagesListModel = new DefaultListModel();
    private DefaultListModel myExternalSitesListModel = new DefaultListModel();

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
        myExternalSitesList = new JList() {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(0, 0);
            }
        };
    }

    public Addons() {
        myThemesScrollPane.setMaximumSize(myThemesScrollPane.getPreferredSize());
        myThemesScrollPane.getViewport().setOpaque(false);
        myLanguagesScrollPane.setMaximumSize(myLanguagesScrollPane.getPreferredSize());
        myLanguagesScrollPane.getViewport().setOpaque(false);
        myExternalSitesScrollPane.setMaximumSize(myExternalSitesScrollPane.getPreferredSize());
        myExternalSitesScrollPane.getViewport().setOpaque(false);
        myThemesList.setModel(myThemesListModel);
        myThemesList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setText(((AddonsUtils.ThemeDefinition) value).getName());
                String info = StringUtils.trimToNull(((AddonsUtils.ThemeDefinition) value).getInfo());
                label.setToolTipText(info != null ? "<html>" + info.replace("\n", "<br>") + "</html>" : null);
                return label;
            }
        });
        myLanguagesList.setModel(myLanguagesListModel);
        myLanguagesList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setText(((AddonsUtils.LanguageDefinition) value).getCode());
                String info = StringUtils.trimToNull(((AddonsUtils.LanguageDefinition) value).getInfo());
                label.setToolTipText(info != null ? "<html>" + info.replace("\n", "<br>") + "</html>" : null);
                return label;
            }
        });
        myExternalSitesList.setModel(myExternalSitesListModel);
        myExternalSitesList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ExternalSiteDefinition def = (ExternalSiteDefinition) value;
                label.setText(def.getType() + " - " + def.getName());
                label.setToolTipText(def.getUrl());
                return label;
            }
        });
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
        myExternalSitesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myRemoveExternalSiteButton.setEnabled(myExternalSitesList.getSelectedIndex() > -1);
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
        myAddExternalSiteButton.addActionListener(new AddExternalSiteButtonListener());
        myRemoveExternalSiteButton.addActionListener(new RemoveExternalSiteButtonListener());
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

    public void initValues() {
        initListModels();
    }

    private void initListModels() {
        myThemesListModel.clear();
        for (AddonsUtils.ThemeDefinition theme : AddonsUtils.getThemes(false)) {
            myThemesListModel.addElement(theme);
        }
        myLanguagesListModel.clear();
        for (AddonsUtils.LanguageDefinition language : AddonsUtils.getLanguages(false)) {
            myLanguagesListModel.addElement(language);
        }
        myExternalSitesListModel.clear();
        for (ExternalSiteDefinition def : MyTunesRss.CONFIG.getExternalSites()) {
            myExternalSitesListModel.addElement(def);
        }
    }

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        myDeleteThemeButton.setEnabled(!serverActive);
        myDeleteLanguageButton.setEnabled(!serverActive);
        myRemoveExternalSiteButton.setEnabled(!serverActive);
    }

    public String updateConfigFromGui() {
        // intentionally left blank
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.addons.title");
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

    public class AddExternalSiteButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        }
    }

    public class RemoveExternalSiteButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        }
    }
}