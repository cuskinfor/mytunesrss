package de.codewave.mytunesrss.datastore.external;

import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * de.codewave.mytunesrss.datastore.external.ExternalLoader
 */
public class ExternalLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalLoader.class);

    public enum Flag {
        Sticky('s');

        private char myChar;

        Flag(char c) {
            myChar = c;
        }

        public static Flag getForChar(char c) {
            for (Flag flag : Flag.values()) {
                if (flag.myChar == c) {
                    return flag;
                }
            }
            throw new IllegalArgumentException("No flag for \"" + c + "\"!");
        }

        public static Set<Flag> getForChars(String chars) {
            Set<Flag> flags = new HashSet<Flag>();
            for (char c : chars.toCharArray()) {
                flags.add(getForChar(c));
            }
            return flags;
        }
    }

    public static void process(String external, String flags, DataStoreSession storeSession, long timeLastUpdate, Collection<String> trackIds) {
        if (YouTubeLoader.handles(external)) {
            YouTubeLoader youTubeLoader = new YouTubeLoader(storeSession, timeLastUpdate, Flag.getForChars(flags), trackIds);
            youTubeLoader.process(external);
            trackIds.removeAll(youTubeLoader.getExistingIds());
        } else {
            LOGGER.warn("External type \"" + external + "\" not supported.");
        }
    }
}