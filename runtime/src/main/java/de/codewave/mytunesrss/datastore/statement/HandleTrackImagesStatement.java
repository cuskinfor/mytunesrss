package de.codewave.mytunesrss.datastore.statement;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.config.*;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.MyTunesRssMp4Utils;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class HandleTrackImagesStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandleTrackImagesStatement.class);
    private static Map<String, String> IMAGE_TO_MIME = new HashMap<String, String>();

    static {
        IMAGE_TO_MIME.put("jpg", "image/jpeg");
        IMAGE_TO_MIME.put("gif", "image/gif");
        IMAGE_TO_MIME.put("png", "image/png");
    }

    private File myFile;
    private String myTrackId;
    private TrackSource mySource;
    private String mySourceId;

    public HandleTrackImagesStatement(TrackSource source, String sourceId, File file, String trackId) {
        myFile = file;
        myTrackId = trackId;
        mySource = source;
        mySourceId = sourceId;
    }

    public void execute(Connection connection) {
        String imageHash = "";
        Image image = null;
        try {
            image = getLocalFileImage();
            if (image != null && image.getData() != null && image.getData().length > 0) {
                imageHash = MyTunesRssBase64Utils.encode(MyTunesRss.MD5_DIGEST.get().digest(image.getData()));
                List<Integer> imageSizes = new GetImageSizesQuery(imageHash).execute(connection).getResults();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Image with hash \"" + imageHash + "\" has " + imageSizes.size() + " entries in database.");
                }
                Image image32 = MyTunesRssUtils.resizeImageWithMaxSize(image, 32, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                if (!imageSizes.contains(Integer.valueOf(32))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 32.");
                    }
                    new InsertImageStatement(imageHash, 32, image32.getMimeType(), image32.getData()).execute(connection);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Updating image with size 32.");
                    }
                    new UpdateImageStatement(imageHash, 32, image32.getMimeType(), image32.getData()).execute(connection);
                }
                Image image64 = MyTunesRssUtils.resizeImageWithMaxSize(image, 64, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                if (!imageSizes.contains(Integer.valueOf(64))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 64.");
                    }
                    new InsertImageStatement(imageHash, 64, image64.getMimeType(), image64.getData()).execute(connection);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Updating image with size 64.");
                    }
                    new UpdateImageStatement(imageHash, 64, image64.getMimeType(), image64.getData()).execute(connection);
                }
                Image image128 = MyTunesRssUtils.resizeImageWithMaxSize(image, 128, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                if (!imageSizes.contains(Integer.valueOf(128))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 128.");
                    }
                    new InsertImageStatement(imageHash, 128, image128.getMimeType(), image128.getData()).execute(connection);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Updating image with size 128.");
                    }
                    new UpdateImageStatement(imageHash, 128, image128.getMimeType(), image128.getData()).execute(connection);
                }
                Image image256 = MyTunesRssUtils.resizeImageWithMaxSize(image, 256, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                if (!imageSizes.contains(Integer.valueOf(256))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 256.");
                    }
                    new InsertImageStatement(imageHash, 256, image256.getMimeType(), image256.getData()).execute(connection);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Updating image with size 256.");
                    }
                    new UpdateImageStatement(imageHash, 256, image256.getMimeType(), image256.getData()).execute(connection);
                }
                int originalSize = MyTunesRssUtils.getMaxImageSize(image);
                if (originalSize > 256) {
                    Image imageMax = image;
                    int maxSize = originalSize;
                    if (originalSize > 2048) {
                        // upper limit for image size is 2048
                        imageMax = MyTunesRssUtils.resizeImageWithMaxSize(image, 2048, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                        maxSize = 2048;
                    }
                    if (!imageSizes.contains(Integer.valueOf(maxSize))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Inserting image with size " + maxSize + ".");
                        }
                        new InsertImageStatement(imageHash, maxSize, imageMax.getMimeType(), imageMax.getData()).execute(connection);
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Updating image with size " + maxSize + ".");
                        }
                        new UpdateImageStatement(imageHash, maxSize, imageMax.getMimeType(), imageMax.getData()).execute(connection);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not extract or resize image from file \"" + myFile.getAbsolutePath() + "\".", e);
        } catch (SQLException e) {
            LOGGER.error("Could not get existing image sizes or insert new image for \"" + myFile.getAbsolutePath() + "\".", e);
        } catch (RuntimeException e) {
            LOGGER.warn("Unknown problem handling images for \"" + myFile.getAbsolutePath() + "\".", e);
        } finally {
            if (image != null) {
                image.deleteImageFile();
            }
            try {
                new UpdateImageForTrackStatement(myTrackId, imageHash).execute(connection);
            } catch (SQLException e) {
                LOGGER.error("Could not mark track \"" + myTrackId + "\". for having no image.", e);
            }
        }
    }
    
    private Image getLocalFileImage() throws IOException {
        Image image = null;
        // look for special image file
        File imageFile = findImageFile(myFile);
        if (imageFile != null) {
            // okay, use special image file
            image = readImageFromImageFile(imageFile);
            if (!MyTunesRssUtils.isImageUsable(image)) {
                LOGGER.debug("Image from special file not readable.");
                // image not readable, try next
                image = null;
            }
        }
        if (mySource == TrackSource.ITunes && image == null) {
            // prefer itunes artwork for performance reasons
            image = findItunesArtwork();
            if (image != null && !MyTunesRssUtils.isImageUsable(image)) {
                LOGGER.debug("Itunes artwork not readable.");
                // image not readable, try next
                image = null;
            }
        }
        if (image == null) {
            // no itunes artwork or no itunes data source
            image = readImageFromTrackFile(image);
            if (image != null && !MyTunesRssUtils.isImageUsable(image)) {
                LOGGER.debug("Image from track file not readable.");
                // image not readable, we don't have one
                image = null;
            }
        }
        return image;
    }

    private Image readImageFromTrackFile(Image image) {
        if (myFile.isFile() && FileSupportUtils.isMp3(myFile)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
            }
            image = MyTunesRssMp3Utils.getImage(myFile);
        } else if (myFile.isFile() && FileSupportUtils.isMp4(myFile)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
            }
            image = MyTunesRssMp4Utils.getImage(myFile);
        }
        return image;
    }

    private Image readImageFromImageFile(File imageFile) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading image information from file \"" + imageFile.getAbsolutePath() + "\".");
        }
        return new Image(IMAGE_TO_MIME.get(FilenameUtils.getExtension(imageFile.getName()).toLowerCase()), FileUtils.readFileToByteArray(imageFile));
    }

    private File findImageFile(File file) {
        for (String suffix : IMAGE_TO_MIME.keySet()) {
            File imageFile;
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase(suffix)) {
                imageFile = file;
            } else {
                String basePath = file.getAbsolutePath();
                for (ReplacementRule rule : ((CommonTrackDatasourceConfig)MyTunesRss.CONFIG.getDatasource(mySourceId)).getTrackImageMappings()) {
                    CompiledReplacementRule compiledReplacementRule = new CompiledReplacementRule(rule);
                    if (compiledReplacementRule.matches(basePath)) {
                        basePath = compiledReplacementRule.replace(basePath);
                        break;
                    }
                }
                imageFile = new File(basePath + "." + suffix);
            }
            if (imageFile.isFile()) {
                return imageFile;
            }
        }
        return null;
    }

    private Image findItunesArtwork() throws IOException {
        LOGGER.debug("Looking for iTunes cover for track \"" + myTrackId + "\".");
        for (DatasourceConfig datasource : MyTunesRss.CONFIG.getDatasources()) {
            if (datasource.getType() == DatasourceType.Itunes) {
                File file = new File(datasource.getDefinition());
                if (file.isFile()) {
                    // assume itunes xml
                    String[] idPair = StringUtils.split(myTrackId, "_");
                    if (idPair.length == 2) {
                        String dirLevel1 = StringUtils.leftPad("" + Long.parseLong("" + idPair[1].charAt(idPair[1].length() - 1), 16), 2, '0');
                        String dirLevel2 = StringUtils.leftPad("" + Long.parseLong("" + idPair[1].charAt(idPair[1].length() - 2), 16), 2, '0');
                        String dirLevel3 = StringUtils.leftPad("" + Long.parseLong("" + idPair[1].charAt(idPair[1].length() - 3), 16), 2, '0');
                        File itcFile = null;
                        File albumArtworkDir = new File(file.getParentFile(), "Album Artwork");
                        LOGGER.debug("Trying album artwork base directory \"" + albumArtworkDir.getAbsolutePath() + "\".");
                        if (albumArtworkDir.isDirectory()) {
                            LOGGER.debug("Listing subdirectories for album artwork base directory \"" + albumArtworkDir.getAbsolutePath() + "\"..");
                            File[] files = albumArtworkDir.listFiles();
                            if (files != null) {
                                for (File subdir : files) {
                                    LOGGER.debug("Trying directory \"" + subdir.getAbsolutePath() + "\".");
                                    itcFile = new File(subdir, idPair[0] + "/" + dirLevel1 + "/" + dirLevel2 + "/" + dirLevel3 + "/" + idPair[0] + "-" + idPair[1] + ".itc");
                                    LOGGER.debug("Looking for ITC file \"" +itcFile.getAbsolutePath() + "\".");
                                    if (itcFile.isFile()) {
                                        break;
                                    }
                                    itcFile = new File(subdir, idPair[0] + "/" + dirLevel1 + "/" + dirLevel2 + "/" + dirLevel3 + "/" + idPair[0] + "-" + idPair[1] + ".itc2");
                                    LOGGER.debug("Looking for ITC2 file \"" +itcFile.getAbsolutePath() + "\".");
                                    if (itcFile.isFile()) {
                                        break;
                                    }
                                }
                            }
                            if (itcFile.isFile()) {
                                LOGGER.debug("Reading atoms from ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                Mp4Atom itemAtom = MyTunesRss.MP4_PARSER.parseAndGet(itcFile, "item");
                                if (itemAtom != null) {
                                    LOGGER.debug("Found item atom in ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                    int offset = CamelUtils.getIntValue(itemAtom.getData(), 0, 4, false, Endianness.Big);
                                    Iterator<ImageReader> iter = ImageIO.getImageReaders(new MemoryCacheImageInputStream(new ByteArrayInputStream(itemAtom.getData(), offset - 8, itemAtom.getData().length - (offset - 8))));
                                    if (iter.hasNext()) {
                                        ImageReader reader = iter.next();
                                        String mimeType = reader.getOriginatingProvider().getMIMETypes()[0];
                                        LOGGER.debug("Extracting image of type \"" + mimeType + "\" from ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                        return new Image(mimeType, ArrayUtils.subarray(itemAtom.getData(), offset - 8, itemAtom.getData().length - (offset - 8)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
