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
    private Image myImage;
    private TrackSource mySource;
    private String mySourceId;

    public HandleTrackImagesStatement(TrackSource source, String sourceId, File file, String trackId) throws IOException {
        myFile = file;
        myTrackId = trackId;
        mySource = source;
        mySourceId = sourceId;
        myImage = getLocalFileImage();
    }

    public void execute(Connection connection) throws SQLException {
        String imageHash = "";
        try {
            if (myImage != null && myImage.getData() != null && myImage.getData().length > 0) {
                imageHash = MyTunesRssBase64Utils.encode(MyTunesRss.MD5_DIGEST.get().digest(myImage.getData()));
                List<Integer> imageSizes = new GetImageSizesQuery(imageHash).execute(connection).getResults();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Image with hash \"" + imageHash + "\" has " + imageSizes.size() + " entries in database.");
                }
                Image image32 = MyTunesRssUtils.resizeImageWithMaxSize(myImage, 32);
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
                Image image64 = MyTunesRssUtils.resizeImageWithMaxSize(myImage, 64);
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
                Image image256 = MyTunesRssUtils.resizeImageWithMaxSize(myImage, 256);
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
                int originalSize = MyTunesRssUtils.getMaxImageSize(myImage);
                if (originalSize > 256) {
                    if (!imageSizes.contains(Integer.valueOf(originalSize))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Inserting image with size " + originalSize + ".");
                        }
                        new InsertImageStatement(imageHash, originalSize, myImage.getMimeType(), myImage.getData()).execute(connection);
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Updating image with size " + originalSize + ".");
                        }
                        new UpdateImageStatement(imageHash, originalSize, myImage.getMimeType(), myImage.getData()).execute(connection);
                    }
                }
                Image image128 = MyTunesRssUtils.resizeImageWithMaxSize(myImage, 128);
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
            }
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not extract image from file \"" + myFile.getAbsolutePath() + "\".", e);
            }
        } finally {
            new UpdateImageForTrackStatement(myTrackId, imageHash).execute(connection);
        }
    }

    private Image getLocalFileImage() throws IOException {
        Image image = null;
        // look for special image file
        File imageFile = findImageFile(myFile);
        if (imageFile != null) {
            // okay, use special image file
            return readImageFromImageFile(imageFile);
        } else {
            if (mySource == TrackSource.ITunes) {
                // prefer itunes artwork for performance reasons
                image = findItunesArtwork();
            }
            if (image == null) {
                // no itunes artwork or no itunes data source
                image = readImageFromTrackFile(image);
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
        Image image;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading image information from file \"" + imageFile.getAbsolutePath() + "\".");
        }
        image = new Image(IMAGE_TO_MIME.get(FilenameUtils.getExtension(imageFile.getName()).toLowerCase()), FileUtils.readFileToByteArray(
                imageFile));
        return image;
    }

    private File findImageFile(File file) {
        for (String suffix : IMAGE_TO_MIME.keySet()) {
            File imageFile;
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase(suffix)) {
                imageFile = file;
            } else {
                String basePath = file.getAbsolutePath();
                for (ReplacementRule rule : ((AudioVideoDatasourceConfig)MyTunesRss.CONFIG.getDatasource(mySourceId)).getTrackImageMappings()) {
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
                    LOGGER.debug("Trying directory \"" + file.getParentFile().getAbsolutePath() + "/Album Artwork\".");
                    String[] idPair = StringUtils.split(myTrackId, "_");
                    if (idPair.length == 2) {
                        String dirLevel1 = StringUtils.leftPad("" + Long.parseLong("" + idPair[1].charAt(idPair[1].length() - 1), 16), 2, '0');
                        String dirLevel2 = StringUtils.leftPad("" + Long.parseLong("" + idPair[1].charAt(idPair[1].length() - 2), 16), 2, '0');
                        String dirLevel3 = StringUtils.leftPad("" + Long.parseLong("" + idPair[1].charAt(idPair[1].length() - 3), 16), 2, '0');
                        File itcFile = null;
                        File albumArtworkDir = new File(file.getParentFile(), "Album Artwork");
                        if (albumArtworkDir.isDirectory()) {
                            File[] files = albumArtworkDir.listFiles();
                            if (files != null) {
                                for (File subdir : files) {
                                    itcFile = new File(subdir, idPair[0] + "/" + dirLevel1 + "/" + dirLevel2 + "/" + dirLevel3 + "/" + idPair[0] + "-" + idPair[1] + ".itc");
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