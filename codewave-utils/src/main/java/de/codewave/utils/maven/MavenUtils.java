package de.codewave.utils.maven;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.io.*;

/**
 * de.codewave.utils.maven.MavenUtils
 */
public class MavenUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenUtils.class);

    public static String getVersion(String groupId, String artifactId) {
        return getVersion(groupId, artifactId, null);
    }

    public static String getVersion(String groupId, String artifactId, ClassLoader classloader) {
        if (classloader == null) {
            classloader = Thread.currentThread().getContextClassLoader();
        }
        Properties properties = new Properties();
        try {
            InputStream stream = classloader.getResourceAsStream("META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");
            if (stream != null) {
                properties.load(stream);
            }
            return properties.getProperty("version");
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get pom version for groupId \"" + groupId + "\" and artifactId \"" + artifactId + "\".", e);
            }
        }
        return null;
    }
}