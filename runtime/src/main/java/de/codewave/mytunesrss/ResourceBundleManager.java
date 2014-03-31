/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceBundleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleManager.class);

    private ConcurrentHashMap<String, ResourceBundle> myCache = new ConcurrentHashMap<>();

    private ClassLoader myLoader;

    public ResourceBundleManager(ClassLoader loader) {
        myLoader = loader;
    }

    public ResourceBundle getBundle(String bundleName, Locale locale) {
        ResourceBundle bundle = myCache.get(getCacheKey(locale, bundleName));
        if (bundle == null) {
            bundle = loadBundle(locale, bundleName);
            if (bundle != null) {
                ResourceBundle existingBundle = myCache.putIfAbsent(getCacheKey(locale, bundleName), bundle);
                return existingBundle != null ? existingBundle : bundle;
            }
        }
        return bundle;
    }

    private String getCacheKey(Locale locale, String bundleName) {
        return bundleName + "_" + locale.toString();
    }

    private ResourceBundle loadBundle(Locale locale, String bundleName) {
        List<String> bundlePaths = getBundlePaths(locale, bundleName);
        for (String bundlePath : bundlePaths) {
            InputStream is = myLoader.getResourceAsStream(bundlePath);
            if (is != null) {
                try {
                    return new PropertyResourceBundle(is);
                } catch (IOException ignored) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Could not read property resource bundle \"" + bundlePath + "\".");
                    }
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
        return null;
    }

    private List<String> getBundlePaths(Locale locale, String bundleName) {
        String[] codes = locale.toString().split("_");
        String bundlePath = StringUtils.replaceChars(bundleName, '.', '/');
        List<String> bundlePaths = new ArrayList<>();
        if (codes.length == 3) {
            bundlePaths.add(bundlePath + "_" + codes[0] + "_" + codes[1] + "_" + codes[2] + ".properties");
        }
        if (codes.length >= 2) {
            bundlePaths.add(bundlePath + "_" + codes[0] + "_" + codes[1] + ".properties");
        }
        if (codes.length >= 1) {
            bundlePaths.add(bundlePath + "_" + codes[0] + ".properties");
        }
        bundlePaths.add(bundlePath + ".properties");
        return bundlePaths;
    }
}
