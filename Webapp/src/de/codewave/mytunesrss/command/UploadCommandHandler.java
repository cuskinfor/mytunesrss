/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.swing.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * de.codewave.mytunesrss.command.UploadCommandHandler
 */
public class UploadCommandHandler extends MyTunesRssCommandHandler {
    private static final Log LOG = LogFactory.getLog(UploadCommandHandler.class);

    @Override
    public void execute() throws Exception {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(getRequest());
        for (FileItem item : items) {
            processItem(item);
        }
        TaskExecutor.execute(MyTunesRss.DATABASE_BUILDER_TASK, new TaskFinishedListener() {
            public void taskFinished(Task task) {
                // intentionally left blank
            }
        });
        forward(MyTunesRssResource.DatabaseUpdating);
    }

    private void processItem(FileItem item) throws IOException {
        if ("file".equals(item.getFieldName())) {
            if (StringUtils.isNotEmpty(item.getName())) {
                if (item.getName().toLowerCase().endsWith(".zip")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Extracting zip file \"" + item.getName() + "\".");
                    }
                    MyTunesRssZipInputStream zipInputStream = new MyTunesRssZipInputStream(item.getInputStream());
                    for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                        saveFile(entry.getName(), zipInputStream);
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

    private void saveFile(String fileName, InputStream inputStream) throws IOException {
        if (StringUtils.isNotEmpty(fileName) && !fileName.endsWith("/") && !fileName.endsWith("\\") && !fileName.contains("/__MACOSX/")) {
            String uploadDirName = MyTunesRss.CONFIG.getUploadDir();
            if (MyTunesRss.CONFIG.isUploadCreateUserDir()) {
                uploadDirName += "/" + getWebConfig().getUserName();
            }
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
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                FileOutputStream targetStream = new FileOutputStream(new File(uploadDir, fileName));
                byte[] buffer = new byte[10240];
                for (int count = inputStream.read(buffer); count != -1; count = inputStream.read(buffer)) {
                    if (count > 0) {
                        targetStream.write(buffer, 0, count);
                    }
                }
                targetStream.close();
            }
        }
    }
}
