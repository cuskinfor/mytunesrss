package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.camel.mp3.Id3Tag;
import de.codewave.camel.mp3.Id3v1Tag;
import de.codewave.camel.mp3.Id3v2Tag;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp4.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssExifUtils;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.TrackMetaData;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.utils.io.FileProcessor;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * de.codewave.mytunesrss.datastore.filesystem.MyTunesRssFileProcessor
 */
public class MyTunesRssFileProcessor implements FileProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssFileProcessor.class);

    private static enum Atom {
        Album("moov.udta.meta.ilst.\u00a9alb.data"),
        Artist("moov.udta.meta.ilst.\u00a9art.data"),
        AlbumArtist("moov.udta.meta.ilst.aART.data"),
        Title("moov.udta.meta.ilst.\u00a9nam.data"),
        TrackNumber("moov.udta.meta.ilst.trkn.data"),
        DiskNumber("moov.udta.meta.ilst.disk.data"),
        Genre("moov.udta.meta.ilst.\u00a9gen.data"),
        Stsd("moov.trak.mdia.minf.stbl.stsd"),
        Cover("moov.udta.meta.ilst.covr.data"),
        Composer("moov.udta.meta.ilst.\u00a9wrt.data"),
        Comment("moov.udta.meta.ilst.\u00a9cmt.data"),
        Year("moov.udta.meta.ilst.\u00a9day.data"),
        Series("moov.udta.meta.ilst.tvsh.data"),
        Season("moov.udta.meta.ilst.tvsn.data"),
        Episode("moov.udta.meta.ilst.tves.data"),
        Compilation("moov.udta.meta.ilst.cpil"),
        Stik("moov.udta.meta.ilst.stik");

        private String myPath;

        Atom(String path) {
            myPath = path;
        }

        public String getPath() {
            return myPath;
        }
    }

    private static final Collection<String> ALL_ATOM_NAMES = new ArrayList<String>();
    static {
        for (Atom atom : Atom.values()) {
            ALL_ATOM_NAMES.add(atom.getPath());
        }
    }

    private long myLastUpdateTime;
    private DataStoreSession myStoreSession;
    private int myUpdatedCount;
    private Set<String> myExistingIds = new HashSet<String>();
    private Collection<String> myTrackIds;
    private Collection<String> myPhotoIds;
    private String[] myDisabledMp4Codecs;
    private WatchfolderDatasourceConfig myDatasourceConfig;
    private Map<String, Pattern> myPatterns = new HashMap<String, Pattern>();
    private Set<String> myPhotoAlbumIds;

    public MyTunesRssFileProcessor(WatchfolderDatasourceConfig datasourceConfig, DataStoreSession storeSession, long lastUpdateTime, Collection<String> trackIds, Collection<String> photoIds) throws SQLException {
        myDatasourceConfig = datasourceConfig;
        myStoreSession = storeSession;
        myLastUpdateTime = lastUpdateTime;
        myTrackIds = trackIds;
        myPhotoIds = photoIds;
        myDisabledMp4Codecs = StringUtils.split(StringUtils.lowerCase(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getDisabledMp4Codecs())), ",");
        myPhotoAlbumIds = new HashSet<String>(myStoreSession.executeQuery(new FindPhotoAlbumIdsQuery()));
    }

    public Set<String> getExistingIds() {
        return myExistingIds;
    }

    public Set<String> getExistingPhotoAlbumIds() {
        return myPhotoAlbumIds;
    }

    public int getUpdatedCount() {
        return myUpdatedCount;
    }

    public void process(File file) {
        try {
            if (file.isFile() && FileSupportUtils.isSupported(file.getName())) {
                String fileId = "file_" + IOUtils.getFilenameHash(file);
                if (!myExistingIds.contains(fileId)) {
                    boolean existing = myTrackIds.contains(fileId) || myPhotoIds.contains(fileId);
                    if (existing) {
                        myExistingIds.add(fileId);
                    }
                    if ((file.lastModified() >= myLastUpdateTime || !existing || (FileSupportUtils.isMp4(file) && myDisabledMp4Codecs.length > 0))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Processing file \"" + file.getAbsolutePath() + "\".");
                        }
                        FileType type = MyTunesRss.CONFIG.getFileType(FileSupportUtils.getFileSuffix(file.getName()));
                        if (type.getMediaType() == MediaType.Image) {
                            insertOrUpdateImage(file, fileId, existing);
                        } else {
                            if (insertOrUpdateTrack(file, fileId, existing, type)) {
                                return; // early return!!!
                            }

                        }
                    }
                    DatabaseBuilderCallable.doCheckpoint(myStoreSession, false);
                }
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not process file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        myTrackIds.removeAll(myExistingIds);
        myPhotoIds.removeAll(myExistingIds);
    }

    private boolean insertOrUpdateTrack(File file, String fileId, boolean existingTrack, FileType type) throws IOException {
        String canonicalFilePath = file.getCanonicalPath();
        InsertOrUpdateTrackStatement statement;
        if (!MyTunesRss.CONFIG.isIgnoreArtwork()) {
            statement = existingTrack ? new UpdateTrackAndImageStatement(TrackSource.FileSystem) : new InsertTrackAndImageStatement(TrackSource.FileSystem);
        } else {
            statement = existingTrack ? new UpdateTrackStatement(TrackSource.FileSystem) : new InsertTrackStatement(TrackSource.FileSystem);
        }
        statement.clear();
        // never set any statement information here, since they are cleared once again later for MP4 file
        // if meta data from files should be ignored.
        TrackMetaData meta = null;
        if (!myDatasourceConfig.isIgnoreFileMeta() && FileSupportUtils.isMp3(file)) {
            meta = parseMp3MetaData(file, statement, fileId, type.getMediaType());
        } else if (!myDatasourceConfig.isIgnoreFileMeta() && FileSupportUtils.isMp4(file)) {
            // we have to fetch meta data even if they should be ignored to get the MP4 codec
            meta = parseMp4MetaData(file, statement, fileId, type.getMediaType());
            if (meta.getMp4Codec() != null && ArrayUtils.contains(myDisabledMp4Codecs, meta.getMp4Codec().toLowerCase())) {
                myExistingIds.remove(fileId);
                DatabaseBuilderCallable.doCheckpoint(myStoreSession, false);
                return true;
            }
        } else {
            setSimpleInfo(statement, file, type.getMediaType());
        }
        statement.setId(fileId);
        statement.setProtected(type.isProtected());
        statement.setMediaType(type.getMediaType());
        statement.setFileName(canonicalFilePath);
        try {
            myStoreSession.executeStatement(statement);
            if (meta != null && meta.getImage() != null && !MyTunesRss.CONFIG.isIgnoreArtwork()) {
                HandleTrackImagesStatement handleTrackImagesStatement = new HandleTrackImagesStatement(file, fileId, meta.getImage(), 0);
                myStoreSession.executeStatement(handleTrackImagesStatement);
            } else if (type.getMediaType() == MediaType.Image || !MyTunesRss.CONFIG.isIgnoreArtwork()) {
                HandleTrackImagesStatement handleTrackImagesStatement = new HandleTrackImagesStatement(TrackSource.FileSystem, file, fileId, 0, type.getMediaType() == MediaType.Image);
                myStoreSession.executeStatement(handleTrackImagesStatement);
            }
            myUpdatedCount++;
            DatabaseBuilderCallable.updateHelpTables(myStoreSession, myUpdatedCount);
            myExistingIds.add(fileId);
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not insert track \"" + canonicalFilePath + "\" into database", e);
            }
        }
        return false;
    }

    private void insertOrUpdateImage(File photoFile, final String photoFileId, boolean existingPhoto) throws IOException {
        String canonicalFilePath = photoFile.getCanonicalPath();
        InsertOrUpdatePhotoStatement statement = existingPhoto ? new UpdatePhotoStatement() : new InsertPhotoStatement();
        statement.clear();
        statement.setId(photoFileId);
        statement.setName(photoFile.getName());
        statement.setFile(canonicalFilePath);
        try {
            IImageMetadata imageMeta = Sanselan.getMetadata(photoFile);
            if (imageMeta instanceof JpegImageMetadata) {
                JpegImageMetadata jpegMeta = (JpegImageMetadata) imageMeta;
                TiffField exifCreateDateTiffField = jpegMeta.findEXIFValue(TiffConstants.EXIF_TAG_CREATE_DATE);
                if (exifCreateDateTiffField != null) {
                    String value = (String) exifCreateDateTiffField.getValue();
                    if (StringUtils.isNotBlank(value) && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("EXIF create date for \"" + photoFile.getAbsolutePath() + "\" is \"" + value + "\".");
                    }
                    Long createDate = MyTunesRssExifUtils.getCreateDate(photoFile);
                    statement.setDate(createDate != null ? createDate.longValue() : -1);
                }
            }
        } catch (ImageReadException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not read EXIF data from \"" + photoFile.getAbsolutePath() + "\".");
            }
        }
        try {
            myStoreSession.executeStatement(statement);
            HandlePhotoImagesStatement handlePhotoImagesStatement = new HandlePhotoImagesStatement(photoFile, photoFileId, 0);
            myStoreSession.executeStatement(handlePhotoImagesStatement);
            String albumName = getPhotoAlbum(photoFile);
            try {
                final String albumId = new String(Hex.encodeHex(MessageDigest.getInstance("SHA-1").digest(albumName.getBytes("UTF-8"))));
                boolean update = myPhotoAlbumIds.contains(albumId);
                if (update) {
                    if (myStoreSession.executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<Boolean>>() {
                        @Override
                        public QueryResult<Boolean> execute(Connection connection) throws SQLException {
                            SmartStatement checkPhotoAlbumLinkStatement = MyTunesRssUtils.createStatement(connection, "checkPhotoAlbumLink");
                            checkPhotoAlbumLinkStatement.setString("album", albumId);
                            checkPhotoAlbumLinkStatement.setString("photo", photoFileId);
                            return execute(checkPhotoAlbumLinkStatement, new ResultBuilder<Boolean>() {
                                public Boolean create(ResultSet resultSet) throws SQLException {
                                    return true;
                                }
                            });
                        }
                    }).getResultSize() == 0) {
                        SavePhotoAlbumStatement savePhotoAlbumStatement = new SavePhotoAlbumStatement();
                        savePhotoAlbumStatement.setId(albumId);
                        savePhotoAlbumStatement.setUpdate(true);
                        savePhotoAlbumStatement.setAdd(true);
                        savePhotoAlbumStatement.setPhotoIds(Collections.singletonList(photoFileId));
                        myStoreSession.executeStatement(savePhotoAlbumStatement);
                    }
                } else {
                    SavePhotoAlbumStatement savePhotoAlbumStatement = new SavePhotoAlbumStatement();
                    savePhotoAlbumStatement.setId(albumId);
                    savePhotoAlbumStatement.setName(albumName);
                    savePhotoAlbumStatement.setPhotoIds(Collections.singletonList(photoFileId));
                    myStoreSession.executeStatement(savePhotoAlbumStatement);
                    myPhotoAlbumIds.add(albumId);
                }
            } catch (NoSuchAlgorithmException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not create message digest.", e);
                }
            }
            myUpdatedCount++;
            DatabaseBuilderCallable.updateHelpTables(myStoreSession, myUpdatedCount);
            myExistingIds.add(photoFileId);
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not insert photo \"" + canonicalFilePath + "\" into database", e);
            }
        }
    }

    private TrackMetaData parseMp3MetaData(File file, InsertOrUpdateTrackStatement statement, String fileId, MediaType mediaType) {
        TrackMetaData meta = new TrackMetaData();
        Id3Tag tag = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading ID3 information from file \"" + file.getAbsolutePath() + "\".");
            }
            tag = Mp3Utils.readId3Tag(file);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        if (tag == null) {
            setSimpleInfo(statement, file, mediaType);
        } else {
            try {
                String album = tag.getAlbum();
                if (StringUtils.isEmpty(album)) {
                    album = getFallbackAlbumName(file);
                }
                statement.setAlbum(MyTunesRssUtils.normalize(album));
                String artist = tag.getArtist();
                if (StringUtils.isEmpty(artist)) {
                    artist = getFallbackArtistName(file);
                }
                statement.setArtist(MyTunesRssUtils.normalize(artist));
                String name = tag.getTitle();
                if (StringUtils.isEmpty(name)) {
                    name = FilenameUtils.getBaseName(file.getName());
                }
                String yearString = StringUtils.defaultIfEmpty(tag.getYear(), "-1");
                try {
                    statement.setYear(Integer.parseInt(yearString));
                } catch (NumberFormatException e) {
                    LOGGER.warn("Illegal YEAR value \"" + yearString + "\" in \"" + file + "\".");
                    statement.setYear(-1);
                }
                statement.setName(MyTunesRssUtils.normalize(name));
                String albumArtist = artist;
                if (tag.isId3v2()) {
                    Id3v2Tag id3v2Tag = ((Id3v2Tag) tag);
                    albumArtist = id3v2Tag.getAlbumArtist();
                    if (StringUtils.isEmpty(albumArtist)) {
                        albumArtist = artist;
                    }
                    String composer = id3v2Tag.getComposer();
                    statement.setComposer(MyTunesRssUtils.normalize(composer));
                    statement.setTime(id3v2Tag.getTimeSeconds());
                    statement.setTrackNumber(id3v2Tag.getTrackNumber());
                    statement.setCompilation(!StringUtils.equalsIgnoreCase(artist, albumArtist));
                    meta.setImage(MyTunesRssMp3Utils.getImage(id3v2Tag));
                    String pos = id3v2Tag.getPos();
                    try {
                        if (StringUtils.isNotEmpty(pos)) {
                            String[] posParts = pos.split("/");
                            if (posParts.length == 1) {
                                statement.setPos(Integer.parseInt(posParts[0].trim()), 0);
                            } else if (posParts.length == 2) {
                                statement.setPos(Integer.parseInt(posParts[0].trim()), Integer.parseInt(posParts[1].trim()));
                            }
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Illegal TPA/TPOS value \"" + pos + "\" in \"" + file + "\".");
                    }
                }
                statement.setAlbumArtist(albumArtist);
                String genre = tag.getGenreAsString();
                if (genre != null) {
                    statement.setGenre(StringUtils.trimToNull(genre));
                }
                statement.setComment(MyTunesRssUtils.normalize(StringUtils.trimToNull(createComment(tag))));
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
                }
                statement.clear();
                statement.setId(fileId);
                setSimpleInfo(statement, file, mediaType);
            }
        }
        return meta;
    }

    private String createComment(Id3Tag tag) {
        try {
            if (tag.isId3v2()) {
                String comment = " " + MyTunesRss.CONFIG.getId3v2TrackComment() + " ";// make sure the comment does neither start nor end with a token
                if (StringUtils.isNotBlank(comment)) {
                    for (int s = comment.indexOf("${"); s > -1; s = comment.indexOf("${")) {
                        int e = comment.indexOf("}", s);
                        if (e != -1) {
                            String[] instructions = comment.substring(s + 2, e).split(";");
                            String[] tokens = instructions[0].split(",");
                            String tagData;
                            if (instructions.length > 2 && instructions[2].trim().toUpperCase().contains("M")) {
                                tagData = ((Id3v2Tag) tag).getFrameBodiesToString(tokens[0].trim(),
                                        tokens.length == 1 ? tokens[0].trim() : tokens[1].trim(),
                                        "\n");
                            } else {
                                tagData = ((Id3v2Tag) tag).getFrameBodyToString(tokens[0].trim(),
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
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Created comment for ID3 tag: \"" + StringUtils.trimToEmpty(comment) + "\"");
                    }
                }
                return StringUtils.trimToNull(comment);
            }
            return ((Id3v1Tag) tag).getComment();
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not create comment for ID3 tag", e);
            }
        }
        return null;
    }

    private TrackMetaData parseMp4MetaData(File file, InsertOrUpdateTrackStatement statement, String fileId, MediaType mediaType) {
        TrackMetaData meta = new TrackMetaData();
        Mp4AtomList atoms = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading ATOM information from file \"" + file.getAbsolutePath() + "\".");
            }
            atoms = MyTunesRss.MP4_PARSER.parse(file, ALL_ATOM_NAMES.toArray(new String[ALL_ATOM_NAMES.size()]));
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get ATOM information from file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        if (atoms == null || atoms.toList().isEmpty()) {
            setSimpleInfo(statement, file, mediaType);
        } else {
            try {
                Mp4Atom atom = atoms.getFirst(Atom.Title.getPath());
                String name = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isBlank(name)) {
                    name = FilenameUtils.getBaseName(file.getName());
                }
                statement.setName(MyTunesRssUtils.normalize(name));
                atom = atoms.getFirst(Atom.Stsd.getPath());
                if (atom != null) {
                    String mp4Codec = atom.getDataAsString(12, 4, "UTF-8");
                    meta.setMp4Codec(mp4Codec);
                    statement.setMp4Codec(mp4Codec);
                }
                atom = atoms.getFirst(Atom.Genre.getPath());
                String genre = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isNotBlank(genre)) {
                    statement.setGenre(MyTunesRssUtils.normalize(genre));
                }
                atom = atoms.getFirst(Atom.Comment.getPath());
                String comment = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isNotBlank(comment)) {
                    statement.setComment(MyTunesRssUtils.normalize(comment));
                }
                atom = atoms.getFirst(Atom.Year.getPath());
                if (atom != null) {
                    String year = atom.getDataAsString(8, "UTF-8");
                    if (StringUtils.isNotBlank(year)) {
                        try {
                            statement.setYear(Integer.parseInt(year));
                        } catch (NumberFormatException e) {
                            statement.setYear(Integer.parseInt(year.substring(0, StringUtils.indexOfAnyBut(year, "1234567890"))));
                        }
                    }
                }
                if (mediaType == MediaType.Audio) {
                    atom = atoms.getFirst(Atom.Album.getPath());
                    String album = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                    if (StringUtils.isBlank(album)) {
                        album = getFallbackAlbumName(file);
                    }
                    statement.setAlbum(MyTunesRssUtils.normalize(album));
                    atom = atoms.getFirst(Atom.Artist.getPath());
                    String artist = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                    if (StringUtils.isBlank(artist)) {
                        artist = getFallbackArtistName(file);
                    }
                    statement.setArtist(MyTunesRssUtils.normalize(artist));
                    atom = atoms.getFirst(Atom.AlbumArtist.getPath());
                    String albumArtist = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                    if (StringUtils.isBlank(albumArtist)) {
                        albumArtist = artist;
                    }
                    statement.setAlbumArtist(MyTunesRssUtils.normalize(albumArtist));
                    atom = atoms.getFirst(Atom.Compilation.getPath());
                    if (atom != null) {
                        statement.setCompilation(((CompilationAtom)atom).isCompilation() || !StringUtils.equalsIgnoreCase(artist, albumArtist));
                    }
                    atom = atoms.getFirst(Atom.Composer.getPath());
                    String composer = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                    statement.setComposer(MyTunesRssUtils.normalize(composer));
                    atom = atoms.getFirst(Atom.TrackNumber.getPath());
                    if (atom != null) {
                        statement.setTrackNumber(atom.getData()[11]);
                    }
                    atom = atoms.getFirst(Atom.DiskNumber.getPath());
                    if (atom != null) {
                        statement.setPos(atom.getData()[11], atom.getData()[13]);
                    }
                } else if (mediaType == MediaType.Video) {
                    atom = atoms.getFirst(Atom.Stik.getPath());
                    VideoType videoType;
                    if (atom != null) {
                        videoType = ((StikAtom) atom).getType() == StikAtom.Type.TvShow ? VideoType.TvShow : VideoType.Movie;
                        statement.setVideoType(videoType);
                    } else {
                        videoType = myDatasourceConfig.getVideoType();
                        statement.setVideoType(videoType);
                    }
                    if (videoType == VideoType.TvShow) {
                        atom = atoms.getFirst(Atom.Series.getPath());
                        String tvShow = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                        if (StringUtils.isNotBlank(tvShow)) {
                            statement.setSeries(MyTunesRssUtils.normalize(tvShow));
                        } else {
                            statement.setSeries(getFallbackSeries(file));
                        }
                        atom = atoms.getFirst(Atom.Season.getPath());
                        if (atom != null) {
                            statement.setSeason(atom.getData()[11]);
                        } else {
                            statement.setSeason(getFallbackSeason(file));
                        }
                        atom = atoms.getFirst(Atom.Episode.getPath());
                        if (atom != null) {
                            statement.setEpisode(atom.getData()[11]);
                        } else {
                            statement.setEpisode(getFallbackEpisode(file));
                        }
                    }
                }
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse MP4 ATOM information from file \"" + file.getAbsolutePath() + "\".", e);
                }
                statement.clear();
                statement.setId(fileId);
                setSimpleInfo(statement, file, mediaType);
            }
        }
        Mp4Atom atom = atoms.getFirst(Atom.Cover.getPath());
        if (atom != null) {
            byte type = atom.getData()[3];
            meta.setImage(new Image(type == 0x0d ? "image/jpeg" : "image/png", ArrayUtils.subarray(atom.getData(), 8, atom.getData().length - 8)));
        }
        return meta;
    }

    private void setSimpleInfo(InsertOrUpdateTrackStatement statement, File file, MediaType mediaType) {
        statement.setName(FilenameUtils.getBaseName(file.getName()));
        if (mediaType == MediaType.Audio) {
            statement.setAlbum(getFallbackAlbumName(file));
            statement.setArtist(getFallbackArtistName(file));
        }
        if (mediaType == MediaType.Video && myDatasourceConfig.getVideoType() == VideoType.TvShow) {
            statement.setSeries(getFallbackSeries(file));
            statement.setSeason(getFallbackSeason(file));
            statement.setEpisode(getFallbackEpisode(file));
        }
    }

    private String getFallbackAlbumName(File file) {
        return myDatasourceConfig.getAlbumFallback() != null ? getFallbackName(file, new String(myDatasourceConfig.getAlbumFallback())) : null;
    }

    private String getFallbackArtistName(File file) {
        return getFallbackName(file, new String(myDatasourceConfig.getArtistFallback()));
    }


    private String getFallbackSeries(File file) {
        return myDatasourceConfig.getSeriesFallback() != null ? getFallbackName(file, new String(myDatasourceConfig.getSeriesFallback())) : null;
    }

    private int getFallbackSeason(File file) {
        if (myDatasourceConfig.getSeasonFallback() != null) {
            String fallback = StringUtils.trimToNull(getFallbackName(file, new String(myDatasourceConfig.getSeasonFallback())));
            return StringUtils.isNumeric(fallback) ? Integer.parseInt(fallback) : 0;
        } else {
            return 0;
        }
    }

    private int getFallbackEpisode(File file) {
        if (myDatasourceConfig.getEpisodeFallback() != null) {
            String fallback = StringUtils.trimToNull(getFallbackName(file, new String(myDatasourceConfig.getEpisodeFallback())));
            return StringUtils.isNumeric(fallback) ? Integer.parseInt(fallback) : 0;
        } else {
            return 0;
        }
    }

    private String getPhotoAlbum(File file) {
        return myDatasourceConfig.getPhotoAlbumPattern() != null ? getFallbackName(file, new String(myDatasourceConfig.getPhotoAlbumPattern())) : null;
    }

    String getFallbackName(File file, String pattern) {
        String name = new String(pattern);
        String[] dirTokens = StringUtils.substringsBetween(pattern, "[[[dir:", "]]]");
        if (dirTokens != null) {
            for (String token : dirTokens) {
                String[] numberAndRegExp = StringUtils.split(StringUtils.trimToEmpty(token), ":", 2);
                for (int i = 0; i < numberAndRegExp.length; i++) {
                    numberAndRegExp[i] = StringUtils.trimToNull(numberAndRegExp[i]);
                }
                if (numberAndRegExp.length > 0 && StringUtils.isNumeric(numberAndRegExp[0])) {
                    int number = Integer.parseInt(numberAndRegExp[0]);
                    File dir = file.getParentFile();
                    while (dir != null && number > 0) {
                        dir = dir.getParentFile();
                        number--;
                    }
                    if (dir != null && dir.isDirectory()) {
                        if (numberAndRegExp.length == 1) {
                            name = name.replace("[[[dir:" + token + "]]]", dir.getName());
                        } else {
                            Pattern regExpPattern = myPatterns.get(numberAndRegExp[1]);
                            if (regExpPattern == null) {
                                regExpPattern = Pattern.compile(numberAndRegExp[1]);
                                myPatterns.put(numberAndRegExp[1], regExpPattern);
                            }
                            Matcher matcher = regExpPattern.matcher(dir.getName());
                            if (matcher.find()) {
                                name = name.replace("[[[dir:" + token + "]]]", matcher.group(matcher.groupCount()));
                            } else {
                                name = name.replace("[[[dir:" + token + "]]]", "");
                            }
                        }
                    }
                }
            }
        }
        String[] fileTokens = StringUtils.substringsBetween(pattern, "[[[file", "]]]");
        if (fileTokens != null) {
            for (String token : fileTokens) {
                String trimmedToken = StringUtils.trimToNull(token);
                if (trimmedToken != null && trimmedToken.length() > 1 && trimmedToken.startsWith(":")) {
                    Pattern regExpPattern = myPatterns.get(trimmedToken.substring(1));
                    if (regExpPattern == null) {
                        regExpPattern = Pattern.compile(trimmedToken.substring(1));
                        myPatterns.put(trimmedToken.substring(1), regExpPattern);
                    }
                    Matcher matcher = regExpPattern.matcher(file.getName());
                    if (matcher.find()) {
                        name = name.replace("[[[file" + token + "]]]", matcher.group(matcher.groupCount()));
                    } else {
                        name = name.replace("[[[file" + token + "]]]", "");
                    }
                } else {
                    name = name.replace("[[[file" + token + "]]]", file.getName());
                }
            }
        }
        name = StringUtils.trimToNull(name);
        LOGGER.debug("Fallback name for \"" + file + "\" and pattern \"" + pattern + "\" is \"" + name + "\".");
        return name;
    }
}