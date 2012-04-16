/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.httplivestreaming;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class HttpLiveStreamingPlaylist {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLiveStreamingPlaylist.class);

    private File myBaseDir;

    public HttpLiveStreamingPlaylist(File baseDir) {
        myBaseDir = baseDir;
        baseDir.mkdirs();
    }

    public File getBaseDir() {
        return myBaseDir;
    }

    public void destroy() {
        try {
            FileUtils.deleteDirectory(myBaseDir);
        } catch (IOException e) {
            LOGGER.warn("Could not delete http-live-streaming directory \"" + myBaseDir.getAbsolutePath() + "\".");
        }
    }
}
