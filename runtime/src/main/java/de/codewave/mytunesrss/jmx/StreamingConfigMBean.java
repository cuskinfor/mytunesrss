package de.codewave.mytunesrss.jmx;

import java.math.BigDecimal;

/**
 * de.codewave.mytunesrss.jmx.AddonsConfigMBean
 */
public interface StreamingConfigMBean {
    String getLameBinary();

    void setLameBinary(String lameBinary);

    String getFaad2Binary();

    void setFaad2Binary(String faad2Binary);

    String getAlacBinary();

    void setAlacBinary(String alacBinary);

    int getCacheTimeout();

    void setCacheTimeout(int timeout);

    int getCacheMaxFiles();

    void setCacheMaxFiles(int sizeLimit);

    boolean isBandwidthLimit();

    void setBandwidthLimit(boolean limit);

    BigDecimal getBandwidthLimitFactor();

    void setBandwidthLimitFactor(BigDecimal factor);

    String getMp3OnlyOptions();

    void setMp3OnlyOptions(String options);

    String getMp3TargetOptions();

    void setMp3TargetOptions(String options);

    String getAacSourceOptions();

    void setAacSourceOptions(String options);

    String getAlacSourceOptions();

    void setAlacSourceOptions();
}