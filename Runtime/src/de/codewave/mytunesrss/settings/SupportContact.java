package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.*;
import de.codewave.utils.io.*;
import de.codewave.utils.swing.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.apache.commons.io.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.zip.*;

/**
 * de.codewave.mytunesrss.settings.SupportContact
 */
public class SupportContact {
    private static final Log LOG = LogFactory.getLog(SupportContact.class);
    private static final String SUPPORT_URL = "http://www.codewave.de/tools/support.php";

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
            SendSupportRequestTask requestTask = new SendSupportRequestTask();
            if (!MyTunesRss.CONFIG.isProxyServer() ||
                    (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getProxyHost()) && MyTunesRss.CONFIG.getProxyPort() > 0)) {
                MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.sendingSupportRequest"), null, false, requestTask);
                if (requestTask.isSuccess()) {
                    MyTunesRssUtils.showInfoMessage(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("info.supportRequestSent"));
                    myDialog.dispose();
                } else {
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.couldNotSendSupportRequest"));
                }
            } else {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.illegalProxySettings"));
            }
        }
    }

    public class SendSupportRequestTask extends MyTunesRssTask {
        private boolean success = false;

        public boolean isSuccess() {
            return success;
        }

        public void execute() {
            ZipOutputStream zipOutput = null;
            PostMethod postMethod = null;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                zipOutput = new ZipOutputStream(baos);
                ZipUtils.addToZip("MyTunesRSS_Support/MyTunesRSS-" + MyTunesRss.VERSION + ".log", new File(
                        PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/MyTunesRSS.log"), zipOutput);
                if (myItunesXmlInput.isSelected()) {
                    int index = 0;
                    for (String dataSource : MyTunesRss.CONFIG.getDatasources()) {
                        File file = new File(dataSource);
                        if (file.exists() && file.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(dataSource))) {
                            if (index == 0) {
                                ZipUtils.addToZip("MyTunesRSS_Support/iTunes Music Library.xml", file, zipOutput);
                            } else {
                                ZipUtils.addToZip("MyTunesRSS_Support/iTunes Music Library (" + index + ").xml", file, zipOutput);
                            }
                            index++;
                        }
                    }
                }
                zipOutput.close();
                postMethod = new PostMethod(System.getProperty("MyTunesRSS.supportUrl", SUPPORT_URL));
                PartSource partSource = new ByteArrayPartSource("MyTunesRSS-" + MyTunesRss.VERSION + "-Support.zip", baos.toByteArray());
                Part[] part = new Part[] {new StringPart("mailSubject", "MyTunesRSS v" + MyTunesRss.VERSION + " Support Request"), new StringPart(
                        "name",
                        myNameInput.getText()), new StringPart("email", myEmailInput.getText()), new StringPart("comment", myCommentInput.getText()),
                                                new FilePart("archive", partSource)};
                MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(part, postMethod.getParams());
                postMethod.setRequestEntity(multipartRequestEntity);
                HttpClient httpClient = MyTunesRssUtils.createHttpClient();
                httpClient.executeMethod(postMethod);
                int statusCode = postMethod.getStatusCode();
                if (statusCode == 200) {
                    success = true;
                } else {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not send support request (status code was " + statusCode + ").");
                    }
                }
            } catch (IOException e1) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not send support request.", e1);
                }
            } finally {
                if (zipOutput != null) {
                    try {
                        zipOutput.close();
                    } catch (IOException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not close output file.", e1);
                        }
                    }
                }
                if (postMethod != null) {
                    postMethod.releaseConnection();
                }
            }

        }
    }
}