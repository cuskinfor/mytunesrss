package de.codewave.mytunesrss.jmx;

import java.math.BigDecimal;

/**
 * de.codewave.mytunesrss.jmx.AddonsConfigMBean
 */
public interface StreamingConfigMBean {
    String getLameBinary();

    void setLameBinary(String lameBinary);

    String getLameOptions();

    void setLameOptions(String lameOptions);

    int getCacheTimeout();

    void setCacheTimeout(int timeout);

    int getCacheMaxFiles();

    void setCacheMaxFiles(int sizeLimit);

    boolean isBandwidthLimit();

    void setBandwidthLimit(boolean limit);

    BigDecimal getBandwidthLimitFactor();

    void setBandwidthLimitFactor(BigDecimal factor);

    String getLameTargetOptions();

    void setLameTargetOptions(String options);

    String[] getTranscoders();

    String addTranscoder(String name, String suffixes, String mp4codecs, String binary, String options);

    String deleteTranscoder(String name);
}