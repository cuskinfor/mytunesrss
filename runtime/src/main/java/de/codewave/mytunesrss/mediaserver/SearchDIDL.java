/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.NotYetImplementedException;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class SearchDIDL extends MyTunesRssContainerDIDL {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchDIDL.class);

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, String searchCriteria, long firstResult, long maxResults) throws SQLException {
        String searchTerms = extractSearchTerms(searchCriteria);
        LOGGER.debug("Extracted search terms \"" + searchTerms + "\".");
        try {
            FindTrackQuery query = FindTrackQuery.getForSearchTerm(
                    getUser(),
                    searchTerms,
                    getClientProfile().getSearchFuzziness(),
                    SortOrder.Album,
                    getClientProfile().getMaxSearchResults()
            );
            executeAndProcess(tx, query, new DataStoreQuery.ResultProcessor<Track>() {
                @Override
                public void process(Track track) {
                    switch (track.getMediaType()) {
                        case Audio:
                            addItem(createMusicTrack(user, track, ObjectID.SearchResultTrack.getValue() + ";" + encode(track.getId()), ObjectID.Root.getValue()));
                            break;
                        case Video:
                            addItem(createMovieTrack(user, track, ObjectID.SearchResultTrack.getValue() + ";" + encode(track.getId()), ObjectID.Root.getValue()));
                            break;
                        default:
                            // do nothing
                    }
                }
            }, firstResult, (int) maxResults);
        } catch (IOException e) {
            LOGGER.error("Could not create search query.", e);
        }
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        throw new NotYetImplementedException();
    }

    private String extractSearchTerms(String searchCriteria) {
        String prefix = "dc:title contains \"";
        if (StringUtils.startsWith(searchCriteria, prefix) && StringUtils.endsWith(searchCriteria, "\"")) {
            String escapedSearchTerm = StringUtils.trimToEmpty(searchCriteria.substring(prefix.length(), searchCriteria.length() - 1));
            String test = escapedSearchTerm.replace("\\\\", "").replace("\\\"", "");
            // after removing all escaped backslashes and double-quotes we should not have any more backslashes in a valid query
            if (!StringUtils.contains(test, "\\")) {
                return escapedSearchTerm.replace("\\\\", "\\").replace("\\\"", "\"");
            }
        }
        LOGGER.warn("Search criteria \"" + searchCriteria + "\" not supported.");
        throw new UnsupportedOperationException("Search criteria \"" + searchCriteria + "\" not supported.");
    }


}
