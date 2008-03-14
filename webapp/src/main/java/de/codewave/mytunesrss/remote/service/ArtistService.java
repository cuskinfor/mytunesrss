package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.FindArtistQuery;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.remote.service.ArtistService
 */
public class ArtistService {
    public Object getArtists(String filter, String album, String genre, int letterIndex, int startItem, int maxItems) throws SQLException,
            IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            FindArtistQuery query = new FindArtistQuery(user, StringUtils.trimToNull(filter), StringUtils.trimToNull(album), StringUtils.trimToNull(
                    genre), letterIndex);
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(query),
                                                                             startItem,
                                                                             maxItems));
        }
        throw new IllegalAccessException("Unauthorized");
    }
}