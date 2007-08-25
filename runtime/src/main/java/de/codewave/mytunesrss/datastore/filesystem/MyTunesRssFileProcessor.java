package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.camel.mp3.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.mp3.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.io.*;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.*;
import de.codewave.utils.graphics.*;
import org.apache.commons.io.*;
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
    private int myUpdatedCount;
    private Set<String> myExistingIds = new HashSet<String>();
    private Set<String> myFoundIds = new HashSet<String>();

    public MyTunesRssFileProcessor(File baseDir, DataStoreSession storeSession, long lastUpdateTime) throws SQLException {
        myBaseDir = baseDir;
        myStoreSession = storeSession;
        myLastUpdateTime = lastUpdateTime;
        myDatabaseIds = (Set<String>)storeSession.executeQuery(new FindTrackIdsQuery(TrackSource.FileSystem.name()));
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
            if (file.isFile() && FileSupportUtils.isSupported(file.getName())) {
                String fileId = "file_" + IOUtils.getFilenameHash(file);
                if (!myFoundIds.contains(fileId)) {
                    if ((file.lastModified() >= myLastUpdateTime || !myDatabaseIds.contains(fileId))) {
                        InsertOrUpdateTrackStatement statement =
                                myDatabaseIds.contains(fileId) ? new UpdateTrackStatement() : new InsertTrackStatement(TrackSource.FileSystem);
                        statement.clear();
                        statement.setId(fileId);
                        Id3Tag tag = null;
                        if ("mp3".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
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
                                    name = FilenameUtils.getBaseName(file.getName());
                                }
                                statement.setName(name);
                                if (tag.isId3v2()) {
                                    Id3v2Tag id3v2Tag = ((Id3v2Tag)tag);
                                    statement.setTime(id3v2Tag.getTimeSeconds());
                                    statement.setTrackNumber(id3v2Tag.getTrackNumber());
                                    Image image = ID3Utils.getImage(id3v2Tag);
                                    if (image != null) {
                                        myStoreSession.executeStatement(new InsertImageStatement(fileId, 32, ImageUtils.resizeImageWithMaxSize(
                                                image.getData(), 32)));
                                        myStoreSession.executeStatement(new InsertImageStatement(fileId, 64, ImageUtils.resizeImageWithMaxSize(
                                                image.getData(), 64)));
                                        myStoreSession.executeStatement(new InsertImageStatement(fileId, 128, ImageUtils.resizeImageWithMaxSize(
                                                image.getData(), 128)));
                                        myStoreSession.executeStatement(new InsertImageStatement(fileId, 256, ImageUtils.resizeImageWithMaxSize(
                                                image.getData(), 256)));
                                    }
                                }
                                String genre = tag.getGenreAsString();
                                if (genre != null) {
                                    statement.setGenre(StringUtils.trimToNull(genre));
                                }
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
                            if (myUpdatedCount % 100 == 0) {
                                // commit every 100 tracks
                                if (myUpdatedCount % 500 == 0) {
                                    // recreate help tables every 500 tracks
                                    try {
                                        myStoreSession
                                                .executeStatement(new RecreateHelpTablesStatement(myStoreSession.executeQuery(new FindAlbumArtistMappingQuery())));
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
                                    myStoreSession.commit();
                                    //                    myDataStoreSession.commitAndContinue();
                                } catch (SQLException e) {
                                    if (LOG.isErrorEnabled()) {
                                        LOG.error("Could not commit transaction.", e);
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
        statement.setName(FilenameUtils.getBaseName(file.getName()));
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