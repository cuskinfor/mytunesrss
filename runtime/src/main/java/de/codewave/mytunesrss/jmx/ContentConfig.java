package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;

import javax.management.*;
import java.util.*;
import java.sql.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.jmx.ContentConfig
 */
public class ContentConfig extends MyTunesRssMBean implements ContentConfigMBean {
    private static final Log LOG = LogFactory.getLog(ContentConfig.class);

    public ContentConfig() throws NotCompliantMBeanException {
        super(ContentConfigMBean.class);
    }

    public String showPlaylist(String playlistId) {
        String error = changePlaylistAttribute(playlistId, false);
        if (error == null) {
            onChange();
        }
        return error != null ? error : "Ok.";
    }

    private String changePlaylistAttribute(String playlistId, boolean hidden) {
        Collection<Playlist> playlists = null;
        try {
            playlists = MyTunesRss.STORE.executeQuery(new FindPlaylistQuery(null, playlistId, true));
            if (playlists != null && playlists.size() == 1) {
                Playlist playlist = playlists.iterator().next();
                DataStoreSession session = MyTunesRss.STORE.getTransaction();
                SavePlaylistAttributesStatement statement = new SavePlaylistAttributesStatement();
                statement.setId(playlist.getId());
                statement.setHidden(hidden);
                statement.setName(playlist.getName());
                statement.setUserOwner(playlist.getUserOwner());
                statement.setUserPrivate(playlist.isUserPrivate());
                try {
                    session.executeStatement(statement);
                    session.commit();
                    return null;
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not update playlist attributes.", e);
                    }
                    try {
                        session.rollback();
                    } catch (SQLException e2) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not rollback transaction.", e2);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Playlist with specified id not found!", e);
            }
        }
        return "Playlist with specified id not found";
    }

    public String hidePlaylist(String playlistId) {
        String error = changePlaylistAttribute(playlistId, true);
        if (error == null) {
            onChange();
        }
        return error != null ? error : "Ok.";
    }

    public String[] getPlaylists() {
        try {
            Collection<Playlist> playlists = MyTunesRss.STORE.executeQuery(new FindPlaylistQuery(null, null, true));
            List<String> displayItems = new ArrayList<String>();
            for (Playlist playlist : playlists) {
                if (playlist.getType() == PlaylistType.ITunes || playlist.getType() == PlaylistType.M3uFile) {
                    displayItems.add("id=\"" + playlist.getId() + "\", name=\"" + playlist.getName() + "\", status=\"" + (playlist.isHidden() ? "hidden" : "visible") + "\"");
                }
            }
            return displayItems.toArray(new String[displayItems.size()]);
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read playlists.", e);
            }
        }
        return new String[] {"Could not retrieve playlists!"};
    }
}