package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.network.MulticastService;
import de.codewave.mytunesrss.network.RemoteServer;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.Version;

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
        throw new IllegalAccessException("UNAUTHORIZED");
    }

    public Object getDatabaseStatistics() throws IllegalAccessException, SQLException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            SystemInformation sysInfo = TransactionFilter.getTransaction().executeQuery(new GetSystemInformationQuery());
            Map<String, Long> statistics = new HashMap<String, Long>();
            statistics.put("tracks", Long.valueOf(sysInfo.getTrackCount()));
            statistics.put("albums", Long.valueOf(sysInfo.getAlbumCount()));
            statistics.put("artists", Long.valueOf(sysInfo.getArtistCount()));
            statistics.put("genres", Long.valueOf(sysInfo.getGenreCount()));
            statistics.put("lastUpdate", Long.valueOf(sysInfo.getLastUpdate()));
            return statistics;
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }

    public Object getServerInfo() throws IllegalAccessException, SQLException {
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("version", MyTunesRss.VERSION);
        Version version = new Version(MyTunesRss.VERSION);
        info.put("major", version.getMajor());
        info.put("minor", version.getMinor());
        info.put("revision", version.getBugfix());
        return info;
    }
}