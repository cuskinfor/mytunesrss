package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.TranscoderConfig;
import org.apache.commons.lang.StringUtils;

import javax.management.NotCompliantMBeanException;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StreamingConfig extends MyTunesRssMBean implements StreamingConfigMBean {
    public StreamingConfig() throws NotCompliantMBeanException {
        super(StreamingConfigMBean.class);
    }

    public int getCacheMaxFiles() {
        return MyTunesRss.CONFIG.getStreamingCacheMaxFiles();
    }

    public int getCacheTimeout() {
        return MyTunesRss.CONFIG.getStreamingCacheTimeout();
    }

    public void setCacheMaxFiles(int maxFiles) {
        MyTunesRss.CONFIG.setStreamingCacheMaxFiles(maxFiles);
        onChange();
    }

    public void setCacheTimeout(int timeout) {
        MyTunesRss.CONFIG.setStreamingCacheTimeout(timeout);
        onChange();
    }

    public BigDecimal getBandwidthLimitFactor() {
        return MyTunesRss.CONFIG.getBandwidthLimitFactor();
    }

    public boolean isBandwidthLimit() {
        return MyTunesRss.CONFIG.isBandwidthLimit();
    }

    public void setBandwidthLimit(boolean limit) {
        MyTunesRss.CONFIG.setBandwidthLimit(limit);
        onChange();
    }

    public void setBandwidthLimitFactor(BigDecimal factor) {
        MyTunesRss.CONFIG.setBandwidthLimitFactor(factor);
        onChange();
    }

    public String[] getTranscoders() {
        List<String> configs = new ArrayList<String>();
        for (TranscoderConfig tc : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            configs.add(tc.getName() + ": pattern=" + tc.getPattern() + " -- mp4codecs=" + tc.getMp4Codecs() + " -- binary=" + tc.getBinary() + " -- options=" + tc.getOptions());
        }
        return configs.toArray(new String[configs.size()]);
    }

    public String addTranscoder(String name, String pattern, String mp4codecs, String targetSuffix, String targetContentType, String binary, String options) {
        if (StringUtils.isBlank(name) || name.length() > 40 || !StringUtils.isAlphanumericSpace(name)) {
            return MyTunesRssUtils.getBundleString("error.transcoderNameInvalid");
        }
        if (StringUtils.isBlank(pattern)) {
            return MyTunesRssUtils.getBundleString("error.transcoderPatternBlank");
        }
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return MyTunesRssUtils.getBundleString("error.transcoderPatternInvalid");
        }
        if (StringUtils.isBlank(targetSuffix)) {
            return MyTunesRssUtils.getBundleString("error.transcoderTargetSuffixBlank");
        }
        if (StringUtils.isBlank(targetContentType)) {
            return MyTunesRssUtils.getBundleString("error.transcoderTargetContentTypeBlank");
        }
        if (StringUtils.isBlank(binary) || !new File(binary).isFile()) {
            return MyTunesRssUtils.getBundleString("error.transcoderBinaryFileMissing");
        }
        Set<String> otherNames = new HashSet<String>();
        for (TranscoderConfig tc : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            otherNames.add(tc.getName());
        }
        if (otherNames.contains(name)) {
            return MyTunesRssUtils.getBundleString("error.duplicateTranscoderName");
        }
        TranscoderConfig tc = new TranscoderConfig();
        tc.setName(name);
        tc.setPattern(pattern);
        tc.setTargetSuffix(targetSuffix);
        tc.setTargetContentType(targetContentType);
        tc.setMp4Codecs(mp4codecs);
        tc.setBinary(binary);
        tc.setOptions(options);
        MyTunesRss.CONFIG.getTranscoderConfigs().add(tc);
        onChange();
        return MyTunesRssUtils.getBundleString("jmx.transcoderAdded", name);
    }

    public String deleteTranscoder(String name) {
        for (Iterator<TranscoderConfig> iter = MyTunesRss.CONFIG.getTranscoderConfigs().iterator(); iter.hasNext();) {
            if (name.equals(iter.next().getName())) {
                iter.remove();
                onChange();
                return MyTunesRssUtils.getBundleString("jmx.transcoderRemoved", name);
            }
        }
        return MyTunesRssUtils.getBundleString("jmx.transcoderNotFound", name);

    }
}