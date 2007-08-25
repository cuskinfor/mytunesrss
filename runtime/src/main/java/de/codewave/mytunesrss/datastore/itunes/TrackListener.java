package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.xml.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.TrackListenerr
 */
public class TrackListener implements PListHandlerListener {
    private static final Log LOG = LogFactory.getLog(TrackListener.class);

    private DataStoreSession myDataStoreSession;
    private LibraryListener myLibraryListener;
    private int myUpdatedCount;
    private Set<String> myExistingIds = new HashSet<String>();
    private Set<String> myDatabaseIds = new HashSet<String>();
    private Map<Long, String> myTrackIdToPersId;

    public TrackListener(DataStoreSession dataStoreSession, LibraryListener libraryListener, Map<Long, String> trackIdToPersId) throws SQLException {
        myDataStoreSession = dataStoreSession;
        myLibraryListener = libraryListener;
        myDatabaseIds = (Set<String>)dataStoreSession.executeQuery(new FindTrackIdsQuery(TrackSource.ITunes.name()));
        myTrackIdToPersId = trackIdToPersId;
    }

    public Set<String> getExistingIds() {
        return myExistingIds;
    }

    public int getUpdatedCount() {
        return myUpdatedCount;
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        Map track = (Map)value;
        String trackId = myLibraryListener.getLibraryId() + "_";
        trackId += track.get("Persistent ID") != null ? track.get("Persistent ID").toString() : "TrackID" + track.get("Track ID").toString();
        myExistingIds.add(trackId);
        myTrackIdToPersId.put((Long)track.get("Track ID"), trackId);
        if (processTrack(track)) {
            myUpdatedCount++;
            if (myUpdatedCount % 100 == 0) {
                // commit every 100 tracks
                if (myUpdatedCount % 500 == 0) {
                    // recreate help tables every 500 tracks
                    try {
                        myDataStoreSession
                                .executeStatement(new RecreateHelpTablesStatement(myDataStoreSession.executeQuery(new FindAlbumArtistMappingQuery())));
                    } catch (SQLException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not recreate help tables..", e);
                        }
                    }
                }
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Committing transaction after 100 inserted/updated tracks.");
                    }
                    myDataStoreSession.commit();
//                    myDataStoreSession.commitAndContinue();
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not commit transaction.", e);
                    }
                }
            }
        }
        return false;
    }

    public boolean beforeArrayAdd(List array, Object value) {
        throw new UnsupportedOperationException("method beforeArrayAdd of class ItunesLoader$TrackListener is not supported!");
    }

    private boolean processTrack(Map track) {
        String trackId = myLibraryListener.getLibraryId() + "_";
        trackId += track.get("Persistent ID") != null ? track.get("Persistent ID").toString() : "TrackID" + track.get("Track ID").toString();
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
                if (!myDatabaseIds.contains(trackId) || dateModifiedTime >= myLibraryListener.getTimeLastUpate() ||
                        dateAddedTime >= myLibraryListener.getTimeLastUpate()) {
                    try {
                        InsertOrUpdateTrackStatement statement =
                                myDatabaseIds.contains(trackId) ? new UpdateTrackStatement() : new InsertTrackStatement(TrackSource.ITunes);
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
        myExistingIds.remove(trackId);
        myTrackIdToPersId.remove(track.get("Track ID"));
        return false;
    }

    public static class SupportedFileFilter implements FilenameFilter {
        public boolean accept(File parent, String filename) {
            return FileSupportUtils.isSupported(filename);
        }
    }
}
