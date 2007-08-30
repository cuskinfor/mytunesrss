package de.codewave.mytunesrss.jmx;

import java.math.*;

/**
 * de.codewave.mytunesrss.jmx.AddonsConfigMBean
 */
public interface StreamingConfigMBean {
    String getLameBinary();

    void setLameBinary(String lameBinary);

    String getFaad2Binary();

    void setFaad2Binary(String faad2Binary);

    int getCacheTimeout();

    void setCacheTimeout(int timeout);

    int getCacheMaxFiles();

    void setCacheMaxFiles(int sizeLimit);

    boolean isBandwidthLimit();

    void setBandwidthLimit(boolean limit);

    BigDecimal getBandwidthLimitFactor();

    void setBandwidthLimitFactor(BigDecimal factor);
}