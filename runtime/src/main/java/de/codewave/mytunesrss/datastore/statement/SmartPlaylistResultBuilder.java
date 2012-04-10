/*
 * Copyright (c) 2009. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.utils.sql.ResultBuilder;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SmartPlaylistResultBuilder implements ResultBuilder<SmartPlaylist> {

    private Map<String, SmartPlaylist> smartPlaylists = new HashMap<String, SmartPlaylist>();

    public SmartPlaylist create(ResultSet resultSet) throws SQLException {
        SmartPlaylist smartPlaylist = smartPlaylists.get(resultSet.getString("ID"));
        if (smartPlaylist == null) {
            Playlist playlist = new Playlist();
            Collection<SmartInfo> smartInfos = new HashSet<SmartInfo>();
            smartPlaylist = new SmartPlaylist();
            smartPlaylist.setPlaylist(playlist);
            smartPlaylist.setSmartInfos(smartInfos);
            playlist.setId(resultSet.getString("ID"));
            playlist.setName(resultSet.getString("NAME"));
            playlist.setType(PlaylistType.MyTunesSmart);
            playlist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            playlist.setUserPrivate(resultSet.getBoolean("USER_PRIVATE"));
            playlist.setHidden(resultSet.getBoolean("HIDDEN"));
            playlist.setUserOwner(resultSet.getString("USER_OWNER"));
            playlist.setContainerId(resultSet.getString("CONTAINER_ID"));
            smartPlaylists.put(resultSet.getString("ID"), smartPlaylist);
        }
        SmartInfo smartInfo = new SmartInfo(SmartFieldType.valueOf(resultSet.getString("SMART_FIELD_TYPE")), resultSet.getString("SMART_PATTERN"), resultSet.getBoolean("SMART_INVERT"));
        smartPlaylist.getSmartInfos().add(smartInfo);
        return null;
    }

    public Collection<SmartPlaylist> getSmartPlaylists() {
        return smartPlaylists.values();
    }
}
