package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.xml.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * de.codewave.mytunesrss.datastore.itunes.TrackListenerr
 */
public class TrackListener implements PListHandlerListener {
    private static final Log LOG = LogFactory.getLog(TrackListener.class);

    private DataStoreSession myDataStoreSession;
    private LibraryListener myLibraryListener;
    private int myUpdatedCount;
    private Map<Long, String> myTrackIdToPersId;
    private Collection<Map> myTrackCache = new HashSet<Map>();

    public TrackListener(DataStoreSession dataStoreSession, LibraryListener libraryListener, Map<Long, String> trackIdToPersId) throws SQLException {
        myDataStoreSession = dataStoreSession;
        myLibraryListener = libraryListener;
        myTrackIdToPersId = trackIdToPersId;
    }

    public int getUpdatedCount() {
        return myUpdatedCount;
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        Map track = (Map)value;
        String trackId = calculateTrackId(track);
        myTrackIdToPersId.put((Long)track.get("Track ID"), trackId);
        myTrackCache.add(track);
        if (myTrackCache.size() > 100) {
            processTrackCache();
        }
        return false;
    }

    private String calculateTrackId(Map track) {
        String trackId = myLibraryListener.getLibraryId() + "_";
        trackId += track.get("Persistent ID") != null ? track.get("Persistent ID").toString() : "TrackID" + track.get("Track ID").toString();
        return trackId;
    }

    public void processTrackCache() {
        if (!myTrackCache.isEmpty()) {
            Collection<String> trackIds = new HashSet<String>(myTrackCache.size());
            for (Map map : myTrackCache) {
                trackIds.add(calculateTrackId(map));
            }
            Collection<String> existingIds = Collections.emptyList();
            try {
                existingIds = myDataStoreSession.executeQuery(new ConfirmTrackIdsQuery(trackIds));
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not confirm track IDs.", e);
                }
            }
            for (Map track : myTrackCache) {
                String trackId = track.get("Track ID").toString();
                if (processTrack(track, existingIds.contains(calculateTrackId(track)))) {
                    myUpdatedCount++;
                    trackIds.remove(trackId); // do not set last seen for track already updated
                    DatabaseBuilderTask.updateHelpTables(myDataStoreSession, myUpdatedCount);
                }
                DatabaseBuilderTask.doCheckpoint(myDataStoreSession);
            }
            DatabaseBuilderTask.setLastSeenTime(myDataStoreSession, trackIds);
            DatabaseBuilderTask.doCheckpoint(myDataStoreSession);
            myTrackCache.clear();
        }
    }

    public boolean beforeArrayAdd(List array, Object value) {
        throw new UnsupportedOperationException("method beforeArrayAdd of class ItunesLoader$TrackListener is not supported!");
    }

    private boolean processTrack(Map track, boolean existing) {
        String trackId = calculateTrackId(track);
        String name = (String)track.get("Name");
        String trackType = (String)track.get("Track Type");
        if (trackType == null || "File".equals(trackType)) {
            File file = ItunesLoader.getFileForLocation((String)track.get("Location"));
            if (trackId != null && StringUtils.isNotEmpty(name) && file != null && new SupportedFileFilter().accept(file.getParentFile(),
                                                                                                                    file.getName())) {
                Date dateModified = ((Date)track.get("Date Modified"));
                long dateModifiedTime = dateModified != null ? dateModified.getTime() : Long.MIN_VALUE;
                Date dateAdded = ((Date)track.get("Date Added"));
                long dateAddedTime = dateAdded != null ? dateAdded.getTime() : Long.MIN_VALUE;
                if (!existing || dateModifiedTime >= myLibraryListener.getTimeLastUpate() || dateAddedTime >= myLibraryListener.getTimeLastUpate()) {
                    try {
                        InsertOrUpdateTrackStatement statement = existing ? new UpdateTrackStatement() : new InsertTrackStatement(TrackSource.ITunes);
                        statement.clear();
                        statement.setId(trackId);
                        statement.setName(name.trim());
                        statement.setArtist(StringUtils.trimToNull((String)track.get("Artist")));
                        statement.setAlbum(StringUtils.trimToNull((String)track.get("Album")));
                        statement.setTime((int)(track.get("Total Time") != null ? (Long)track.get("Total Time") / 1000 : 0));
                        statement.setTrackNumber((int)(track.get("Track Number") != null ? (Long)track.get("Track Number") : 0));
                        String fileName = file.getAbsolutePath();
                        statement.setFileName(fileName);
                        statement.setProtected(FileSupportUtils.isProtected(fileName));
                        statement.setVideo(track.get("Has Video") != null && ((Boolean)track.get("Has Video")).booleanValue());
                        statement.setGenre(StringUtils.trimToNull((String)track.get("Genre")));
                        if (FileSupportUtils.isMp4(file)) {
                            String kind = (String)track.get("Kind");
                            if (StringUtils.isNotEmpty(kind)) {
                                kind = kind.toLowerCase();
                                if (kind.contains("aac")) {
                                    statement.setMp4Codec("mp4a");
                                } else if (kind.contains("apple lossless")) {
                                    statement.setMp4Codec("alac");
                                }
                            }
                        }
                        myDataStoreSession.executeStatement(statement);
                        return true;
                    } catch (SQLException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not insert track \"" + name + "\" into database", e);
                        }
                    }
                }
                return false;
            }
        }
        myTrackIdToPersId.remove(track.get("Track ID"));
        return false;
    }

    public static class SupportedFileFilter implements FilenameFilter {
        public boolean accept(File parent, String filename) {
            return FileSupportUtils.isSupported(filename);
        }
    }
}
