/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import com.google.common.collect.ImmutableList;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.utils.WildcardMatcher;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MediaServerClientProfile {

    @XmlElement
    private String myUserAgentPattern;
    private WildcardMatcher myWildcardMatcher;
    @XmlElement
    private List<Integer> myPhotoSizes = Arrays.asList(1024, 2048, 4096, 0);
    @XmlElement
    private List<String> myTranscoders = Collections.emptyList();

    public String getUserAgentPattern() {
        return myUserAgentPattern;
    }

    public void setUserAgentPattern(String userAgentPattern) {
        myUserAgentPattern = userAgentPattern;
        myWildcardMatcher = new WildcardMatcher(userAgentPattern);
    }

    public ImmutableList<Integer> getPhotoSizes() {
        return ImmutableList.copyOf(myPhotoSizes);
    }

    public void setPhotoSizes(List<Integer> photoSizes) {
        myPhotoSizes = photoSizes;
    }

    public boolean matches(String userAgent) {
        return myWildcardMatcher.matches(userAgent);
    }

    public ImmutableList<String> getTranscoders() {
        return ImmutableList.copyOf(myTranscoders);
    }

    public void setTranscoders(List<String> transcoders) {
        myTranscoders = transcoders;
    }

    public TranscoderConfig[] getTranscodersConfigs() {
        List<TranscoderConfig> configs = new ArrayList<>();
        for (TranscoderConfig transcoderConfig : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            if (getTranscoders().contains(transcoderConfig.getName())) {
                configs.add(transcoderConfig);
            }
        }
        return configs.toArray(new TranscoderConfig[configs.size()]);
    }
}
