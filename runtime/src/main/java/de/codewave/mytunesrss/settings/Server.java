/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.network.MulticastService;
import de.codewave.utils.swing.*;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Server settings panel
 */
public class Server implements MyTunesRssEventListener, SettingsForm {
    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JCheckBox myAutoStartServerInput;
    private JTextField myServerNameInput;
    private JCheckBox myAvailableOnLocalNetInput;
    private JCheckBox myTempZipArchivesInput;
    private JTextField myHttpProxyHostInput;
    private JTextField myHttpProxyPortInput;
    private JTextField myHttpsPortInput;
    private JTextField myHttpsProxyHostInput;
    private JTextField myHttpsProxyPortInput;
    private JTextField myKeystoreInput;
    private JPasswordField myKeystorePasswordInput;
    private JTextField myKeystoreAliasInput;
    private JButton mySelectKeystoreButton;
    private JTextField myMaxThreadsInput;
    private JTextField myAjpPortInput;
    private JButton myAddContextButton;
    private JButton myRemoveContextButton;
    private JScrollPane myAdditionContextsScrollpane;
    private JTable myAdditionalContextsTable;
    private JTextField myContextInput;
    private JComboBox myHttpProxySchemeInput;
    private JComboBox myHttpsProxySchemeInput;
    private File myFileChooserDierctory;

