package de.codewave.utils.io;


import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * de.codewave.utils.io.ZipUtils
 */
public class ZipUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ZipUtils.class);

    public static void addToZip(String name, File file, ZipArchiveOutputStream zipOutputStream) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            addToZip(name, inputStream, zipOutputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static void addToZip(String name, byte[] data, ZipArchiveOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putArchiveEntry(new ZipArchiveEntry(name));
        zipOutputStream.write(data);
        zipOutputStream.closeArchiveEntry();
    }

    public static void addToZip(String name, InputStream inputStream, ZipArchiveOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putArchiveEntry(new ZipArchiveEntry(name));
        org.apache.commons.io.IOUtils.copy(inputStream, zipOutputStream);
        zipOutputStream.closeArchiveEntry();
    }

    public static boolean unzip(File zipFile, File targetDir) {
        ZipArchiveInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipArchiveInputStream(new FileInputStream(zipFile));
            for (ZipArchiveEntry entry = zipInputStream.getNextZipEntry(); entry != null; entry = zipInputStream.getNextZipEntry()) {
                saveFile(targetDir, entry.getName(), entry.isDirectory() ? null : zipInputStream);
            }
        } catch (IOException e) {
            if (targetDir != null && targetDir.exists()) {
                try {
                    FileUtils.deleteDirectory(targetDir);
                } catch (IOException e1) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not delete directory.", e);
                    }
                }
            }
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not close zip input stream.", e);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Add all files in the specified directory to the specified zip output stream. Method adds directories and
     * files recursively.
     *
     * @param baseName        Base name for zip file entries.
     * @param dir             Directory to scan for files and sub directories to add.
     * @param filter          An optional file filter to filter files and directories added.
     * @param zipOutputStream Output stream to add files to.
     * @throws IOException Any IO exception.
     */
    public static void addFilesToZipRecursively(String baseName, File dir, FileFilter filter, ZipArchiveOutputStream zipOutputStream) throws IOException {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("You must specify an existing directoy.");
        }
        for (File file : dir.listFiles(filter)) {
            if (file.isDirectory()) {
                addFilesToZipRecursively(baseName + "/" + file.getName(), file, filter, zipOutputStream);
            } else {
                addToZip(baseName + "/" + file.getName(), file, zipOutputStream);
            }
        }
    }

    private static void saveFile(File baseDir, String fileName, InputStream inputStream) throws IOException {
        String dirName = "";
        if (fileName.contains("/")) {
            dirName = fileName.substring(0, fileName.lastIndexOf("/"));
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        } else if (fileName.contains("\\")) {
            dirName = fileName.substring(0, fileName.lastIndexOf("\\"));
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        }
        File uploadDir = new File(baseDir, dirName);
        if (!uploadDir.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating upload directory \"" + uploadDir + "\".");
            }
            uploadDir.mkdirs();
        }
        if (uploadDir.isDirectory() && inputStream != null) {
            FileOutputStream targetStream = new FileOutputStream(new File(uploadDir, fileName));
            org.apache.commons.io.IOUtils.copy(inputStream, targetStream);
            targetStream.close();
        }
    }
}