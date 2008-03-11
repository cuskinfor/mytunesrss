package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.FindArtistQuery;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.remote.service.ArtistService
 */
public class ArtistService {
    public Object getArtists(String filter, String album, String genre, int letterIndex, int startItem, int maxItems) throws SQLException {
        User user = MyTunesRssRemoteEnv.getUser();
        if (user != null) {
            FindArtistQuery query = new FindArtistQuery(user, StringUtils.trimToNull(filter), StringUtils.trimToNull(album), StringUtils.trimToNull(
                    genre), letterIndex);
            return MyTunesRssRemoteEnv.getRenderMachine().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(query),
                                                                                        startItem,
                                                                                        maxItems));
        }
        return null;
    }
}