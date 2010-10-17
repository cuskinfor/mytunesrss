package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.DatasourceConfig;
import de.codewave.mytunesrss.DatasourceType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.io.ZipUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.zip.ZipOutputStream;

/**
 * de.codewave.mytunesrss.task.SendSupportRequestCallable
 */
public class SendSupportRequestCallable implements Callable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(SendSupportRequestCallable.class);
    private static final String SUPPORT_URL = "http://www.codewave.de/tools/support.php";

    private boolean myIncludeItunesXml;
    private String myEmail;
    private String myName;
    private String myComment;

    public SendSupportRequestCallable(String name, String email, String comment, boolean includeItunesXml) {
        myName = name;
        myEmail = email;
        myComment = comment;
        myIncludeItunesXml = includeItunesXml;
    }

    public Boolean call() {
        ZipOutputStream zipOutput = null;
        PostMethod postMethod = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            zipOutput = new ZipOutputStream(baos);
            String archiveName = "MyTunesRSS_" + MyTunesRss.VERSION + "_Support" + (StringUtils.isNotBlank(myName) ? "_" + StringUtils.trim(myName) : "");
            ZipUtils.addToZip(archiveName + "/MyTunesRSS.log", new File(MyTunesRssUtils.getCacheDataPath() + "/MyTunesRSS.log"), zipOutput);
            if (myIncludeItunesXml) {
                int index = 0;
                for (DatasourceConfig dataSource : MyTunesRss.CONFIG.getDatasources()) {
                    if (dataSource.getType() == DatasourceType.Itunes) {
                        File file = new File(dataSource.getDefinition());
                        if (index == 0) {
                            ZipUtils.addToZip(archiveName + "/iTunes Music Library.xml", file, zipOutput);
                        } else {
                            ZipUtils.addToZip(archiveName + "/iTunes Music Library (" + index + ").xml", file, zipOutput);
                        }
                        index++;
                    }
                }
            }
            zipOutput.close();
            postMethod = new PostMethod(System.getProperty("MyTunesRSS.supportUrl", SUPPORT_URL));
            PartSource partSource = new ByteArrayPartSource(archiveName + ".zip", baos.toByteArray());
            Part[] part = new Part[]{new StringPart("mailSubject", "MyTunesRSS v" + MyTunesRss.VERSION + " Support Request"), new StringPart("name",
                    myName),
                    new StringPart("email", myEmail), new StringPart("comment", myComment), new FilePart("archive", partSource)};
            MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(part, postMethod.getParams());
            postMethod.setRequestEntity(multipartRequestEntity);
            HttpClient httpClient = MyTunesRssUtils.createHttpClient();
            httpClient.executeMethod(postMethod);
            int statusCode = postMethod.getStatusCode();
            if (statusCode == 200) {
                return Boolean.TRUE;
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
        return Boolean.FALSE;

    }
}
