package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.remote.service.AlbumService
 */
public class AlbumService {
    public Object getAlbums(String filter, String artist, String genre, int letterIndex, int startItem, int maxItems) throws SQLException {
        User user = MyTunesRssRemoteEnv.getUser();
        if (user != null) {
            FindAlbumQuery query = new FindAlbumQuery(user, StringUtils.trimToNull(filter),
                                                      StringUtils.trimToNull(artist), StringUtils.trimToNull(genre), letterIndex);
            return MyTunesRssRemoteEnv.getRenderMachine().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(query), startItem, maxItems));
        }
        return null;
    }
}