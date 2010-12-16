/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.transcoding;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.TranscoderConfig;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PresetManager implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresetManager.class);

    public static final String PRESETS_URL = "http://www.codewave.de/tools/mytunesrss_tc_presets.xml";

    private List<TranscoderConfig> myPresets = new ArrayList<TranscoderConfig>();
    private TranscoderConfig myIphoneVideo;
    private TranscoderConfig myMp3Audio;

    public PresetManager() {
        myIphoneVideo = create(
                "iPhone Video",
                "^.+\\.(mp4|m4v|mov|avi)$",
                null,
                "ts",
                "video/MP2T",
                "ffmpeg",
                "-i {infile} -threads 4 -f mpegts -r 25 -vcodec libx264 -s 480x272 -flags +loop -cmp +chroma -deblockalpha 0 -deblockbeta 0 -crf 24 -b 150K -bt 175K -refs 1 -coder 0 -me_range 16 -subq 5 -partitions +parti4x4+parti8x8+partp8x8 -g 250 -keyint_min 25 -level 30 -qmin 10 -qmax 51 -trellis 2 -sc_threshold 40 -i_qfactor 0.71 -acodec libmp3lame -ab 64k -ar 44100 -ac 2 -"
        );
        myMp3Audio = create(
                "MP3 Audio",
                "^.+\\.(mp4|m4a|m4b)$",
                "alac,mp4a",
                "mp3",
                "audio/mp3",
                "ffmpeg",
                "-i {infile} -f mp3 -ar 44100 -ab 128 -acodec mp3 -"
        );
        myPresets.add(myIphoneVideo);
        myPresets.add(myMp3Audio);
        MyTunesRss.EXECUTOR_SERVICE.scheduleWithFixedDelay(this, 0, 3600, TimeUnit.SECONDS);
    }

    public List<TranscoderConfig> getPresets() {
        return new ArrayList<TranscoderConfig>(myPresets);
    }

    public void run() {
        List<TranscoderConfig> presets = new ArrayList<TranscoderConfig>();
        presets.add(myIphoneVideo);
        presets.add(myMp3Audio);
        HttpClient httpClient = MyTunesRssUtils.createHttpClient();
        GetMethod getMethod = new GetMethod(PRESETS_URL);
        try {
            if (httpClient.executeMethod(getMethod) == 200) {
                JXPathContext context = JXPathUtils.getContext(getMethod.getResponseBodyAsString());
                Iterator<JXPathContext> iterator = JXPathUtils.getContextIterator(context, "transcoder-presets/transcoder");
                while (iterator.hasNext()) {
                    presets.add(new TranscoderConfig(iterator.next()));
                }
            }
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not load transcoder presets from \"" + PRESETS_URL + "\": " + getMethod.getStatusCode());
            }
        } finally {
            getMethod.releaseConnection();
        }
        Collections.sort(presets, new Comparator<TranscoderConfig>() {
            public int compare(TranscoderConfig o1, TranscoderConfig o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else {
                    return StringUtils.trimToEmpty(o1.getName()).compareTo(StringUtils.trimToEmpty(o2.getName()));
                }
            }
        });
        myPresets = presets;
    }

    private TranscoderConfig create(String name, String pattern, String mp4Codecs, String suffix, String contentType, String binary, String options) {
        TranscoderConfig transcoderConfig = new TranscoderConfig();
        transcoderConfig.setBinary(binary);
        transcoderConfig.setMp4Codecs(mp4Codecs);
        transcoderConfig.setName(name);
        transcoderConfig.setOptions(options);
        transcoderConfig.setPattern(pattern);
        transcoderConfig.setTargetContentType(contentType);
        transcoderConfig.setTargetSuffix(suffix);
        return transcoderConfig;
    }
}
