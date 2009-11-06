package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MediaType;
import de.codewave.utils.sql.ResultBuilder;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Result builder for tracks.
 */
public class TrackResultBuilder implements ResultBuilder<Track> {
    public Track create(ResultSet resultSet) throws SQLException {
        Track track = new Track();
        track.setSource(TrackSource.valueOf(resultSet.getString("SOURCE")));
        track.setId(resultSet.getString("ID"));
        track.setName(resultSet.getString("NAME"));
        track.setArtist(resultSet.getString("ARTIST"));
        track.setOriginalArtist(resultSet.getString("ORIGINAL_ARTIST"));
        track.setAlbum(resultSet.getString("ALBUM"));
        track.setTime(resultSet.getInt("TIME"));
        track.setTrackNumber(resultSet.getInt("TRACK_NUMBER"));
        String pathname = resultSet.getString("FILE");
        track.setFilename(pathname);
        track.setFile(StringUtils.isNotEmpty(pathname) ? new File(pathname) : null);
        track.setProtected(resultSet.getBoolean("PROTECTED"));
        track.setMediaType(MediaType.valueOf(resultSet.getString("MEDIATYPE")));
        track.setGenre(resultSet.getString("GENRE"));
        track.setMp4Codec(resultSet.getString("MP4CODEC"));
        track.setTsPlayed(resultSet.getLong("TS_PLAYED"));
        track.setTsUpdated(resultSet.getLong("TS_UPDATED"));
        track.setLastImageUpdate(resultSet.getLong("LAST_IMAGE_UPDATE"));
        track.setPlayCount(resultSet.getLong("PLAYCOUNT"));
        track.setImageHash(resultSet.getString("IMAGE_HASH"));
        track.setComment(resultSet.getString("COMMENT"));
        track.setPosNumber(resultSet.getInt("POS_NUMBER"));
        track.setPosSize(resultSet.getInt("POS_SIZE"));
        track.setYear(resultSet.getInt("YEAR"));
        return track;
    }
}