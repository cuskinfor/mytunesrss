/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.utils.WildcardMatcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MediaServerClientProfile implements Cloneable, Comparable<MediaServerClientProfile> {

    private String myName;
    private String myUserAgentPattern;
    private WildcardMatcher myWildcardMatcher;
    private String myNetwork;
    private List<Integer> myPhotoSizes = Arrays.asList(1024, 2048, 4096, 0);
    private List<String> myTranscoders = Collections.emptyList();

    @XmlElement
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    @XmlElement
    public String getUserAgentPattern() {
        return myUserAgentPattern;
    }

    public void setUserAgentPattern(String userAgentPattern) {
        myUserAgentPattern = userAgentPattern;
        myWildcardMatcher = new WildcardMatcher(userAgentPattern);
    }

    @XmlElement
    public String getNetwork() {
        return myNetwork;
    }

    public void setNetwork(String network) {
        myNetwork = network;
    }

    @XmlElement
    public List<Integer> getPhotoSizes() {
        return new ArrayList<>(myPhotoSizes);
    }

    public void setPhotoSizes(List<Integer> photoSizes) {
        myPhotoSizes = new ArrayList<>(photoSizes);
    }

    public boolean matches(String userAgent) {
        return myWildcardMatcher.matches(userAgent);
    }

    @XmlElement
    public List<String> getTranscoders() {
        return new ArrayList<>(myTranscoders);
    }

    public void setTranscoders(List<String> transcoders) {
        myTranscoders = new ArrayList<>(transcoders);
    }

    @XmlTransient
    @JsonIgnore
    public TranscoderConfig[] getTranscodersConfigs() {
        List<TranscoderConfig> configs = new ArrayList<>();
        for (TranscoderConfig transcoderConfig : MyTunesRss.CONFIG.getEffectiveTranscoderConfigs()) {
            if (getTranscoders().contains(transcoderConfig.getName())) {
                configs.add(transcoderConfig);
            }
        }
        return configs.toArray(new TranscoderConfig[configs.size()]);
    }

    @Override
    public Object clone() {
        MediaServerClientProfile clone;
        try {
            clone = (MediaServerClientProfile) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new MediaServerClientProfile();
        }
        clone.setName(getName());
        clone.setUserAgentPattern(getUserAgentPattern());
        clone.setPhotoSizes(new ArrayList<Integer>(getPhotoSizes()));
        clone.setTranscoders(new ArrayList<String>(getTranscoders()));
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o.getClass().equals(getClass())) {
            return new EqualsBuilder().append(StringUtils.trimToEmpty(getName()), StringUtils.trimToEmpty(((MediaServerClientProfile) o).getName())).build();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(StringUtils.trimToEmpty(getName())).build();
    }

    @Override
    public int compareTo(MediaServerClientProfile o) {
        return StringUtils.trimToEmpty(getName()).compareTo(StringUtils.trimToEmpty(o.getName()));
    }
}
