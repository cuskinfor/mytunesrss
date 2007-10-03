package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.camel.mp3.*;
import de.codewave.camel.mp4.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.meta.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.io.*;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.*;
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
    private static final String ATOM_ALBUM = "moov.udta.meta.ilst.\u00a9alb.data";
    private static final String ATOM_ARTIST = "moov.udta.meta.ilst.\u00a9ART.data";
    private static final String ATOM_TITLE = "moov.udta.meta.ilst.\u00a9nam.data";
    private static final String ATOM_TRACK_NUMBER = "moov.udta.meta.ilst.trkn.data";
    private static final String ATOM_GENRE = "moov.udta.meta.ilst.\u00a9gen.data";
    private static final String ATOM_STSD = "moov.trak.mdia.minf.stbl.stsd";

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
                        if (FileSupportUtils.isMp3(file)) {
                            parseMp3MetaData(file, statement, fileId);
                        } else if (FileSupportUtils.isMp4(file)) {
                            parseMp4MetaData(file, statement, fileId);
                        }
                        FileSuffixInfo fileSuffixInfo = FileSupportUtils.getFileSuffixInfo(file.getName());
                        statement.setProtected(fileSuffixInfo.isProtected());
                        statement.setVideo(fileSuffixInfo.isVideo());
                        statement.setFileName(canonicalFilePath);
                        try {
                            myStoreSession.executeStatement(statement);
                            myUpdatedCount++;
                            DatabaseBuilderTask.doCheckpoint(myStoreSession);
                            if (myUpdatedCount % MyTunesRssDataStore.UPDATE_HELP_TABLES_FREQUENCY == 0) {
                                // recreate help tables every N tracks
                                try {
                                    myStoreSession
                                            .executeStatement(new RecreateHelpTablesStatement(myStoreSession.executeQuery(new FindAlbumArtistMappingQuery())));
                                    myStoreSession.commit();
                                } catch (SQLException e) {
                                    if (LOG.isErrorEnabled()) {
                                        LOG.error("Could not recreate help tables..", e);
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

    private void parseMp3MetaData(File file, InsertOrUpdateTrackStatement statement, String fileId) {
        Id3Tag tag = null;
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
    }

    private void parseMp4MetaData(File file, InsertOrUpdateTrackStatement statement, String fileId) {
        Map<String, Mp4Atom> atoms = null;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reading ATOM information from file \"" + file.getAbsolutePath() + "\".");
            }
            atoms = Mp4Utils.getAtoms(file, Arrays.asList(ATOM_ALBUM, ATOM_ARTIST, ATOM_TITLE, ATOM_TRACK_NUMBER, ATOM_GENRE, ATOM_STSD));
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get ATOM information from file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        if (atoms == null || atoms.isEmpty()) {
            setSimpleInfo(statement, file);
        } else {
            try {
                String album = atoms.get(ATOM_ALBUM).getDataAsString(8, "UTF-8");
                if (StringUtils.isEmpty(album)) {
                    album = getAncestorAlbumName(file);
                }
                statement.setAlbum(album);
                String artist = atoms.get(ATOM_ARTIST).getDataAsString(8, "UTF-8");
                if (StringUtils.isEmpty(artist)) {
                    artist = getAncestorArtistName(file);
                }
                statement.setArtist(artist);
                String name = atoms.get(ATOM_TITLE).getDataAsString(8, "UTF-8");
                if (StringUtils.isEmpty(name)) {
                    name = FilenameUtils.getBaseName(file.getName());
                }
                statement.setName(name);
                //statement.setTime(atoms.get(ATOM_TIME).getData()[11]);
                statement.setTrackNumber(atoms.get(ATOM_TRACK_NUMBER).getData()[11]);
                statement.setMp4Codec(atoms.get(ATOM_STSD).getDataAsString(24, 4, "UTF-8"));
                String genre = atoms.get(ATOM_GENRE).getDataAsString(8, "UTF-8");
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