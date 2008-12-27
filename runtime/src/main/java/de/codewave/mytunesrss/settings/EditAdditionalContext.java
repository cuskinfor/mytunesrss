package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.swing.CompositeTextFieldValidation;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.SwingUtils;
import de.codewave.utils.swing.components.FileSelectionTextField;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.settings.EditAdditionalContext
 */
public class EditAdditionalContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditAdditionalContext.class);

    private JTextField myCtxNameInput;
    private FileSelectionTextField myDocRootInput;
    private JButton myCancelButton;
    private JButton myOkButton;
    private JPanel myRootPanel;
    private int myIndex;
    private JDialog myDialog;

    public void display(final JFrame parent, int index) {
        myDialog = new JDialog(parent, MyTunesRssUtils.getBundleString(index != -1 ? "editAddCtx.editTitle" : "editAddCtx.addTitle"), true);
        myDialog.add(myRootPanel);
        myDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        myDialog.setResizable(false);
        init(index);
        SwingUtils.packAndShowRelativeTo(myDialog, parent);
    }

    private void init(int index) {
        myIndex = index;
        if (index > -1) {
            String context = MyTunesRss.CONFIG.getAdditionalContexts().get(index);
            myCtxNameInput.setText(context.split(":")[0]);
            myDocRootInput.setFile(new File(context.split(":")[1]));
        }
        JTextFieldValidation.setValidation(new CompositeTextFieldValidation(myCtxNameInput,
                                                                            new ValidContextTextFieldValidation(),
                                                                            new DuplicateContextTextFieldValidation()));
        JTextFieldValidation.validateAll(myRootPanel);
        myOkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
                if (messages == null) {
                    try {
                        if (myIndex == -1) {
                            MyTunesRss.CONFIG.getAdditionalContexts().add(
                                    myCtxNameInput.getText() + ":" + myDocRootInput.getFile().getCanonicalPath());
                        } else {
                            MyTunesRss.CONFIG.getAdditionalContexts().remove(myIndex);
                            MyTunesRss.CONFIG.getAdditionalContexts().add(myIndex,
                                                                          myCtxNameInput.getText() + ":" +
                                                                                  myDocRootInput.getFile().getCanonicalPath());
                        }
                    } catch (IOException e) {
                        LOGGER.warn("Could not get canonical path.", e);
                    }
                    myDialog.dispose();
                } else {
                    MyTunesRssUtils.showErrorMessage(messages);
                }
            }
        });
        myCancelButton.addActionListener(new CancelButtonActionListener());
    }

    private void createUIComponents() {
        myDocRootInput = new FileSelectionTextField(myRootPanel,
                                                    10,
                                                    null,
                                                    MyTunesRssUtils.getBundleString("dialog.lookupDocRootDir"),
                                                    MyTunesRssUtils.getBundleString("filechooser.approve.docroot"),
                                                    new DocRootSelectionSupport());
    }

    public class CancelButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(MyTunesRss.ROOT_FRAME,
                                              MyTunesRssUtils.getBundleString("confirm.cancelEditAddCtx"),
                                              MyTunesRssUtils.getBundleString("confirm.cancelEditAddCtxTitle"),
                                              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                myDialog.dispose();
            }
        }
    }

    public class ValidContextTextFieldValidation extends JTextFieldValidation {

        public ValidContextTextFieldValidation() {
            super(myCtxNameInput, MyTunesRssUtils.getBundleString("error.invalidAddCtx"));
        }

        protected boolean isValid(String text) {
            return StringUtils.isNotEmpty(text) && text.startsWith("/") && (text.length() == 1 || text.charAt(1) != '/');
        }
    }

    public class DuplicateContextTextFieldValidation extends JTextFieldValidation {

        public DuplicateContextTextFieldValidation() {
            super(myCtxNameInput, MyTunesRssUtils.getBundleString("error.duplicateAddCtx"));
        }

        protected boolean isValid(String text) {
            if (StringUtils.isNotEmpty(text)) {
                int i = 0;
                for (String context : MyTunesRss.CONFIG.getAdditionalContexts()) {
                    if (i != myIndex) {
                        if (context.startsWith(text + ":")) {
                            return false;
                        } else if (("/" + MyTunesRss.CONFIG.getWebappContext()).equals(text)) {
                            return false;
                        }
                    }
                    i++;
                }
            }
            return true;
        }
    }

    public class DocRootSelectionSupport implements FileSelectionTextField.FileSelectionTextFieldSupport {
        public boolean accept(File file) {
            return file.isDirectory() || (file.isFile() && "war".equalsIgnoreCase(FilenameUtils.getExtension(file.getName())));
        }

        public String getFileFilterDescription() {
            return MyTunesRssUtils.getBundleString("filechooser.filter.docroot");
        }

        public void handleSelect(File file) {
            // intentionally left blank
        }

        public boolean isValid(String text) {
            File file = new File(text);
            return StringUtils.isNotEmpty(text) && file.isDirectory() || (file.isFile() && file.getName().toLowerCase().endsWith(".war"));

        }

        public String getValidationFailureMessage() {
            return MyTunesRssUtils.getBundleString("error.invalidDocRoot");
        }
    }
}