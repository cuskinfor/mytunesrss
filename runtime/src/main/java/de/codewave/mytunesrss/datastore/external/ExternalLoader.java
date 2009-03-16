package de.codewave.mytunesrss.datastore.external;

import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.external.ExternalLoader
 */
public class ExternalLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalLoader.class);

    public static void process(String external, DataStoreSession storeSession, long timeLastUpdate, Collection<String> trackIds) {
        if (StringUtils.startsWithIgnoreCase(external, "youtube:")) {
            new YouTubeLoader(storeSession, timeLastUpdate, trackIds).process(external.substring("youtube:".length()));
        } else {
            LOGGER.warn("External type \"" + external + "\" not supported.");
        }
    }
}