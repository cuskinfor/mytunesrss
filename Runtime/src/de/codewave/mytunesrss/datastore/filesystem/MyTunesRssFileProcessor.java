package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.camel.mp3.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.io.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.filesystem.MyTunesRssFileProcessor
 */
public class MyTunesRssFileProcessor implements FileProcessor {
    private static final Log LOG = LogFactory.getLog(MyTunesRssFileProcessor.class);

    private File myBaseDir;
    private long myLastUpdateTime;
    private DataStoreSession myStoreSession;
    private Set<String> myDatabaseIds;
    private InsertTrackStatement myInsertStatement;
    private UpdateTrackStatement myUpdateStatement;
    private int myUpdatedCount;
    private Set<String> myExistingIds = new HashSet<String>();
    private Set<String> myFoundIds = new HashSet<String>();

    public MyTunesRssFileProcessor(File baseDir, DataStoreSession storeSession, long lastUpdateTime) throws SQLException {
        myBaseDir = baseDir;
        myStoreSession = storeSession;
        myLastUpdateTime = lastUpdateTime;
        myDatabaseIds = (Set<String>)storeSession.executeQuery(new FindTrackIdsQuery(TrackSource.FileSystem.name()));
        myInsertStatement = new InsertTrackStatement(storeSession, TrackSource.FileSystem);
        myUpdateStatement = new UpdateTrackStatement(storeSession);
    }

    public Set<String> getExistingIds() {
        return myExistingIds;
    }

    public int getUpdatedCount() {
        return myUpdatedCount;
    }

    public void process(File file) {
        try {
            String canonicalFilePath = file.getCanonicalPath();
            if (file.isFile()) {
                String fileId = IOUtils.getFileIdentifier(file);
                if (!myFoundIds.contains(fileId)) {
                    if ((file.lastModified() >= myLastUpdateTime || !myDatabaseIds.contains(fileId))) {
                        InsertOrUpdateTrackStatement statement = myDatabaseIds.contains(fileId) ? myUpdateStatement : myInsertStatement;
                        statement.clear();
                        statement.setId(fileId);
                        Id3Tag tag = null;
                        if ("mp3".equalsIgnoreCase(IOUtils.getSuffix(file))) {
                            try {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Reading ID3 information from file \"" + file.getAbsolutePath() + "\".");
                                }
                                tag = Mp3Utils.readId3Tag(file);
                            } catch (Exception e) {
                                if (LOG.isErrorEnabled()) {
                                    LOG.error("Could not get ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
                                }
                            }
                        }
                        if (tag == null) {
                            setSimpleInfo(statement, file);
                        } else {
                            try {
                                String album = tag.getAlbum();
                                if (StringUtils.isEmpty(album)) {
                                    album = getAncestorAlbumName(file);
                                }
                                statement.setAlbum(album);
                                String artist = tag.getArtist();
                                if (StringUtils.isEmpty(artist)) {
                                    artist = getAncestorArtistName(file);
                                }
                                statement.setArtist(artist);
                                String name = tag.getTitle();
                                if (StringUtils.isEmpty(name)) {
                                    name = IOUtils.getNameWithoutSuffix(file);
                                }
                                statement.setName(name);
                                if (tag.isId3v2()) {
                                    statement.setTime(((Id3v2Tag)tag).getTimeSeconds());
                                    statement.setTrackNumber(((Id3v2Tag)tag).getTrackNumber());
                                }
                                statement.setGenre(StringUtils.trimToNull(tag.getGenreAsString()));
                            } catch (Exception e) {
                                if (LOG.isErrorEnabled()) {
                                    LOG.error("Could not parse ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
                                }
                                statement.clear();
                                statement.setId(fileId);
                                setSimpleInfo(statement, file);
                            }
                        }
                        FileSuffixInfo fileSuffixInfo = FileSupportUtils.getFileSuffixInfo(file.getName());
                        statement.setProtected(fileSuffixInfo.isProtected());
                        statement.setVideo(fileSuffixInfo.isVideo());
                        statement.setFileName(canonicalFilePath);
                        try {
                            myStoreSession.executeStatement(statement);
                            myUpdatedCount++;
                            if (myUpdatedCount % 5000 == 0) {// commit every 5000 tracks to not run out of memory
                                try {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Committing transaction after 5000 inserted/updated tracks.");
                                    }
                                    myStoreSession.commitAndContinue();
                                } catch (SQLException e) {
                                    if (LOG.isErrorEnabled()) {
                                        LOG.error("Could not commit block of track updates.", e);
                                    }
                                }
                            }
                            myFoundIds.add(fileId);
                            myExistingIds.add(fileId);
                        } catch (SQLException e) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error("Could not insert track \"" + canonicalFilePath + "\" into database", e);
                            }
                        }
                    } else if (myDatabaseIds.contains(fileId)) {
                        myExistingIds.add(fileId);
                    }
                }
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not process file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
    }

    private void setSimpleInfo(InsertOrUpdateTrackStatement statement, File file) {
        statement.setName(IOUtils.getNameWithoutSuffix(file));
        statement.setAlbum(getAncestorAlbumName(file));
        statement.setArtist(getAncestorArtistName(file));
    }

    private String getAncestorAlbumName(File file) {
        return getAncestorName(file, MyTunesRss.CONFIG.getFileSystemAlbumNameFolder());
    }

    private String getAncestorName(File file, int level) {
        if (level > 0) {
            File ancestor = IOUtils.getAncestor(file, level);
            try {
                if (ancestor != null && IOUtils.isContained(myBaseDir, ancestor)) {
                    return ancestor.getName();
                }
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not check if ancestor folder is inside base folder.", e);
                }
                return null;
            }
        }
        return null;
    }

    private String getAncestorArtistName(File file) {
        return getAncestorName(file, MyTunesRss.CONFIG.getFileSystemArtistNameFolder());
    }
}