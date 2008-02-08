package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.*;
import de.codewave.utils.io.*;
import de.codewave.utils.swing.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.zip.*;

/**
 * de.codewave.mytunesrss.settings.SupportContact
 */
public class SupportContact {
    private static final Log LOG = LogFactory.getLog(SupportContact.class);

    private JPanel myRootPanel;
    private JButton mySendButton;
    private JButton myCancelButton;
    private JTextField myNameInput;
    private JTextField myEmailInput;
    private JTextArea myCommentInput;
    private JTextArea myInfoText;
    private JCheckBox myItunesXmlInput;

    public void display(JFrame parent, String title, String infoText) {
        final JDialog dialog = new JDialog(parent, title, true);
        dialog.add(myRootPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        init(dialog, infoText);
        SwingUtils.packAndShowRelativeTo(dialog, parent);
    }

    private void init(JDialog dialog, String infoText) {
        myInfoText.setText(infoText);
        myNameInput.setText(MyTunesRss.CONFIG.getSupportName());
        myEmailInput.setText(MyTunesRss.CONFIG.getSupportEmail());
        mySendButton.addActionListener(new SendButtonActionListener(dialog));
        myCancelButton.addActionListener(new CancelButtonActionListener(dialog));
    }

    public static class CancelButtonActionListener implements ActionListener {
        private JDialog myDialog;

        public CancelButtonActionListener(JDialog dialog) {
            myDialog = dialog;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            myDialog.dispose();
        }
    }

    public class SendButtonActionListener implements ActionListener {
        JDialog myDialog;


        public SendButtonActionListener(JDialog dialog) {
            myDialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            MyTunesRss.CONFIG.setSupportName(myNameInput.getText());
            MyTunesRss.CONFIG.setSupportEmail(myEmailInput.getText());
            SendSupportRequestTask requestTask = new SendSupportRequestTask(myNameInput.getText(), myEmailInput.getText(), myCommentInput.getText(), myItunesXmlInput.isSelected());
            if (!MyTunesRss.CONFIG.isProxyServer() ||
                    (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getProxyHost()) && MyTunesRss.CONFIG.getProxyPort() > 0)) {
                MyTunesRssUtils.executeTask(null, MyTunesRssUtils.getBundleString("pleaseWait.sendingSupportRequest"), null, false, requestTask);
                if (requestTask.isSuccess()) {
                    MyTunesRssUtils.showInfoMessage(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("info.supportRequestSent"));
                    myDialog.dispose();
                } else {
                    MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.couldNotSendSupportRequest"));
                }
            } else {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.illegalProxySettings"));
            }
        }
    }
}