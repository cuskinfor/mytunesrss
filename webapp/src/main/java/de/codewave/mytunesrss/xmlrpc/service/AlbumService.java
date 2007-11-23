package de.codewave.mytunesrss.xmlrpc.service;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.xmlrpc.*;

import java.sql.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.xmlrpc.service.AlbumService
 */
public class AlbumService {
    public Object getAlbums(String filter, String artist, String genre, int letterIndex, int startItem, int maxItems) throws SQLException {
        FindAlbumQuery query = new FindAlbumQuery(MyTunesRssXmlRpcServlet.getAuthUser(), StringUtils.trimToNull(filter),
                                                  StringUtils.trimToNull(artist), StringUtils.trimToNull(genre), letterIndex);
        return MyTunesRssXmlRpcServlet.RENDER_MACHINE.render(new QueryResultWrapper(TransactionFilter.getTransaction().executeQuery(query), startItem, maxItems));
    }
}