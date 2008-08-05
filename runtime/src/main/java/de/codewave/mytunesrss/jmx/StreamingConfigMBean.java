package de.codewave.mytunesrss.jmx;

import java.math.BigDecimal;

/**
 * de.codewave.mytunesrss.jmx.AddonsConfigMBean
 */
public interface StreamingConfigMBean {
    String getLameBinary();

    void setLameBinary(String lameBinary);

    String getFaadBinary();

    void setFaadBinary(String faadBinary);

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

    String getLameOnlyOptions();

    void setLameOnlyOptions(String options);

    String getLameTargetOptions();

    void setLameTargetOptions(String options);

    String getFaadSourceOptions();

    void setFaadSourceOptions(String options);

    String getAlacSourceOptions();

    void setAlacSourceOptions(String options);
}