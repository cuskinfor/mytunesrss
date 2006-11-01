package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.io.*;
import de.codewave.utils.swing.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;
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
    private JCheckBox myItunesXmlInput;
    private JTextArea myInfoText;
    private JTextField myProxyHostInput;
    private JTextField myProxyPortInput;
    private JCheckBox myUseProxyInput;

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
        myUseProxyInput.setSelected(MyTunesRss.CONFIG.isProxyServer());
        myUseProxyInput.addActionListener(new UseProxyActionListener());
        SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
        SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
        myProxyHostInput.setText(MyTunesRss.CONFIG.getProxyHost());
        int port = MyTunesRss.CONFIG.getProxyPort();
        if (port > 0 && port < 65536) {
            myProxyPortInput.setText(Integer.toString(port));
        } else {
            myProxyPortInput.setText("");
        }
        myRootPanel.validate();
        mySendButton.addActionListener(new SendButtonActionListener());
        myCancelButton.addActionListener(new CancelButtonActionListener(dialog));
    }

    public class CancelButtonActionListener implements ActionListener {
        private JDialog myDialog;

        public CancelButtonActionListener(JDialog dialog) {
            myDialog = dialog;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            myDialog.dispose();
        }
    }

    public class SendButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MyTunesRss.CONFIG.setSupportName(myNameInput.getText());
            MyTunesRss.CONFIG.setSupportEmail(myEmailInput.getText());
            MyTunesRss.CONFIG.setProxyServer(myUseProxyInput.isSelected());
            MyTunesRss.CONFIG.setProxyHost(myProxyHostInput.getText());
            try {
                MyTunesRss.CONFIG.setProxyPort(Integer.parseInt(myProxyPortInput.getText()));
            } catch (NumberFormatException e1) {
                MyTunesRss.CONFIG.setProxyPort(-1);
            }
            if (!MyTunesRss.CONFIG.isProxyServer() || (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getProxyHost()) && MyTunesRss.CONFIG.getProxyPort() > 0)) {
                ZipOutputStream zipOutput = null;
                PostMethod postMethod = null;
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    zipOutput = new ZipOutputStream(baos);
                    ZipUtils.addToZip("MyTunesRSS.log", new File("MyTunesRSS.log"), zipOutput);
                    String iTunesXml = MyTunesRss.CONFIG.getLibraryXml();
                    if (myItunesXmlInput.isSelected() && StringUtils.isNotEmpty(iTunesXml)) {
                        File file = new File(iTunesXml);
                        if (file.exists()) {
                            ZipUtils.addToZip("iTunes Music Library.xml", file, zipOutput);
                        }
                    }
                    postMethod = new PostMethod("http://codewavesite/tools/support.php");
                    PartSource partSource = new ByteArrayPartSource("MyTunesRSS-Support.zip", baos.toByteArray());
                    Part[] part = new Part[] {new StringPart("mailSubject", "MyTunesRSS Support Request"), new StringPart("name", myNameInput.getText()),
                                              new StringPart("email", myEmailInput.getText()), new StringPart("comment", myCommentInput.getText()),
                                              new FilePart("archive", partSource)};
                    MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(part, postMethod.getParams());
                    postMethod.setRequestEntity(multipartRequestEntity);
                    HttpClient httpClient = new HttpClient();
                    if (MyTunesRss.CONFIG.isProxyServer()) {
                        HostConfiguration hostConfiguration = new HostConfiguration();
                        hostConfiguration.setProxy(MyTunesRss.CONFIG.getProxyHost(), MyTunesRss.CONFIG.getProxyPort());
                        httpClient.setHostConfiguration(hostConfiguration);
                    }
                    httpClient.executeMethod(postMethod);
                    int statusCode = postMethod.getStatusCode();
                    if (statusCode == 200) {
                        MyTunesRssUtils.showInfoMessage(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("info.supportRequestSent"));
                    } else {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not send support request (status code was " + statusCode + ").");
                        }
                        MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.couldNotSendSupportRequest"));
                    }
                } catch (IOException e1) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not send support request.", e1);
                    }
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.couldNotSendSupportRequest"));
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
            } else {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.illegalProxySettings"));
            }
        }
    }

    public class UseProxyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
            SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
        }
    }
}