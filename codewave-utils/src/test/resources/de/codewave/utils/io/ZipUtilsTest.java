package de.codewave.utils.io;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZipUtilsTest {

    @Test
    public void testZipAndUnzipDirectory() throws IOException {
        File basedir = File.createTempFile(ZipUtils.class.getSimpleName(), ".tmp");
        basedir.delete();
        assertTrue("Could not create temporary base directory.", basedir.mkdir());
        for (int i = 0; i < 5; i++) {
            createSubDirWithFiles(basedir, i);
        }
        File zipFile = File.createTempFile(ZipUtils.class.getSimpleName(), ".zip");
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(new FileOutputStream(zipFile));
        ZipUtils.addFilesToZipRecursively("mybase", basedir, null, zipOutputStream);
        zipOutputStream.close();
        final File unzipBase = File.createTempFile(ZipUtils.class.getSimpleName(), ".unzipped");
        ZipUtils.unzip(zipFile, unzipBase);
        IOUtils.processFiles(unzipBase, new FileProcessor() {
            public void process(File file) {
                try {
                    assertEquals(IOUtils.getFileIdentifier(unzipBase, file), FileUtils.readFileToString(file));
                } catch (IOException e) {
                    throw new RuntimeException("Could not process file.", e);
                }
            }
        }, null);
    }

    private void createSubDirWithFiles(File basedir, int i) throws IOException {
        File dir = new File(basedir, "dir-" + i);
        assertTrue("Could not create sub directory.", dir.mkdir());
        for (int k = 0; k < 5; k++) {
            File file = new File(dir, "file-" + k);
            FileUtils.writeStringToFile(file, IOUtils.getFileIdentifier(basedir, file));
        }
    }
}
