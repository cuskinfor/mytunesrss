package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.remote.service.AlbumService
 */
public class AlbumService {
    public Object getAlbums(String filter, String artist, String genre, int letterIndex, int minYear, int maxYear, int startItem, int maxItems)
            throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            FindAlbumQuery query = new FindAlbumQuery(user, StringUtils.trimToNull(filter), StringUtils.trimToNull(artist), StringUtils.trimToNull(
                    genre), letterIndex, minYear, maxYear);
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(query),
                                                                             startItem,
                                                                             maxItems));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object getTracks(String[] albums) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter
                    .getTransaction().executeQuery(FindTrackQuery.getForAlbum(user, albums, SortOrder.Album)), 0, -1));
        }
        throw new IllegalAccessException("Unauthorized");
    }
}