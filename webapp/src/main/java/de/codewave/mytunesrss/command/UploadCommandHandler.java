/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.mytunesrss.statistics.UploadEvent;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.ProgressRequestWrapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.CodewaveZipInputStream;
import java.util.zip.CodewaveZipInputStreamFactory;
import java.util.zip.ZipEntry;

/**
 * de.codewave.mytunesrss.command.UploadCommandHandler
 */
public class UploadCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UploadCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> items = upload.parseRequest(new ProgressRequestWrapper(getRequest()));
            StringBuilder info = new StringBuilder();
            for (FileItem item : items) {
                processItem(item);
                StatisticsEventManager.getInstance().fireEvent(new UploadEvent(getAuthUser(), item.getSize()));
                info.append(item.getName()).append("\n");
            }
            runDatabaseUpdate();
            MyTunesRss.ADMIN_NOTIFY.notifyWebUpload(getAuthUser(), info.toString());
            forward(MyTunesRssResource.UploadFinished);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

    private void processItem(FileItem item) throws IOException {
        if ("file".equals(item.getFieldName())) {
            if (StringUtils.isNotEmpty(item.getName())) {
                if (item.getName().toLowerCase().endsWith(".zip")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Extracting zip file \"" + item.getName() + "\".");
                    }
                    CodewaveZipInputStream zipInputStream = CodewaveZipInputStreamFactory.newInstance(item.getInputStream());
                    String lineSeparator = System.getProperty("line.separator");
                    StringBuilder m3uPlaylist = new StringBuilder("#EXTM3U").append(lineSeparator);
                    for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                        if (saveFile(entry.getName(), (InputStream)zipInputStream)) {
                            m3uPlaylist.append(entry.getName().replace('\\', '/')).append(lineSeparator);
                        }
                    }
                    String filename = getUpoadDirName() + "/" + FilenameUtils.getBaseName(item.getName()) + ".m3u";
                    File file = new File(filename);
                    if (!file.exists()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Creating M3U playlist for all files of ZIP archive: \"" + filename + "\".");
                        }
                        FileUtils.writeByteArrayToFile(file, m3uPlaylist.toString().getBytes());
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Uploading file \"" + item.getName() + "\".");
                    }
                    saveFile(item.getName(), item.getInputStream());
                }
            }
        }
    }

    private boolean saveFile(String fileName, InputStream inputStream) throws IOException {
        if (isAccepted(fileName)) {
            String uploadDirName = getUpoadDirName();
            if (fileName.contains("/")) {
                uploadDirName += "/" + fileName.substring(0, fileName.lastIndexOf("/"));
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            } else if (fileName.contains("\\")) {
                uploadDirName += "/" + fileName.substring(0, fileName.lastIndexOf("\\"));
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            }
            File uploadDir = new File(uploadDirName);
            if (!uploadDir.exists()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating upload directory \"" + uploadDir + "\".");
                }
                uploadDir.mkdirs();
            }
            if (uploadDir.isDirectory()) {
                FileOutputStream targetStream = new FileOutputStream(new File(uploadDir, fileName));
                try {
                    IOUtils.copy(inputStream, targetStream);
                } finally {
                    targetStream.close();
                }
            }
            return true;
        }
        return false;
    }

    private String getUpoadDirName() {
        String uploadDirName = MyTunesRss.CONFIG.getUploadDir();
        if (MyTunesRss.CONFIG.isUploadCreateUserDir()) {
            uploadDirName += "/" + getWebConfig().getUserName();
        }
        return uploadDirName;
    }

    private boolean isAccepted(String fileName) {
        return !(StringUtils.isEmpty(fileName) || fileName.endsWith("/") || fileName.endsWith("\\")) && !fileName.contains("__MACOSX/") &&
                FileSupportUtils.isSupported(fileName);
    }
}
