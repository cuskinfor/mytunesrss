package de.codewave.mytunesrss.datastore.external;

import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.external.ExternalLoader
 */
public class ExternalLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalLoader.class);

    public static void process(String external, DataStoreSession storeSession, long timeLastUpdate, Collection<String> trackIds) {
        if (YouTubeLoader.handles(external)) {
            YouTubeLoader youTubeLoader = new YouTubeLoader(storeSession, timeLastUpdate, trackIds);
            youTubeLoader.process(external);
            trackIds.removeAll(youTubeLoader.getExistingIds());
        } else {
            LOGGER.warn("External type \"" + external + "\" not supported.");
        }
    }
}