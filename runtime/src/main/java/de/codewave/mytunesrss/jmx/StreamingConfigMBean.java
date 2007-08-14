package de.codewave.mytunesrss.jmx;

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
}