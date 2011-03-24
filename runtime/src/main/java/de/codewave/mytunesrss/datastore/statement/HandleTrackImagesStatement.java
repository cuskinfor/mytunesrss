package de.codewave.mytunesrss.datastore.statement;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.camel.mp4.Mp4Utils;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.MyTunesRssMp4Utils;
import de.codewave.utils.graphics.ImageUtils;
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
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class HandleTrackImagesStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandleTrackImagesStatement.class);
    private static Map<String, String> IMAGE_TO_MIME = new HashMap<String, String>();
    private static final Image IMAGE_UP_TO_DATE = new Image(null, (byte[]) null);

    static {
        IMAGE_TO_MIME.put("jpg", "image/jpeg");
        IMAGE_TO_MIME.put("gif", "image/gif");
        IMAGE_TO_MIME.put("png", "image/png");
    }

    private long myLastUpdateTime;
    private File myFile;
    private String myTrackId;
    private Image myImage;
    private TrackSource mySource;
    private boolean myPhoto;

    public HandleTrackImagesStatement(TrackSource source, File file, String trackId, long lastUpdateTime, boolean photo) {
        myLastUpdateTime = lastUpdateTime;
        myFile = file;
        myTrackId = trackId;
        mySource = source;
        myPhoto = photo;
    }

    public HandleTrackImagesStatement(File file, String trackId, Image image, long lastUpdateTime) {
        myLastUpdateTime = lastUpdateTime;
        myFile = file;
        myTrackId = trackId;
        myImage = image;
    }

    public void execute(Connection connection) throws SQLException {
        try {
            Image image = getImage();
            if (image != IMAGE_UP_TO_DATE && image != null && image.getData() != null && image.getData().length > 0) {
                String imageHash = MyTunesRssBase64Utils.encode(MyTunesRss.MD5_DIGEST.digest(image.getData()));
                List<Integer> imageSizes = new GetImageSizesQuery(imageHash).execute(connection).getResults();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Image with hash \"" + imageHash + "\" has " + imageSizes.size() + " entries in database.");
                }
                if (!myPhoto && !imageSizes.contains(Integer.valueOf(32))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 32.");
                    }
                    new InsertImageStatement(imageHash, 32, ImageUtils.resizeImageWithMaxSize(image.getData(), 32)).execute(connection);
                }
                if (!myPhoto && !imageSizes.contains(Integer.valueOf(64))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 64.");
                    }
                    new InsertImageStatement(imageHash, 64, ImageUtils.resizeImageWithMaxSize(image.getData(), 64)).execute(connection);
                }
                if (!imageSizes.contains(Integer.valueOf(128))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 128.");
                    }
                    new InsertImageStatement(imageHash, 128, ImageUtils.resizeImageWithMaxSize(image.getData(), 128)).execute(connection);
                }
                if (!myPhoto && !imageSizes.contains(Integer.valueOf(256))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 256.");
                    }
                    new InsertImageStatement(imageHash, 256, ImageUtils.resizeImageWithMaxSize(image.getData(), 256)).execute(connection);
                }
                int originalSize = ImageUtils.getMaxSize(image.getData());
                if (!myPhoto && originalSize > 256 && MyTunesRss.CONFIG.isImportOriginalImageSize() && !imageSizes.contains(Integer.valueOf(originalSize))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size " + originalSize + ".");
                    }
                    new InsertImageStatement(imageHash, originalSize, image.getData()).execute(connection);
                }
                new UpdateImageForTrackStatement(myTrackId, imageHash).execute(connection);
            }
        } catch (Throwable t) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not extract image from file \"" + myFile.getAbsolutePath() + "\".", t);
            }
        }
    }

    private Image getImage() throws IOException {
        if (myImage != null) {
            return myImage;
        }
        return getLocalFileImage();
    }

    private Image getLocalFileImage() throws IOException {
        Image image = null;
        File imageFile = findImageFile(myFile);
        if (imageFile != null) {
            return readImageFromImageFile(imageFile);
        } else {
            image = readImageFromTrackFile(image);
            if (image == null && mySource == TrackSource.ITunes) {
                image = findItunesArtwork();
            }
        }
        return image;
    }

    private Image readImageFromTrackFile(Image image) {
        if (myFile.isFile() && FileSupportUtils.isMp3(myFile)) {
            if (myFile.lastModified() >= myLastUpdateTime) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
                }
                image = MyTunesRssMp3Utils.getImage(myFile);
            } else {
                image = IMAGE_UP_TO_DATE;
            }
        } else if (myFile.isFile() && FileSupportUtils.isMp4(myFile)) {
            if (myFile.lastModified() >= myLastUpdateTime) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
                }
                image = MyTunesRssMp4Utils.getImage(myFile);
            } else {
                image = IMAGE_UP_TO_DATE;
            }
        }
        return image;
    }

    private Image readImageFromImageFile(File imageFile) throws IOException {
        Image image;
        if (imageFile.isFile() && imageFile.lastModified() >= myLastUpdateTime) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading image information from file \"" + imageFile.getAbsolutePath() + "\".");
            }
            image = new Image(IMAGE_TO_MIME.get(FilenameUtils.getExtension(imageFile.getName()).toLowerCase()), FileUtils.readFileToByteArray(
                    imageFile));
        } else {
            image = IMAGE_UP_TO_DATE;
        }
        return image;
    }

    private File findImageFile(File file) {
        for (String suffix : IMAGE_TO_MIME.keySet()) {
            File imageFile;
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase(suffix)) {
                imageFile = file;
            } else {
                imageFile = new File(file.getAbsolutePath() + "." + suffix);
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
                            for (File subdir : albumArtworkDir.listFiles()) {
                                itcFile = new File(subdir, idPair[0] + "/" + dirLevel1 + "/" + dirLevel2 + "/" + dirLevel3 + "/" + idPair[0] + "-" + idPair[1] + ".itc");
                                if (itcFile.isFile()) {
                                    break;
                                }
                            }
                            if (itcFile.isFile() && itcFile.lastModified() >= myLastUpdateTime) {
                                LOGGER.debug("Reading atoms from ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                Map<String, Mp4Atom> atoms = Mp4Utils.getAtoms(itcFile, Collections.<String>singletonList("item"));
                                Mp4Atom itemAtom = atoms.get("item");
                                if (itemAtom != null) {
                                    LOGGER.debug("Found item atom in ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                    int offset = CamelUtils.getValue(itemAtom.getData(), 0, 4, false, Endianness.Big);
                                    Iterator<ImageReader> iter = ImageIO.getImageReaders(new MemoryCacheImageInputStream(new ByteArrayInputStream(itemAtom.getData(), offset - 8, itemAtom.getData().length - (offset - 8))));
                                    if (iter.hasNext()) {
                                        ImageReader reader = iter.next();
                                        String mimeType = reader.getOriginatingProvider().getMIMETypes()[0];
                                        LOGGER.debug("Extracting image of type \"" + mimeType + "\" from ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                        return new Image(mimeType, ArrayUtils.subarray(itemAtom.getData(), offset - 8, itemAtom.getData().length));
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