    public void init() {
        initValues();
        MyTunesRssEventManager.getInstance().addListener(this);
        myAutoStartServerInput.addActionListener(new AutoStartServerInputListener());
        myAvailableOnLocalNetInput.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (MyTunesRss.WEBSERVER.isRunning()) {
                    if (myAvailableOnLocalNetInput.isSelected()) {
                        MulticastService.startListener();
                    } else {
                        MulticastService.stopListener();
                    }
                }
                SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected() && !MyTunesRss.WEBSERVER.isRunning());
            }
        });
        if (myAutoStartServerInput.isSelected()) {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.ENABLE_AUTO_START_SERVER);
        } else {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DISABLE_AUTO_START_SERVER);
        }
        myAdditionContextsScrollpane.getViewport().setOpaque(false);
        myAdditionalContextsTable.setModel(new AdditionalContextsTableModel());
        myRemoveContextButton.addActionListener(new DeleteAddCtxActionListener());
        myAddContextButton.addActionListener(new AddAddCtxActionListener());
        myAdditionalContextsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myRemoveContextButton.setEnabled(myAdditionalContextsTable.getSelectedRow() > -1);
            }
        });
        myAdditionalContextsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && myAdditionalContextsTable.getSelectedRow() > -1) {
                    new EditAdditionalContext().display(MyTunesRss.ROOT_FRAME, myAdditionalContextsTable.getSelectedRow());
                    ((AbstractTableModel)myAdditionalContextsTable.getModel()).fireTableDataChanged();
                }
            }
        });
        mySelectKeystoreButton.addActionListener(new SelectKeystoreButtonListener());
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myPortInput, 1, 65535, false, MyTunesRssUtils.getBundleString(
                "error.illegalServerPort")));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myServerNameInput,
                                                                           MyTunesRssUtils.getBundleString("error.emptyServerName")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myHttpsPortInput, 1, 65535, true, MyTunesRssUtils.getBundleString(
                "error.illegalHttpsPort")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myHttpProxyPortInput, 1, 65535, true, MyTunesRssUtils.getBundleString(
                "error.illegalHttpProxyPort")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myHttpsProxyPortInput, 1, 65535, true, MyTunesRssUtils.getBundleString(
                "error.illegalHttpsProxyPort")));
        JTextFieldValidation.setValidation(new CompositeTextFieldValidation(myContextInput,
                                                                            new WebAppContextValidValidation(),
                                                                            new DuplicateWebAppContextValidation()));
        JTextFieldValidation.validateAll(myRootPanel);
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
        myAutoStartServerInput.setSelected(MyTunesRss.CONFIG.isAutoStartServer());
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        myServerNameInput.setText(MyTunesRss.CONFIG.getServerName());
        myAvailableOnLocalNetInput.setSelected(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected());
        myTempZipArchivesInput.setSelected(MyTunesRss.CONFIG.isLocalTempArchive());
        myHttpProxyHostInput.setText(MyTunesRss.CONFIG.getTomcatProxyHost());
        myHttpProxyPortInput.setText(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getTomcatProxyPort(), 1, 65535, null));
        myHttpsPortInput.setText(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getSslPort(), 1, 65535, null));
        myHttpsProxyHostInput.setText(MyTunesRss.CONFIG.getTomcatSslProxyHost());
        myHttpsProxyPortInput.setText(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getTomcatSslProxyPort(), 1, 65535, null));
        myKeystoreInput.setText(MyTunesRss.CONFIG.getSslKeystoreFile());
        myKeystorePasswordInput.setText(MyTunesRss.CONFIG.getSslKeystorePass());
        myKeystoreAliasInput.setText(MyTunesRss.CONFIG.getSslKeystoreKeyAlias());
        myMaxThreadsInput.setText(MyTunesRss.CONFIG.getTomcatMaxThreads());
        myAjpPortInput.setText(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getTomcatAjpPort(), 1, 65535, null));
        myContextInput.setText(MyTunesRss.CONFIG.getWebappContext());
        myHttpProxySchemeInput.addItem("HTTP");
        myHttpProxySchemeInput.addItem("HTTPS");
        myHttpsProxySchemeInput.addItem("HTTPS");
        myHttpsProxySchemeInput.addItem("HTTP");
        myHttpProxySchemeInput.setSelectedItem(StringUtils.defaultIfEmpty(MyTunesRss.CONFIG.getTomcatProxyScheme(), "HTTP").toUpperCase());
        myHttpsProxySchemeInput.setSelectedItem(StringUtils.defaultIfEmpty(MyTunesRss.CONFIG.getTomcatSslProxyScheme(), "HTTPS").toUpperCase());
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages == null) {
            MyTunesRss.CONFIG.setPort(MyTunesRssUtils.getTextFieldInteger(myPortInput, -1));
            MyTunesRss.CONFIG.setAutoStartServer(myAutoStartServerInput.isSelected());
            MyTunesRss.CONFIG.setServerName(myServerNameInput.getText());
            MyTunesRss.CONFIG.setAvailableOnLocalNet(myAvailableOnLocalNetInput.isSelected());
            MyTunesRss.CONFIG.setLocalTempArchive(myTempZipArchivesInput.isSelected());
            MyTunesRss.CONFIG.setTomcatProxyHost(myHttpProxyHostInput.getText());
            MyTunesRss.CONFIG.setTomcatProxyPort(MyTunesRssUtils.getStringInteger(myHttpProxyPortInput.getText(), 0));
            MyTunesRss.CONFIG.setSslPort(MyTunesRssUtils.getStringInteger(myHttpsPortInput.getText(), 0));
            MyTunesRss.CONFIG.setTomcatSslProxyHost(myHttpsProxyHostInput.getText());
            MyTunesRss.CONFIG.setTomcatSslProxyPort(MyTunesRssUtils.getStringInteger(myHttpsProxyPortInput.getText(), 0));
            MyTunesRss.CONFIG.setSslKeystoreFile(myKeystoreInput.getText());
            MyTunesRss.CONFIG.setSslKeystorePass(new String(myKeystorePasswordInput.getPassword()));
            MyTunesRss.CONFIG.setSslKeystoreKeyAlias(myKeystoreAliasInput.getText());
            MyTunesRss.CONFIG.setTomcatAjpPort(MyTunesRssUtils.getStringInteger(myAjpPortInput.getText(), 0));
            MyTunesRss.CONFIG.setTomcatMaxThreads(myMaxThreadsInput.getText());
            MyTunesRss.CONFIG.setWebappContext(myContextInput.getText());
            MyTunesRss.CONFIG.setTomcatProxyScheme(myHttpProxySchemeInput.getSelectedItem().toString().toLowerCase());
            MyTunesRss.CONFIG.setTomcatSslProxyScheme(myHttpsProxySchemeInput.getSelectedItem().toString().toLowerCase());
        }
        return messages;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        SwingUtils.enableElementAndLabel(myPortInput, !serverActive);
        SwingUtils.enableElementAndLabel(myHttpProxySchemeInput, !serverActive);
        SwingUtils.enableElementAndLabel(myHttpProxyHostInput, !serverActive);
        SwingUtils.enableElementAndLabel(myHttpProxyPortInput, !serverActive);
        SwingUtils.enableElementAndLabel(myHttpsPortInput, !serverActive);
        SwingUtils.enableElementAndLabel(myHttpsProxySchemeInput, !serverActive);
        SwingUtils.enableElementAndLabel(myHttpsProxyHostInput, !serverActive);
        SwingUtils.enableElementAndLabel(myHttpsProxyPortInput, !serverActive);
        SwingUtils.enableElementAndLabel(myKeystoreInput, !serverActive);
        mySelectKeystoreButton.setEnabled(!serverActive);
        SwingUtils.enableElementAndLabel(myKeystorePasswordInput, !serverActive);
        SwingUtils.enableElementAndLabel(myKeystoreAliasInput, !serverActive);
        myAutoStartServerInput.setEnabled(!serverActive);
        myTempZipArchivesInput.setEnabled(!serverActive);
        SwingUtils.enableElementAndLabel(myServerNameInput, !serverActive && myAvailableOnLocalNetInput.isSelected());
        SwingUtils.enableElementAndLabel(myAjpPortInput, !serverActive);
        SwingUtils.enableElementAndLabel(myContextInput, !serverActive);
        SwingUtils.enableElementAndLabel(myMaxThreadsInput, !serverActive);
        SwingUtils.enableElementAndLabel(myAddContextButton, !serverActive);
        SwingUtils.enableElementAndLabel(myRemoveContextButton, !serverActive && !myAdditionalContextsTable.getSelectionModel().isSelectionEmpty());
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.server.title");
    }

    public class AutoStartServerInputListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (myAutoStartServerInput.isSelected()) {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.ENABLE_AUTO_START_SERVER);
            } else {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DISABLE_AUTO_START_SERVER);
            }
            myRootPanel.validate();
        }
    }

    public class AdditionalContextsTableModel extends AbstractTableModel {
        public int getRowCount() {
            return MyTunesRss.CONFIG.getAdditionalContexts().size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return MyTunesRss.CONFIG.getAdditionalContexts().get(rowIndex).split(":")[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public String getColumnName(int column) {
            return MyTunesRssUtils.getBundleString("settings.server.addCtxHeader." + column);
        }
    }

    public class DeleteAddCtxActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int row = myAdditionalContextsTable.getSelectedRow();
            if (row > -1) {
                String context = MyTunesRss.CONFIG.getAdditionalContexts().get(row);
                int result = JOptionPane.showConfirmDialog(myRootPanel, MyTunesRssUtils.getBundleString("confirmation.deleteAddCtx",
                                                                                                        context.split(":")[0]),
                                                           MyTunesRssUtils.getBundleString("confirmation.titleDeleteAddCtx"),
                                                           JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    MyTunesRss.CONFIG.getAdditionalContexts().remove(row);
                    ((AbstractTableModel)myAdditionalContextsTable.getModel()).fireTableDataChanged();
                }
            }
        }
    }

    public class AddAddCtxActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new EditAdditionalContext().display(MyTunesRss.ROOT_FRAME, -1);
            ((AbstractTableModel)myAdditionalContextsTable.getModel()).fireTableDataChanged();
        }
    }

    public class WebAppContextValidValidation extends JTextFieldValidation {
        protected WebAppContextValidValidation() {
            super(myContextInput, MyTunesRssUtils.getBundleString("error.invalidAddCtx"));
        }

        protected boolean isValid(String text) {
            return !StringUtils.isNotEmpty(text) || text.startsWith("/") && text.length() > 1;
        }
    }

    public class DuplicateWebAppContextValidation extends JTextFieldValidation {
        protected DuplicateWebAppContextValidation() {
            super(myContextInput, MyTunesRssUtils.getBundleString("error.duplicateAddCtx"));
        }

        protected boolean isValid(String text) {
            if (StringUtils.isNotEmpty(text)) {
                for (String ctx : MyTunesRss.CONFIG.getAdditionalContexts()) {
                    if (ctx.split(":")[0].equals(text)) {
                        return false;
                    }
                }
            } else {
                for (String ctx : MyTunesRss.CONFIG.getAdditionalContexts()) {
                    if ("/".equals(ctx.split(":")[0])) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public class SelectKeystoreButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setCurrentDirectory(myFileChooserDierctory);
            fileChooser.setDialogTitle(MyTunesRssUtils.getBundleString("dialog.lookupKeystore"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File file) {
                    return true;
                }

                public String getDescription() {
                    return MyTunesRssUtils.getBundleString("filechooser.filter.keystore");
                }
            });
            int result = fileChooser.showDialog(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("filechooser.approve.keystore"));
            if (result == JFileChooser.APPROVE_OPTION) {
                myFileChooserDierctory = fileChooser.getCurrentDirectory();
                myKeystoreInput.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
}