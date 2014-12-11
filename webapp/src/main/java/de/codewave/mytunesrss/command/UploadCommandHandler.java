/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.DatabaseJobRunningException;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.TikaUtils;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.config.DatasourceType;
import de.codewave.mytunesrss.config.ItunesDatasourceConfig;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.ProgressRequestWrapper;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.mytunesrss.statistics.UploadEvent;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * de.codewave.mytunesrss.command.UploadCommandHandler
 */
public class UploadCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UploadCommandHandler.class);
    public static final int UPDATE_MAX_WAIT_MILLIS = 3600 * 1000 * 10;
    public static final int BLOCKING_UPDATE_DELAY_MILLIS = 60000;
    public static final int FILE_CHECK_DELAY_MILLIS = 10000;

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            if (!getAuthUser().isUpload()) {
                throw new UnauthorizedException();
            }
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            Map<String, List<FileItem>> items = upload.parseParameterMap(new ProgressRequestWrapper(getRequest()));
            DatasourceConfig datasource = MyTunesRss.CONFIG.getDatasource(items.get("datasource").get(0).getString("UTF-8"));
            if (datasource == null || !datasource.isUpload() || !datasource.isUploadable()) {
                throw new BadRequestException("Datasource missing or not uploadable.");
            }
            List<File> uploadedItunesFiles = new ArrayList<>();
            StringBuilder info = new StringBuilder();
            int uploadCount = 0;
            long lastModified = datasource.getDefinitionLastModified();
            if (items.get("file") != null) {
                for (FileItem item : items.get("file")) {
                    if (item.getSize() > 0) {
                        uploadCount++;
                        uploadedItunesFiles.addAll(processItem(datasource, item));
                        StatisticsEventManager.getInstance().fireEvent(new UploadEvent(getAuthUser().getName(), items.size()));
                        info.append(item.getName()).append("\n");
                    }
                }
            }
            if (uploadCount == 0) {
                addError(new BundleError("upload.error.noFiles"));
                forward(MyTunesRssResource.RestartTopWindow);
            } else {
                triggerDatabaseUpdate(datasource, uploadedItunesFiles, lastModified);
                MyTunesRss.ADMIN_NOTIFY.notifyWebUpload(getAuthUser(), info.toString());
            }
            forward(MyTunesRssResource.RestartTopWindow);
        } else {
            throw new UnauthorizedException();
        }
    }

    // TODO: Each upload will trigger a db update. While one is waiting to be scheduled all subsequent ones should be ignored 
    private void triggerDatabaseUpdate(final DatasourceConfig datasource, final List<File> uploadedFiles, final long lastModifiedBeforeUpload) throws DatabaseJobRunningException {
        final long startTime = System.currentTimeMillis();
        MyTunesRss.EXECUTOR_SERVICE.schedule(new Runnable() {
            public void run() {
                // remove all files from the list that have been consumed by iTunes
                for (Iterator<File> iterFiles = uploadedFiles.iterator(); iterFiles.hasNext(); ) {
                    if (!iterFiles.next().exists()) {
                        iterFiles.remove();
                    }
                }
                if (!uploadedFiles.isEmpty() || datasource.getDefinitionLastModified() == lastModifiedBeforeUpload) {
                    // iTunes has not consumed all files yet or the XML has not been updated yet, so run the check again in 10 seconds
                    MyTunesRss.EXECUTOR_SERVICE.schedule(this, FILE_CHECK_DELAY_MILLIS, TimeUnit.MILLISECONDS);
                } else {
                    try {
                        // iTunes has consumed all files and the XML has been updated, so schedule a database update now
                        MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(Collections.singleton(datasource), false);
                    } catch (DatabaseJobRunningException ignored) {
                        // There is another database update running, so try to schedule the update again in 5 minutes
                        // unless we have been waiting for more than 10 hours, in this case simply quit
                        if (System.currentTimeMillis() - startTime <= UPDATE_MAX_WAIT_MILLIS) {
                            MyTunesRss.EXECUTOR_SERVICE.schedule(this, BLOCKING_UPDATE_DELAY_MILLIS, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    private List<File> processItem(DatasourceConfig datasource, FileItem item) throws IOException {
        List<File> uploadedItunesFiles = new ArrayList<>();
        if (StringUtils.isNotEmpty(item.getName())) {
            if (item.getName().toLowerCase().endsWith(".zip")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Extracting zip file \"" + item.getName() + "\".");
                }
                StringBuilder m3uPlaylist;
                try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(item.getInputStream())) {
                    String lineSeparator = System.getProperty("line.separator");
                    m3uPlaylist = new StringBuilder("#EXTM3U").append(lineSeparator);
                    for (ZipArchiveEntry entry = zipInputStream.getNextZipEntry(); entry != null; entry = zipInputStream.getNextZipEntry()) {
                        File file = saveFile(datasource, entry.getName(), zipInputStream);
                        if (file != null) {
                            m3uPlaylist.append(entry.getName().replace('\\', '/')).append(lineSeparator);
                            if (datasource.getType() == DatasourceType.Itunes) {
                                uploadedItunesFiles.add(file);
                            }
                        }
                    }
                }
                if (datasource.getType() == DatasourceType.Watchfolder) {
                    File file = new File(datasource.getDefinition(), FilenameUtils.getBaseName(item.getName()) + ".m3u");
                    if (!file.exists()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Creating M3U playlist for all files of ZIP archive: \"" + file.getAbsolutePath() + "\".");
                        }
                        FileUtils.writeByteArrayToFile(file, m3uPlaylist.toString().getBytes(Charset.forName("UTF-8")));
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Uploading file \"" + item.getName() + "\".");
                }
                try (InputStream inputStream = item.getInputStream()) {
                    File file = saveFile(datasource, item.getName(), inputStream);
                    if (datasource.getType() == DatasourceType.Itunes) {
                        uploadedItunesFiles.add(file);
                    }
                }
            }
        }
        return uploadedItunesFiles;
    }

    private File saveFile(DatasourceConfig datasource, String fileName, InputStream inputStream) throws IOException {
        Metadata metadata = TikaUtils.extractMetadata(fileName, TikaInputStream.get(inputStream));
        if (MediaType.get(metadata.get(Metadata.CONTENT_TYPE)) != MediaType.Other) {
            File targetFile = null;
            switch (datasource.getType()) {
                case Watchfolder:
                    targetFile = new File(datasource.getDefinition(), FilenameUtils.getName(fileName));
                    break;
                case Itunes:
                    targetFile = new File(((ItunesDatasourceConfig)datasource).getAutoAddToItunesFolder(), FilenameUtils.getName(fileName));
                    break;
                default:
                    throw new IllegalArgumentException("Cannot upload to datasource of type \"" + datasource.getType() + "\".");
            }
            try (FileOutputStream targetStream = new FileOutputStream(targetFile)) {
                LOG.debug("Saving uploaded file \"" + targetFile.getAbsolutePath() + "\".");
                IOUtils.copy(inputStream, targetStream);
            }
            return targetFile;
        }
        return null;
    }
}
