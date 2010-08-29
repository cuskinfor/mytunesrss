/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.httplivestreaming;

import de.codewave.jna.ffmpeg.HttpLiveStreamingSegmenter;
import de.codewave.utils.io.ExpiringCacheItem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class HttpLiveStreamingCacheItem extends ExpiringCacheItem {
    private static final Logger LOG = LoggerFactory.getLogger(HttpLiveStreamingCacheItem.class);

    private HttpLiveStreamingSegmenter mySegmenter;

    public HttpLiveStreamingCacheItem(String identifier, long timeout, HttpLiveStreamingSegmenter segmenter) {
        super(identifier, timeout);
        mySegmenter = segmenter;
    }

    public HttpLiveStreamingSegmenter getSegmenter() {
        return mySegmenter;
    }

    @Override
    protected void onItemExpired() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP Live Streaming files expired.");
        }

        while (!mySegmenter.isDone()) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Waiting 1 second for segmenter job to finish.");
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        for (File file : mySegmenter.getFiles()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Deleting HTTP Live Streaming file \"" + file.getAbsolutePath() + "\".");
            }

            FileUtils.deleteQuietly(file);
        }
    }
}
