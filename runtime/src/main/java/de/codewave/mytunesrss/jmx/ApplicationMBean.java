package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.ApplicationMBean
 */
public interface ApplicationMBean {
    String getVersion();
    String quit();
    String getLicense();
    boolean isDebugLogging();
    void setDebugLogging(boolean debugLogging);
    String sendSupportRequest(String name, String email, String comment, boolean includeItunesXml);
}