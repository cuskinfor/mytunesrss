package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.*;

import java.sql.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.remote.service.AlbumService
 */
public class AlbumService {
    public Object getAlbums(String filter, String artist, String genre, int letterIndex, int startItem, int maxItems) throws SQLException {
        FindAlbumQuery query = new FindAlbumQuery(MyTunesRssRemoteEnv.getUser(), StringUtils.trimToNull(filter),
                                                  StringUtils.trimToNull(artist), StringUtils.trimToNull(genre), letterIndex);
        return MyTunesRssRemoteEnv.getRenderMachine().render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(query), startItem, maxItems));
    }
}