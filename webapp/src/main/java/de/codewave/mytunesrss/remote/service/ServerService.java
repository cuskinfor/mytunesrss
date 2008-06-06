package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.network.MulticastService;
import de.codewave.mytunesrss.network.RemoteServer;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;

import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.remote.service.ServerService
 */
public class ServerService {

    public Object discoverOtherServers() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            List<RemoteServer> servers = new ArrayList<RemoteServer>(MulticastService.getOtherInstances());
            Collections.sort(servers, new Comparator<RemoteServer>() {
                public int compare(RemoteServer server1, RemoteServer server2) {
                    return server1.getName().compareTo(server2.getName());
                }
            });
            return RenderMachine.getInstance().render(servers);
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object getDatabaseStatistics() throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            SystemInformation sysInfo = TransactionFilter.getTransaction().executeQuery(new GetSystemInformationQuery());
            Map<String, Integer> statistics = new HashMap<String, Integer>();
            statistics.put("tracks", sysInfo.getTrackCount());
            statistics.put("albums", sysInfo.getAlbumCount());
            statistics.put("artists", sysInfo.getArtistCount());
            statistics.put("genres", sysInfo.getGenreCount());
            return statistics;
        }
        throw new IllegalAccessException("Unauthorized");
    }
}