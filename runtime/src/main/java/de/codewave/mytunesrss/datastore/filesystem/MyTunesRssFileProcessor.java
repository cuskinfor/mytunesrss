package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.camel.mp3.Id3Tag;
import de.codewave.camel.mp3.Id3v1Tag;
import de.codewave.camel.mp3.Id3v2Tag;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.camel.mp4.Mp4Utils;
import de.codewave.mytunesrss.FileSuffixInfo;
import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.TrackMetaData;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.io.FileProcessor;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
    private static final String ATOM_COVER = "moov.udta.meta.ilst.covr.data";

    private File myBaseDir;
    private long myLastUpdateTime;
    private DataStoreSession myStoreSession;
    private int myUpdatedCount;
    private Set<String> myExistingIds = new HashSet<String>();
    private Collection<String> myTrackIds;

    public MyTunesRssFileProcessor(File baseDir, DataStoreSession storeSession, long lastUpdateTime, Collection<String> trackIds)
            throws SQLException {
        myBaseDir = baseDir;
        myStoreSession = storeSession;
        myLastUpdateTime = lastUpdateTime;
        myTrackIds = trackIds;
    }

    public Set<String> getExistingIds() {
        return myExistingIds;
    }

    public int getUpdatedCount() {
        return myUpdatedCount;
    }

    public void process(File file) {
        try {
            if (file.isFile() && FileSupportUtils.isSupported(file.getName())) {
                String fileId = "file_" + IOUtils.getFilenameHash(file);
                if (!myExistingIds.contains(fileId)) {
                    String canonicalFilePath = file.getCanonicalPath();
                    boolean existing = myTrackIds.contains(fileId);
                    if (existing) {
                        myExistingIds.add(fileId);
                    }
                    if ((file.lastModified() >= myLastUpdateTime || !existing)) {
                        InsertOrUpdateTrackStatement statement;
                        if (!MyTunesRss.CONFIG.isIgnoreArtwork()) {
                            statement = existing ? new UpdateTrackAndImageStatement() : new InsertTrackAndImageStatement(TrackSource.FileSystem);
                        } else {
                            statement = existing ? new UpdateTrackStatement() : new InsertTrackStatement(TrackSource.FileSystem);
                        }
                        statement.clear();
                        statement.setId(fileId);
                        TrackMetaData meta = null;
                        if (FileSupportUtils.isMp3(file)) {
                            meta = parseMp3MetaData(file, statement, fileId);
                        } else if (FileSupportUtils.isMp4(file)) {
                            meta = parseMp4MetaData(file, statement, fileId);
                        }
                        FileSuffixInfo fileSuffixInfo = FileSupportUtils.getFileSuffixInfo(file.getName());
                        statement.setProtected(fileSuffixInfo.isProtected());
                        statement.setVideo(fileSuffixInfo.isVideo());
                        statement.setFileName(canonicalFilePath);
                        try {
                            myStoreSession.executeStatement(statement);
                            if (meta.getImage() != null && !MyTunesRss.CONFIG.isIgnoreArtwork()) {
                                HandleTrackImagesStatement handleTrackImagesStatement = new HandleTrackImagesStatement(file, fileId, meta.getImage());
                                myStoreSession.executeStatement(handleTrackImagesStatement);
                            }
                            myUpdatedCount++;
                            DatabaseBuilderTask.updateHelpTables(myStoreSession, myUpdatedCount);
                            myExistingIds.add(fileId);
                        } catch (SQLException e) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error("Could not insert track \"" + canonicalFilePath + "\" into database", e);
                            }
                        }
                    }
                    DatabaseBuilderTask.doCheckpoint(myStoreSession, false);
                }
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not process file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        myTrackIds.removeAll(myExistingIds);
    }

    private TrackMetaData parseMp3MetaData(File file, InsertOrUpdateTrackStatement statement, String fileId) {
        TrackMetaData meta = new TrackMetaData();
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
                    meta.setImage(MyTunesRssMp3Utils.getImage(id3v2Tag));
                    String pos = id3v2Tag.getFrameBodyToString("TPA", "TPOS");
                    if (StringUtils.isNotEmpty(pos)) {
                        String[] posParts = pos.split("/");
                        if (posParts.length == 1) {
                            statement.setPos(Integer.parseInt(posParts[0].trim()), 0);
                        } else if (posParts.length == 2) {
                            statement.setPos(Integer.parseInt(posParts[0].trim()), Integer.parseInt(posParts[1].trim()));
                        }
                    }
                }
                String genre = tag.getGenreAsString();
                if (genre != null) {
                    statement.setGenre(StringUtils.trimToNull(genre));
                }
                statement.setComment(StringUtils.trimToNull(createComment(tag)));
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not parse ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
                }
                statement.clear();
                statement.setId(fileId);
                setSimpleInfo(statement, file);
            }
        }
        return meta;
    }

    private String createComment(Id3Tag tag) {
        try {
            if (tag.isId3v2()) {
                String comment = " " + MyTunesRss.CONFIG.getId3v2TrackComment() + " "; // make sure the comment does neither start nor end with a token
                if (StringUtils.isNotBlank(comment)) {
                    for (int s = comment.indexOf("${"); s > -1; s = comment.indexOf("${")) {
                        int e = comment.indexOf("}", s);
                        if (e != -1) {
                            String[] instructions = comment.substring(s + 2, e).split(";");
                            String[] tokens = instructions[0].split(",");
                            String tagData;
                            if (instructions.length > 2 && instructions[2].trim().toUpperCase().contains("M")) {
                                tagData = ((Id3v2Tag)tag).getFrameBodiesToString(tokens[0].trim(), tokens.length == 1 ? tokens[0].trim() : tokens[1].trim(), "\n");
                            } else {
                                tagData = ((Id3v2Tag)tag).getFrameBodyToString(tokens[0].trim(),
                                                                                 tokens.length == 1 ? tokens[0].trim() : tokens[1].trim());
                                }
                            String value = StringUtils.trimToEmpty(tagData);
                            if (StringUtils.isEmpty(value) && instructions.length > 1) {
                                value = instructions[1];
                                }
                                comment = comment.substring(0, s) + value + comment.substring(e + 1);
                            }
                        }
                    }
                if (StringUtils.isNotBlank(comment)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Created comment for ID3 tag: \"" + StringUtils.trimToEmpty(comment) + "\"");
                    }
                }
                return StringUtils.trimToNull(comment);
            }
            return ((Id3v1Tag)tag).getComment();
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not create comment for ID3 tag", e);
            }
        }
        return null;
    }

    private TrackMetaData parseMp4MetaData(File file, InsertOrUpdateTrackStatement statement, String fileId) {
        TrackMetaData meta = new TrackMetaData();
        Map<String, Mp4Atom> atoms = null;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reading ATOM information from file \"" + file.getAbsolutePath() + "\".");
            }
            atoms = Mp4Utils.getAtoms(file, Arrays.asList(ATOM_ALBUM, ATOM_ARTIST, ATOM_TITLE, ATOM_TRACK_NUMBER, ATOM_GENRE, ATOM_STSD, ATOM_COVER));
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get ATOM information from file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        if (atoms == null || atoms.isEmpty()) {
            setSimpleInfo(statement, file);
        } else {
            try {
                Mp4Atom atom = atoms.get(ATOM_ALBUM);
                String album = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isEmpty(album)) {
                    album = getAncestorAlbumName(file);
                }
                statement.setAlbum(album);
                atom = atoms.get(ATOM_ARTIST);
                String artist = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isEmpty(artist)) {
                    artist = getAncestorArtistName(file);
                }
                statement.setArtist(artist);
                atom = atoms.get(ATOM_TITLE);
                String name = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isEmpty(name)) {
                    name = FilenameUtils.getBaseName(file.getName());
                }
                statement.setName(name);
                //statement.setTime(atoms.get(ATOM_TIME).getData()[11]);
                atom = atoms.get(ATOM_TRACK_NUMBER);
                if (atom != null) {
                    statement.setTrackNumber(atom.getData()[11]);
                }
                atom = atoms.get(ATOM_STSD);
                if (atom != null) {
                    statement.setMp4Codec(atom.getDataAsString(12, 4, "UTF-8"));
                }
                atom = atoms.get(ATOM_GENRE);
                String genre = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
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
        Mp4Atom atom = atoms.get(ATOM_COVER);
        if (atom != null) {
            byte type = atom.getData()[3];
            meta.setImage(new Image(type == 0x0d ? "image/jpeg" : "image/png", ArrayUtils.subarray(atom.getData(), 8, atom.getData().length - 8)));
        }
        return meta;
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