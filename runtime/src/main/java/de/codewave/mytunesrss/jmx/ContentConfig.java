package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.SavePlaylistAttributesStatement;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotCompliantMBeanException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.jmx.ContentConfig
 */
public class ContentConfig extends MyTunesRssMBean implements ContentConfigMBean {
    private static final Logger LOG = LoggerFactory.getLogger(ContentConfig.class);

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
        try {
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            DataStoreQuery.QueryResult<Playlist> queryResult = session.executeQuery(new FindPlaylistQuery(null, playlistId, true));
            if (queryResult.getResultSize() == 1) {
                Playlist playlist = queryResult.nextResult();
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
            } else {
                session.commit();
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
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            DataStoreQuery.QueryResult<Playlist> playlists = session.executeQuery(new FindPlaylistQuery(null, null, true));
            List<String> displayItems = new ArrayList<String>();
            for (Playlist playlist = playlists.nextResult(); playlist != null; playlist = playlists.nextResult()) {
                if (playlist.getType() == PlaylistType.ITunes || playlist.getType() == PlaylistType.M3uFile) {
                    displayItems.add("id=\"" + playlist.getId() + "\", name=\"" + playlist.getName() + "\", status=\"" + (playlist.isHidden() ? "hidden" : "visible") + "\"");
                }
            }
            return displayItems.toArray(new String[displayItems.size()]);
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read playlists.", e);
            }
        } finally {
            session.commit();
        }
        return new String[] {"Could not retrieve playlists!"};
    }
}