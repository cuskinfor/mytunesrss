package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.camel.mp3.Id3Tag;
import de.codewave.camel.mp3.Id3v1Tag;
import de.codewave.camel.mp3.Id3v2Tag;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp4.CoverAtom;
import de.codewave.camel.mp4.DiskAtom;
import de.codewave.camel.mp4.MoovAtom;
import de.codewave.camel.mp4.StikAtom;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.config.WatchfolderDatasourceConfig;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreStatementEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssExifUtils;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.TrackMetaData;
import de.codewave.utils.io.FileProcessor;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.tika.metadata.Metadata;
import org.h2.mvstore.MVStore;
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

    private DatabaseUpdateQueue myQueue;
    private int myUpdatedCount;
    private Map<String, Byte> myExistingIds;
    private Map<String, Long> myTrackTsUpdate;
    private Map<String, String> myTrackSourceId;
    private Map<String, Long> myPhotoTsUpdate;
    private Map<String, String> myPhotoSourceId;
    private String[] myDisabledMp4Codecs;
    private WatchfolderDatasourceConfig myDatasourceConfig;
    private Map<String, Pattern> myPatterns = new HashMap<>();
    private Set<String> myPhotoAlbumIds;
    private long processedCount;
    private long lastProcessedCountLog = System.currentTimeMillis();

    public MyTunesRssFileProcessor(WatchfolderDatasourceConfig datasourceConfig, DatabaseUpdateQueue queue, Map<String, Long> trackTsUpdate, Map<String, String> trackSourceId, Map<String, Long> photoTsUpdate, Map<String, String> photoSourceId, MVStore mvStore) throws SQLException {
        myExistingIds = MyTunesRssUtils.openMvMap(mvStore, "existingIds");
        myDatasourceConfig = datasourceConfig;
        myQueue = queue;
        myTrackTsUpdate = trackTsUpdate;
        myTrackSourceId = trackSourceId;
        myPhotoTsUpdate = photoTsUpdate;
        myPhotoSourceId = photoSourceId;
        myDisabledMp4Codecs = StringUtils.split(StringUtils.lowerCase(StringUtils.trimToEmpty(myDatasourceConfig.getDisabledMp4Codecs())), ",");
        myPhotoAlbumIds = new HashSet<>(MyTunesRss.STORE.executeQuery(new FindPhotoAlbumIdsQuery()));
    }

    public Set<String> getExistingIds() {
        return myExistingIds.keySet();
    }

    public int getUpdatedCount() {
        return myUpdatedCount;
    }

    public void process(File file) {
        try {
            if (file.isFile()) {
                String fileId = "file_" + IOUtils.getFilenameHash(file);
                if (!myExistingIds.containsKey(fileId)) {
                    boolean existingTrack = myTrackTsUpdate.containsKey(fileId);
                    boolean existingPhoto = myPhotoTsUpdate.containsKey(fileId);
                    boolean existing = existingTrack || existingPhoto;
                    if (existing) {
                        myExistingIds.put(fileId, (byte) 0);
                    }
                    long trackUpdateTs = existingTrack && myDatasourceConfig.getId().equals(myTrackSourceId.get(fileId)) ? myTrackTsUpdate.get(fileId).longValue() : 0;
                    long photoUpdateTs = existingPhoto && myDatasourceConfig.getId().equals(myPhotoSourceId.get(fileId)) ? myPhotoTsUpdate.get(fileId).longValue() : 0;
                    if (!existing || (existingTrack && trackUpdateTs < file.lastModified()) || (existingPhoto && photoUpdateTs < file.lastModified()) || (FileSupportUtils.isMp4(file) && myDisabledMp4Codecs.length > 0)) {
                        String contentType = TikaUtils.getContentType(file);
                        MediaType mediaType = MediaType.get(contentType);
                        if (mediaType != MediaType.Other) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Processing file \"" + file.getAbsolutePath() + "\".");
                            }
                            if (mediaType == MediaType.Image) {
                                insertOrUpdateImage(file, fileId, existing);
                            } else {
                                if (insertOrUpdateTrack(file, fileId, existing, contentType)) {
                                    return; // early return!!!
                                }

                            }
                        }
                    } else {
                        String contentType = TikaUtils.getContentType(file);
                        MediaType mediaType = MediaType.get(contentType);
                        if (mediaType == MediaType.Image) {
                            String albumName = getPhotoAlbum(file);
                            try {
                                final String albumId = new String(Hex.encodeHex(MessageDigest.getInstance("SHA-1").digest(albumName.getBytes("UTF-8"))));
                                myQueue.offer(new DataStoreStatementEvent(new DataStoreStatement() {
                                    public void execute(Connection connection) throws SQLException {
                                        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "touchPhotoAlbum");
                                        statement.setString("id", albumId);
                                        statement.execute();
                                    }
                                }, true));
                            } catch (NoSuchAlgorithmException e) {
                                if (LOGGER.isErrorEnabled()) {
                                    LOGGER.error("Could not create message digest.", e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (ShutdownRequestedException e) {
            throw e; // don't handle this one
        } catch (IOException | RuntimeException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not process file \"" + file.getAbsolutePath() + "\".", e);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            processedCount++;
            if (System.currentTimeMillis() - lastProcessedCountLog > 60000) {
                LOGGER.info("Processed " + processedCount + " files.");
                lastProcessedCountLog = System.currentTimeMillis();
            }
        }
    }

    private boolean insertOrUpdateTrack(File file, String fileId, boolean existingTrack, String contentType) throws IOException, InterruptedException {
        MediaType mediaType = MediaType.get(contentType);
        String canonicalFilePath = file.getCanonicalPath();
        InsertOrUpdateTrackStatement statement;
        statement = existingTrack ? new UpdateTrackStatement(TrackSource.FileSystem, myDatasourceConfig.getId()) : new InsertTrackStatement(TrackSource.FileSystem, myDatasourceConfig.getId());
        statement.clear();
        // never set any statement information here, since they are cleared once again later for MP4 files
        // if meta data from files should be ignored.
        if (!myDatasourceConfig.isIgnoreFileMeta() && FileSupportUtils.isMp3(file)) {
            parseMp3MetaData(file, statement, fileId, mediaType);
        } else if (FileSupportUtils.isMp4(file)) {
            // we have to fetch meta data even if they should be ignored to get the MP4 codec
            TrackMetaData meta = parseMp4MetaData(file, statement, fileId, mediaType);
            if (meta.getMp4Codec() != null && ArrayUtils.contains(myDisabledMp4Codecs, meta.getMp4Codec().toLowerCase())) {
                myExistingIds.remove(fileId);
                return true;
            } else if (myDatasourceConfig.isIgnoreFileMeta()) {
                statement.clear(); // meta data should be ignored
            }
        } else {
            setSimpleInfo(statement, file, mediaType);
        }
        statement.setId(fileId);
        statement.setProtected(statement.isProtected()); // TODO DRM detection
        statement.setMediaType(mediaType);
        statement.setContentType(contentType);
        statement.setFileName(canonicalFilePath);
        myQueue.offer(new DataStoreStatementEvent(statement, true));
        myUpdatedCount++;
        myExistingIds.put(fileId, (byte) 0);
        return false;
    }

    private void insertOrUpdateImage(File photoFile, final String photoFileId, boolean existingPhoto) throws IOException, InterruptedException {
        final String canonicalFilePath = photoFile.getCanonicalPath();
        InsertOrUpdatePhotoStatement statement = existingPhoto ? new UpdatePhotoStatement(myDatasourceConfig.getId()) : new InsertPhotoStatement(myDatasourceConfig.getId());
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
        } catch (ImageReadException ignored) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not read EXIF data from \"" + photoFile.getAbsolutePath() + "\".");
            }
        }
        myQueue.offer(new DataStoreStatementEvent(statement, true, "Could not insert photo \"" + canonicalFilePath + "\" into database"));
        String albumName = getPhotoAlbum(photoFile);
        try {
            final String albumId = new String(Hex.encodeHex(MessageDigest.getInstance("SHA-1").digest(albumName.getBytes("UTF-8"))));
            boolean update = myPhotoAlbumIds.contains(albumId);
            if (update) {
                myQueue.offer(new DataStoreEvent() {
                    public boolean execute(DataStoreSession session) {
                        try {
                            if (session.executeQuery(new DataStoreQuery<QueryResult<Boolean>>() {
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
                                SavePhotoAlbumStatement savePhotoAlbumStatement = new SavePhotoAlbumStatement(myDatasourceConfig.getId());
                                savePhotoAlbumStatement.setId(albumId);
                                savePhotoAlbumStatement.setUpdate(true);
                                savePhotoAlbumStatement.setAdd(true);
                                savePhotoAlbumStatement.setPhotoIds(Collections.singletonList(photoFileId));
                                session.executeStatement(savePhotoAlbumStatement);
                            }
                        } catch (SQLException e) {
                            LOGGER.warn("Could not insert photo \"" + canonicalFilePath + "\" into database", e);
                        }
                        return true;
                    }

                    @Override
                    public boolean isCheckpointRelevant() {
                        return true;
                    }
                });
            } else {
                SavePhotoAlbumStatement savePhotoAlbumStatement = new SavePhotoAlbumStatement(myDatasourceConfig.getId());
                savePhotoAlbumStatement.setId(albumId);
                savePhotoAlbumStatement.setName(albumName);
                savePhotoAlbumStatement.setPhotoIds(Collections.singletonList(photoFileId));
                myQueue.offer(new DataStoreStatementEvent(savePhotoAlbumStatement, true, "Could not insert photo \"" + canonicalFilePath + "\" into database"));
                myPhotoAlbumIds.add(albumId);
            }
        } catch (NoSuchAlgorithmException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not create message digest.", e);
            }
        }
        myUpdatedCount++;
        myExistingIds.put(photoFileId, (byte) 0);
    }

    private TrackMetaData parseMp3MetaData(File file, InsertOrUpdateTrackStatement statement, String fileId, MediaType mediaType) {
        TrackMetaData meta = new TrackMetaData();
        Id3Tag tag = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading ID3 information from file \"" + file.getAbsolutePath() + "\".");
            }
            tag = Mp3Utils.readId3Tag(file);
        } catch (IOException | IllegalHeaderException | RuntimeException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        int timeSeconds = MyTunesRssMp3Utils.calculateTimeFromMp3AudioFrames(file);
        if (tag == null) {
            setSimpleInfo(statement, file, mediaType);
        } else {
            try {
                String album = tag.getAlbum();
                if (StringUtils.isEmpty(album)) {
                    album = getFallbackAlbumName(file);
                }
                statement.setAlbum(album);
                String artist = tag.getArtist();
                if (StringUtils.isEmpty(artist)) {
                    artist = getFallbackArtistName(file);
                }
                statement.setArtist(artist);
                String name = tag.getTitle();
                if (StringUtils.isEmpty(name)) {
                    name = StringUtils.defaultIfBlank(getFallbackTitleName(file), FilenameUtils.getBaseName(file.getName()));
                }
                String yearString = StringUtils.defaultIfEmpty(tag.getYear(), "-1");
                try {
                    statement.setYear(Integer.parseInt(yearString));
                } catch (NumberFormatException ignored) {
                    LOGGER.warn("Illegal YEAR value \"" + yearString + "\" in \"" + file + "\".");
                    statement.setYear(-1);
                }
                statement.setName(name);
                String albumArtist = artist;
                if (tag.isId3v2()) {
                    Id3v2Tag id3v2Tag = ((Id3v2Tag) tag);
                    albumArtist = id3v2Tag.getAlbumArtist();
                    if (StringUtils.isEmpty(albumArtist)) {
                        albumArtist = artist;
                    }
                    String composer = id3v2Tag.getComposer();
                    statement.setComposer(composer);
                    if (timeSeconds <= 0) {
                        LOGGER.debug("Could not calculate MP3 duration from audio frames, trying length ID3 tag.");
                        timeSeconds = id3v2Tag.getTimeSeconds();
                    }
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
                    } catch (NumberFormatException ignored) {
                        LOGGER.warn("Illegal TPA/TPOS value \"" + pos + "\" in \"" + file + "\".");
                    }
                    statement.setSortAlbum(StringUtils.trimToNull(id3v2Tag.getSortAlbum()));
                    statement.setSortAlbumArtist(StringUtils.trimToNull(id3v2Tag.getSortAlbumArtist()));
                }
                statement.setAlbumArtist(albumArtist);
                String genre = tag.getGenreAsString();
                if (genre != null) {
                    statement.setGenre(StringUtils.trimToNull(genre));
                }
                statement.setComment(StringUtils.trimToNull(createComment(tag)));
            } catch (RuntimeException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
                }
                statement.clear();
                statement.setId(fileId);
                setSimpleInfo(statement, file, mediaType);
            }
            statement.setTime(timeSeconds);
        }
        return meta;
    }

    private String createComment(Id3Tag tag) {
        try {
            if (tag.isId3v2()) {
                String comment = " " + myDatasourceConfig.getId3v2TrackComment() + " "; // make sure the comment does neither start nor end with a token
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
        } catch (RuntimeException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not create comment for ID3 tag", e);
            }
        }
        return null;
    }

    private TrackMetaData parseMp4MetaData(File file, InsertOrUpdateTrackStatement statement, String fileId, MediaType mediaType) {
        TrackMetaData meta = new TrackMetaData();
        MoovAtom moov = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading ATOM information from file \"" + file.getAbsolutePath() + "\".");
            }
            moov = (MoovAtom) MyTunesRss.MP4_PARSER.parseAndGet(file, "moov");
        } catch (IOException | RuntimeException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get ATOM information from file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        if (moov == null) {
            setSimpleInfo(statement, file, mediaType);
        } else {
            try {
                String name = moov.getTitle();
                if (StringUtils.isBlank(name)) {
                    name = StringUtils.defaultIfBlank(getFallbackTitleName(file), FilenameUtils.getBaseName(file.getName()));
                }
                statement.setName(name);
                meta.setMp4Codec(moov.getCodec());
                statement.setMp4Codec(moov.getCodec());
                String genre = moov.getGenre();
                if (StringUtils.isNotBlank(genre)) {
                    statement.setGenre(genre);
                }
                String comment = moov.getComment();
                if (StringUtils.isNotBlank(comment)) {
                    statement.setComment(comment);
                }
                Integer year = moov.getYear();
                if (year != null) {
                    statement.setYear(year);
                }
                statement.setTime((int) moov.getDurationSeconds());
                if (mediaType == MediaType.Audio) {
                    String album = moov.getAlbum();
                    if (StringUtils.isBlank(album)) {
                        album = getFallbackAlbumName(file);
                    }
                    statement.setAlbum(album);
                    String artist = moov.getArtist();
                    String albumArtist = moov.getAlbumArtist();
                    if (StringUtils.isBlank(artist)) {
                        if (StringUtils.isBlank(albumArtist)) {
                            artist = getFallbackArtistName(file);
                        } else {
                            artist = albumArtist;
                        }
                    }
                    statement.setArtist(artist);
                    if (StringUtils.isBlank(albumArtist)) {
                        albumArtist = artist;
                    }
                    statement.setAlbumArtist(albumArtist);
                    statement.setCompilation(moov.isCompilation() || !StringUtils.equalsIgnoreCase(artist, albumArtist));
                    String composer = moov.getComposer();
                    statement.setComposer(composer);
                    Long trackNumer = moov.getTrackNumber();
                    if (trackNumer != null) {
                        statement.setTrackNumber(trackNumer.intValue());
                    }
                    DiskAtom diskAtom = moov.getDiskAtom();
                    if (diskAtom != null) {
                        statement.setPos(diskAtom.getNumber(), diskAtom.getSize());
                    }
                    statement.setSortAlbum(StringUtils.trimToNull(moov.getSortAlbum()));
                    statement.setSortAlbumArtist(StringUtils.trimToNull(moov.getSortAlbumArtist()));
                } else if (mediaType == MediaType.Video) {
                    VideoType videoType = myDatasourceConfig.getVideoType();
                    if (moov.getMediaType() == StikAtom.Type.TvShow) {
                        videoType = VideoType.TvShow;
                    } else if (moov.getMediaType() == StikAtom.Type.Movie) {
                        videoType = VideoType.Movie;
                    }
                    statement.setVideoType(videoType);
                    if (videoType == VideoType.TvShow) {
                        String tvShow = moov.getTvShow();
                        if (StringUtils.isNotBlank(tvShow)) {
                            statement.setSeries(tvShow);
                        } else {
                            statement.setSeries(getFallbackSeries(file));
                        }
                        Long season = moov.getTvSeason();
                        if (season != null) {
                            statement.setSeason(season.intValue());
                        } else {
                            statement.setSeason(getFallbackSeason(file));
                        }
                        Long episode = moov.getTvEpisode();
                        if (episode != null) {
                            statement.setEpisode(episode.intValue());
                        } else {
                            statement.setEpisode(getFallbackEpisode(file));
                        }
                    }
                }
                statement.setProtected(moov.isDrmProtected());
            } catch (RuntimeException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
                }
                statement.clear();
                statement.setId(fileId);
                setSimpleInfo(statement, file, mediaType);
            }
            CoverAtom coverAtom = moov.getCoverAtom();
            if (coverAtom != null) {
                try {
                    meta.setImage(new Image(coverAtom.getMimeType(), coverAtom.getDataStream()));
                } catch (IOException e) {
                    LOGGER.warn("Could not extract image from MP4 file.", e);
                }
            }
        }
        return meta;
    }

    private void setSimpleInfo(InsertOrUpdateTrackStatement statement, File file, MediaType mediaType) {
        statement.setName(StringUtils.defaultIfBlank(getFallbackTitleName(file), FilenameUtils.getBaseName(file.getName())));
        if (mediaType == MediaType.Audio) {
            statement.setAlbum(getFallbackAlbumName(file));
            statement.setArtist(getFallbackArtistName(file));
            statement.setAlbumArtist(getFallbackArtistName(file)); // TODO fallback album artist
        }
        if (mediaType == MediaType.Video) {
            statement.setVideoType(myDatasourceConfig.getVideoType());
            if (myDatasourceConfig.getVideoType() == VideoType.TvShow) {
                statement.setSeries(getFallbackSeries(file));
                statement.setSeason(getFallbackSeason(file));
                statement.setEpisode(getFallbackEpisode(file));
            }
        }
    }

    private String getFallbackTitleName(File file) {
        return myDatasourceConfig.getTitleFallback() != null ? getFallbackName(file, myDatasourceConfig.getTitleFallback()) : null;
    }

    private String getFallbackAlbumName(File file) {
        return myDatasourceConfig.getAlbumFallback() != null ? getFallbackName(file, myDatasourceConfig.getAlbumFallback()) : null;
    }

    private String getFallbackArtistName(File file) {
        return myDatasourceConfig.getArtistFallback() != null ? getFallbackName(file, myDatasourceConfig.getArtistFallback()) : null;
    }


    private String getFallbackSeries(File file) {
        return myDatasourceConfig.getSeriesFallback() != null ? getFallbackName(file, myDatasourceConfig.getSeriesFallback()) : null;
    }

    private int getFallbackSeason(File file) {
        if (myDatasourceConfig.getSeasonFallback() != null) {
            String fallback = StringUtils.trimToNull(getFallbackName(file, myDatasourceConfig.getSeasonFallback()));
            return StringUtils.isNumeric(fallback) ? Integer.parseInt(fallback) : 0;
        } else {
            return 0;
        }
    }

    private int getFallbackEpisode(File file) {
        if (myDatasourceConfig.getEpisodeFallback() != null) {
            String fallback = StringUtils.trimToNull(getFallbackName(file, myDatasourceConfig.getEpisodeFallback()));
            return StringUtils.isNumeric(fallback) ? Integer.parseInt(fallback) : 0;
        } else {
            return 0;
        }
    }

    private String getPhotoAlbum(File file) {
        return myDatasourceConfig.getPhotoAlbumPattern() != null ? getFallbackName(file, myDatasourceConfig.getPhotoAlbumPattern()) : null;
    }

    String getFallbackName(File file, String pattern) {
        try {
            String name = pattern;
            String[] dirTokens = MyTunesRssUtils.substringsBetween(pattern, "[[[dir:", "]]]");
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
                                matcher.reset();
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
            String[] fileTokens = MyTunesRssUtils.substringsBetween(pattern, "[[[file", "]]]");
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
                        matcher.reset();
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
        } catch (RuntimeException ignored) {
            LOGGER.warn("Could not create fallback name for \"" + file + "\" with pattern \"" + pattern + "\".");
        }
        return null;
    }
}
