package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.FindGenreQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.remote.service.GenreService
 */
public class GenreService {
    public Object getGenres(int letterIndex, int startItem, int maxItems) throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            FindGenreQuery query = new FindGenreQuery(user, false, letterIndex);
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(query),
                                                                             startItem,
                                                                             maxItems));
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }

    public Object getTracks(String[] genres) throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter
                    .getTransaction().executeQuery(FindTrackQuery.getForGenre(user, genres, SortOrder.Album)), 0, -1));
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }
}