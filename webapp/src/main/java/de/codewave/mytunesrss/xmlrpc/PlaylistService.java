package de.codewave.mytunesrss.xmlrpc;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.command.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.xmlrpc.PlaylistService
 */
public class PlaylistService {
    public List<Map<String, Object>> getPlaylists() throws SQLException {
        FindPlaylistQuery query = new FindPlaylistQuery(MyTunesRssXmlRpcServlet.getAuthUser(), null, null, false, false);
        DataStoreQuery.QueryResult<Playlist> result = TransactionFilter.getTransaction().executeQuery(query);
        List<Map<String, Object>> answer = new ArrayList<Map<String, Object>>();
        for (Playlist playlist = result.nextResult(); playlist != null; playlist = result.nextResult()) {
            Map<String, Object> answerPlaylist = new HashMap<String, Object>();
            answerPlaylist.put("id", playlist.getId());
            answerPlaylist.put("name", playlist.getName());
            answerPlaylist.put("count", playlist.getTrackCount());
            answer.add(answerPlaylist);
        }
        return answer;
    }

    public String getPlaylistUrl(String playlistId, String type, String filename) {
        StringBuilder builder = new StringBuilder();
        builder.append(MyTunesRssXmlRpcServlet.getServerCall(MyTunesRssCommand.CreatePlaylist));
        StringBuilder pathInfo = new StringBuilder();
        pathInfo.append("playlist=").append(playlistId).append("/type=").append(type);
        builder.append("/").append(MyTunesRssWebUtils.encryptPathInfo(pathInfo.toString())).append("/").append(filename);
        return builder.toString();
    }
}