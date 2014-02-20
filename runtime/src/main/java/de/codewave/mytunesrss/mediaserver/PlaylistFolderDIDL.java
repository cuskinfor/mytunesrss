package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.PlaylistContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class PlaylistFolderDIDL extends MyTunesRssContainerDIDL {

    static final List<PlaylistType> PLAYLIST_TYPES = Arrays.asList(
            PlaylistType.ITunes,
            PlaylistType.ITunesFolder,
            PlaylistType.M3uFile,
            PlaylistType.MyTunes,
            PlaylistType.MyTunesSmart,
            PlaylistType.System
    );

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistFolderDIDL.class);

    @Override
    void createDirectChildren(final User user, final DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(user, PLAYLIST_TYPES, null, StringUtils.isEmpty(oidParams) ? "ROOT" : decode(oidParams).get(0), false, false);
        findPlaylistQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 100);
        executeAndProcess(
                tx,
                findPlaylistQuery,
                new DataStoreQuery.ResultProcessor<Playlist>() {
                    @Override
                    public void process(Playlist playlist) {
                        int childCount;
                        String idPrefix;
                        if (playlist.getType() == PlaylistType.ITunesFolder) {
                            idPrefix = ObjectID.PlaylistFolder.getValue();
                            childCount = 0;
                            try {
                                childCount = tx.executeQuery(new FindPlaylistQuery(user, PLAYLIST_TYPES, null, playlist.getId(), false, false)).getResultSize();
                            } catch (SQLException e) {
                                LOGGER.warn("Could not get playlist child count.", e);
                            }
                        } else {
                            idPrefix = ObjectID.Playlist.getValue();
                            childCount = playlist.getTrackCount();
                        }
                        addContainer(
                                new PlaylistContainer(
                                        idPrefix + ";" + encode(playlist.getId()),
                                        StringUtils.isNotBlank(playlist.getContainerId()) ? ObjectID.PlaylistFolder.getValue() + ";" + encode(playlist.getContainerId()) : ObjectID.PlaylistFolder.getValue(),
                                        playlist.getName(),
                                        "MyTunesRSS",
                                        childCount
                                )
                        );
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

}
