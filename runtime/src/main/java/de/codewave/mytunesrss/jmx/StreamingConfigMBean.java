package de.codewave.mytunesrss.jmx;

import java.math.BigDecimal;

/**
 * de.codewave.mytunesrss.jmx.AddonsConfigMBean
 */
public interface StreamingConfigMBean {
    int getCacheTimeout();

    void setCacheTimeout(int timeout);

    int getCacheMaxFiles();

    void setCacheMaxFiles(int sizeLimit);

    boolean isBandwidthLimit();

    void setBandwidthLimit(boolean limit);

    BigDecimal getBandwidthLimitFactor();

    void setBandwidthLimitFactor(BigDecimal factor);

    String[] getTranscoders();

    String addTranscoder(String name, String pattern, String mp4codecs, String targetSuffix, String targetContentType, String binary, String options);

    String deleteTranscoder(String name);
}