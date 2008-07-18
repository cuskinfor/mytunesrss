package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssTask;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.io.ZipUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * de.codewave.mytunesrss.task.SendSupportRequestTask
 */
public class SendSupportRequestTask extends MyTunesRssTask {
    private static final Logger LOG = LoggerFactory.getLogger(SendSupportRequestTask.class);
    private static final String SUPPORT_URL = "http://www.codewave.de/tools/support.php";

    private boolean mySuccess;
    private boolean myIncludeItunesXml;
    private String myEmail;
    private String myName;
    private String myComment;

    public SendSupportRequestTask(String name, String email, String comment, boolean includeItunesXml) {
        myName = name;
        myEmail = email;
        myComment = comment;
        myIncludeItunesXml = includeItunesXml;
    }

    public boolean isSuccess() {
        return mySuccess;
    }

    public void execute() {
        ZipOutputStream zipOutput = null;
        PostMethod postMethod = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            zipOutput = new ZipOutputStream(baos);
            ZipUtils.addToZip("MyTunesRSS_Support/MyTunesRSS-" + MyTunesRss.VERSION + ".log", new File(
                    PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/MyTunesRSS.log"), zipOutput);
            if (myIncludeItunesXml) {
                int index = 0;
                for (String dataSource : MyTunesRss.CONFIG.getDatasources()) {
                    File file = new File(dataSource);
                    if (file.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(dataSource))) {
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
            Part[] part = new Part[] {new StringPart("mailSubject", "MyTunesRSS v" + MyTunesRss.VERSION + " Support Request"), new StringPart("name",
                                                                                                                                              myName),
                                      new StringPart("email", myEmail), new StringPart("comment", myComment), new FilePart("archive", partSource)};
            MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(part, postMethod.getParams());
            postMethod.setRequestEntity(multipartRequestEntity);
            HttpClient httpClient = MyTunesRssUtils.createHttpClient();
            httpClient.executeMethod(postMethod);
            int statusCode = postMethod.getStatusCode();
            if (statusCode == 200) {
                mySuccess = true;
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
