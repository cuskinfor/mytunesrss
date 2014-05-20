/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.meta;

import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.mytunesrss.meta.Image
 */
public class Image {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Image.class);
    private static final File NO_FILE = new File("/");

    private String myMimeType;
    private byte[] myData;
    private File myImageFile;

    public Image(String mimeType, InputStream is) throws IOException {
        this(mimeType, IOUtils.toByteArray(new BufferedInputStream(is)));
    }

    @Override
    protected void finalize() throws Throwable {
        // Just to make sure we delete the file, at least when the VM quits
        deleteImageFile();
        super.finalize();
    }

    public Image(String mimeType, byte[] data) {
        myMimeType = mimeType;
        myData = data;
    }

    public String getMimeType() {
        return myMimeType;
    }

    public byte[] getData() {
        return myData;
    }

    public synchronized File getImageFile() {
        if (myImageFile == null) {
            try {
                myImageFile = MyTunesRssUtils.createTempFile("-image.tmp");
                FileUtils.writeByteArrayToFile(myImageFile, getData());
            } catch (IOException e) {
                myImageFile = NO_FILE;
                LOGGER.warn("Could not create temporary image file.", e);
            }
        }
        return myImageFile != NO_FILE ? myImageFile : null;
    }
    
    public synchronized void deleteImageFile() {
        if (myImageFile != null && myImageFile != NO_FILE) {
            if (!myImageFile.delete()) {
                LOGGER.debug("Could not delete image file \"" + myImageFile.getAbsolutePath() + "\".");
            }
            myImageFile = null;
        }
    }

}
