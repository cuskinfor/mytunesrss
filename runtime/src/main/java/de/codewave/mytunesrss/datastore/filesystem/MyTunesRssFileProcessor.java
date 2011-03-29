package de.codewave.mytunesrss.datastore.filesystem;

import com.sun.imageio.plugins.jpeg.JPEGMetadata;
import de.codewave.camel.mp3.Id3Tag;
import de.codewave.camel.mp3.Id3v1Tag;
import de.codewave.camel.mp3.Id3v2Tag;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.camel.mp4.Mp4Utils;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.TrackMetaData;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.utils.io.FileProcessor;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * de.codewave.mytunesrss.datastore.filesystem.MyTunesRssFileProcessor
 */
public class MyTunesRssFileProcessor implements FileProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssFileProcessor.class);
    private static final String ATOM_ALBUM = "moov.udta.meta.ilst.\u00a9alb.data";
    private static final String ATOM_ARTIST = "moov.udta.meta.ilst.\u00a9ART.data";
    private static final String ATOM_TITLE = "moov.udta.meta.ilst.\u00a9nam.data";
    private static final String ATOM_TRACK_NUMBER = "moov.udta.meta.ilst.trkn.data";
    private static final String ATOM_DISK_NUMBER = "moov.udta.meta.ilst.disk.data";
    private static final String ATOM_GENRE = "moov.udta.meta.ilst.\u00a9gen.data";
    private static final String ATOM_STSD = "moov.trak.mdia.minf.stbl.stsd";
    private static final String ATOM_COVER = "moov.udta.meta.ilst.covr.data";

    private long myLastUpdateTime;
    private DataStoreSession myStoreSession;
    private int myUpdatedCount;
    private Set<String> myExistingIds = new HashSet<String>();
    private Collection<String> myTrackIds;
    private String[] myDisabledMp4Codecs;
    private WatchfolderDatasourceConfig myDatasourceConfig;
    private Map<String, Pattern> myPatterns = new HashMap<String, Pattern>();

    public MyTunesRssFileProcessor(WatchfolderDatasourceConfig datasourceConfig, DataStoreSession storeSession, long lastUpdateTime, Collection<String> trackIds) {
        myDatasourceConfig = datasourceConfig;
        myStoreSession = storeSession;
        myLastUpdateTime = lastUpdateTime;
        myTrackIds = trackIds;
        myDisabledMp4Codecs = StringUtils.split(StringUtils.lowerCase(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getDisabledMp4Codecs())), ",");
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
                    if ((file.lastModified() >= myLastUpdateTime || !existing || (FileSupportUtils.isMp4(file) && myDisabledMp4Codecs.length > 0))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Processing file \"" + file.getAbsolutePath() + "\".");
                        }
                        InsertOrUpdateTrackStatement statement;
                        if (!MyTunesRss.CONFIG.isIgnoreArtwork()) {
                            statement = existing ? new UpdateTrackAndImageStatement(TrackSource.FileSystem) : new InsertTrackAndImageStatement(TrackSource.FileSystem);
                        } else {
                            statement = existing ? new UpdateTrackStatement(TrackSource.FileSystem) : new InsertTrackStatement(TrackSource.FileSystem);
                        }
                        statement.clear();
                        statement.setId(fileId);
                        TrackMetaData meta = null;
                        FileType type = MyTunesRss.CONFIG.getFileType(FileSupportUtils.getFileSuffix(file.getName()));
                        if (FileSupportUtils.isMp3(file)) {
                            meta = parseMp3MetaData(file, statement, fileId, type.getMediaType());
                        } else if (FileSupportUtils.isMp4(file)) {
                            meta = parseMp4MetaData(file, statement, fileId, type.getMediaType());
                            if (meta.getMp4Codec() != null && ArrayUtils.contains(myDisabledMp4Codecs, meta.getMp4Codec().toLowerCase())) {
                                myExistingIds.remove(fileId);
                                DatabaseBuilderCallable.doCheckpoint(myStoreSession, false);
                                return; // early return!!!
                            }
                        } else {
                            setSimpleInfo(statement, file, type.getMediaType());
                        }
                        statement.setProtected(type.isProtected());
                        statement.setMediaType(type.getMediaType());
                        statement.setFileName(canonicalFilePath);
                        if (type.getMediaType() == MediaType.Video) {
                            statement.setAlbum(null);
                            statement.setArtist(null);
                            statement.setVideoType(myDatasourceConfig.getVideoType());
                            if (myDatasourceConfig.getVideoType() == VideoType.TvShow) {
                                statement.setSeason(getFallbackSeason(file)); // TODO: try meta info first
                                statement.setSeries(getFallbackSeries(file)); // TODO: try meta info first
                                statement.setEpisode(getFallbackEpisode(file)); // TODO: try meta info first
                            }
                        } else if (type.getMediaType() == MediaType.Image) {
                            statement.setAlbum(null);
                            statement.setArtist(null);
                            statement.setPhotoAlbum(getPhotoAlbum(file));
                            try {
                                IImageMetadata imageMeta = Sanselan.getMetadata(file);
                                if (imageMeta instanceof JpegImageMetadata) {
                                    JpegImageMetadata jpegMeta = (JpegImageMetadata) imageMeta;
                                    TiffField exifCreateDateTiffField = jpegMeta.findEXIFValue(TiffConstants.EXIF_TAG_CREATE_DATE);
                                    if (exifCreateDateTiffField != null) {
                                        String value = (String)exifCreateDateTiffField.getValue();
                                        if (StringUtils.isNotBlank(value) && LOGGER.isDebugEnabled()) {
                                            LOGGER.debug("EXIF create date for \"" + file.getAbsolutePath() + "\" is \"" + value + "\".");
                                        }
                                    }
                                }
                            } catch (ImageReadException e) {
                                if (LOGGER.isWarnEnabled()) {
                                    LOGGER.warn("Could not read EXIF data from \"" + file.getAbsolutePath() + "\".");
                                }

                            }
                        }
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
                String yearString;
                if (tag.isId3v1()) {
                    yearString = StringUtils.defaultIfEmpty(((Id3v1Tag) tag).getYear(), "-1");
                } else {
                    yearString = StringUtils.defaultIfEmpty(((Id3v2Tag) tag).getFrameBodyToString("TYE", "TYER"), "-1");
                }
                try {
                    statement.setYear(Integer.parseInt(yearString));
                } catch (NumberFormatException e) {
                    LOGGER.warn("Illegal YEAR value \"" + yearString + "\" in \"" + file + "\".");
                    statement.setYear(-1);
                }
                statement.setName(MyTunesRssUtils.normalize(name));
                if (tag.isId3v2()) {
                    Id3v2Tag id3v2Tag = ((Id3v2Tag) tag);
                    statement.setTime(id3v2Tag.getTimeSeconds());
                    statement.setTrackNumber(id3v2Tag.getTrackNumber());
                    meta.setImage(MyTunesRssMp3Utils.getImage(id3v2Tag));
                    String pos = id3v2Tag.getFrameBodyToString("TPA", "TPOS");
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
        Map<String, Mp4Atom> atoms = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading ATOM information from file \"" + file.getAbsolutePath() + "\".");
            }
            atoms = Mp4Utils.getAtoms(file, Arrays.asList(ATOM_ALBUM, ATOM_ARTIST, ATOM_TITLE, ATOM_TRACK_NUMBER, ATOM_DISK_NUMBER, ATOM_GENRE, ATOM_STSD, ATOM_COVER));
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get ATOM information from file \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        if (atoms == null || atoms.isEmpty()) {
            setSimpleInfo(statement, file, mediaType);
        } else {
            try {
                Mp4Atom atom = atoms.get(ATOM_ALBUM);
                String album = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isEmpty(album)) {
                    album = getFallbackAlbumName(file);
                }
                statement.setAlbum(MyTunesRssUtils.normalize(album));
                atom = atoms.get(ATOM_ARTIST);
                String artist = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isEmpty(artist)) {
                    artist = getFallbackArtistName(file);
                }
                statement.setArtist(MyTunesRssUtils.normalize(artist));
                atom = atoms.get(ATOM_TITLE);
                String name = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (StringUtils.isEmpty(name)) {
                    name = FilenameUtils.getBaseName(file.getName());
                }
                statement.setName(MyTunesRssUtils.normalize(name));
                //statement.setTime(atoms.get(ATOM_TIME).getData()[11]);
                atom = atoms.get(ATOM_TRACK_NUMBER);
                if (atom != null) {
                    statement.setTrackNumber(atom.getData()[11]);
                }
                atom = atoms.get(ATOM_DISK_NUMBER);
                if (atom != null) {
                    statement.setPos(atom.getData()[11], atom.getData()[13]);
                }
                atom = atoms.get(ATOM_STSD);
                if (atom != null) {
                    String mp4Codec = atom.getDataAsString(12, 4, "UTF-8");
                    meta.setMp4Codec(mp4Codec);
                    statement.setMp4Codec(mp4Codec);
                }
                atom = atoms.get(ATOM_GENRE);
                String genre = atom != null ? atom.getDataAsString(8, "UTF-8") : null;
                if (genre != null) {
                    statement.setGenre(StringUtils.trimToNull(genre));
                }
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse ID3 information from file \"" + file.getAbsolutePath() + "\".", e);
                }
                statement.clear();
                statement.setId(fileId);
                setSimpleInfo(statement, file, mediaType);
            }
        }
        Mp4Atom atom = atoms.get(ATOM_COVER);
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