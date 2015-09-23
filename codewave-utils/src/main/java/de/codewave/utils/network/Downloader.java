/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.network;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * de.codewave.utils.network.Downloader
 */
public class Downloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    public static enum Result {
        Finished(),Failed(),Cancelled();
    }

    private URL myUrl;
    private File myTarget;
    private boolean myCancel;
    private DownloadProgressListener myListener;

    Downloader(URL url, File target, DownloadProgressListener listener) {
        if (url == null) {
            throw new NullPointerException("Download url is null.");
        }
        if (target == null) {
            throw new NullPointerException("Target file is null.");
        }
        myListener = listener;
        myUrl = url;
        myTarget = target;
    }

    public Result download() {
        synchronized (myTarget) {
            Result result = null;
            FileOutputStream outputStream = null;
            InputStream inputStream = null;
            try {
                URLConnection connection = myUrl.openConnection();
                long fileSize = connection.getContentLength();
                long totalBytesRead = 0;
                byte[] data = new byte[4096];
                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(myTarget);
                while (!myCancel && totalBytesRead < fileSize) {
                    int bytesRead = inputStream.read(data);
                    outputStream.write(data, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (myListener != null) {
                        myListener.reportProgress((int)((100L * totalBytesRead) / fileSize));
                    }
                }
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not download data.", e);
                }
                result = Result.Failed;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Could not close input stream.", e);
                        }
                        result = Result.Failed;
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Could not close output stream.", e);
                        }
                        result = Result.Failed;
                    }
                }
            }
            if (result == null) {
                return myCancel ? Result.Cancelled : Result.Finished;
            }
            return result;
        }
    }

    public void cancel() {
        myCancel = true;
    }
}