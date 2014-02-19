package de.codewave.mytunesrss.datastore.statement;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.config.*;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.mytunesrss.meta.MyTunesRssMp3Utils;
import de.codewave.mytunesrss.meta.MyTunesRssMp4Utils;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackImagesStatement
 */
public class HandleTrackImagesStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandleTrackImagesStatement.class);
    private static Map<String, String> IMAGE_TO_MIME = new HashMap<>();
    static {
        IMAGE_TO_MIME.put("jpg", "image/jpeg");
        IMAGE_TO_MIME.put("jpeg", "image/jpeg");
        IMAGE_TO_MIME.put("gif", "image/gif");
        IMAGE_TO_MIME.put("png", "image/png");
    }

    private static class ExtendedImage extends Image {

        private boolean myFolderImage;

        public ExtendedImage(String mimeType, byte[] data, boolean folderImage) {
            super(mimeType, data);
            myFolderImage = folderImage;
        }

        private boolean isFolderImage() {
            return myFolderImage;
        }
    }

    private final Map<String, String> myFolderImageCache;
    private File myFile;
    private String myTrackId;
    private TrackSource mySource;
    private String mySourceId;
    private boolean myUseSingleImageInFolder;

    public HandleTrackImagesStatement(Map<String, String> folderImageCache, TrackSource source, String sourceId, File file, String trackId, boolean useSingleImageInFolder) {
        myFolderImageCache = folderImageCache;
        myFile = file;
        myTrackId = trackId;
        mySource = source;
        mySourceId = sourceId;
        myUseSingleImageInFolder = useSingleImageInFolder;
    }

    public void execute(Connection connection) {
        String imageHash = "";
        String cacheKey = null;
        try {
            cacheKey = IOUtils.getFilenameHash(myFile.getParentFile());
            imageHash = StringUtils.trimToEmpty(myFolderImageCache.get(cacheKey));
            if (StringUtils.isNotBlank(imageHash)) {
                LOGGER.debug("Found image hash \"" + imageHash + "\" for folder with hash \"" + cacheKey + "\" in cache.");
            }
        } catch (IOException e) {
            LOGGER.warn("Could not create filename hash for \"" + myFile.getParentFile() + "\".", e);
        }
        ExtendedImage image = null;
        try {
            if (StringUtils.isBlank(imageHash)) {
                image = getLocalFileImage();
                if (image != null && image.getData() != null && image.getData().length > 0) {
                    imageHash = MyTunesRssBase64Utils.encode(MyTunesRss.MD5_DIGEST.get().digest(image.getData()));
                    Collection<Integer> imageSizes = MyTunesRssUtils.getImageSizes(imageHash);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Image with hash \"" + imageHash + "\" has " + imageSizes.size() + " files.");
                    }
                    if (!imageSizes.contains(Integer.valueOf(32))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Saving image with size 32.");
                        }
                        MyTunesRssUtils.resizeImageWithMaxSize(image, MyTunesRssUtils.getSaveImageFile(imageHash, 32, "image/jpg"), 32, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                    }
                    if (!imageSizes.contains(Integer.valueOf(64))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Saving image with size 64.");
                        }
                        MyTunesRssUtils.resizeImageWithMaxSize(image, MyTunesRssUtils.getSaveImageFile(imageHash, 64, "image/jpg"), 64, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                    }
                    if (!imageSizes.contains(Integer.valueOf(128))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Saving image with size 128.");
                        }
                        MyTunesRssUtils.resizeImageWithMaxSize(image, MyTunesRssUtils.getSaveImageFile(imageHash, 128, "image/jpg"), 128, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                    }
                    if (!imageSizes.contains(Integer.valueOf(256))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Saving image with size 256.");
                        }
                        MyTunesRssUtils.resizeImageWithMaxSize(image, MyTunesRssUtils.getSaveImageFile(imageHash, 256, "image/jpg"), 256, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                    }
                    int originalSize = MyTunesRssUtils.getMaxImageSize(image);
                    if (originalSize > 256) {
                        Image bigImage = image;
                        // upper limit for image size is 2048
                        int bigImageSize = originalSize <= 2048 ? originalSize : 2048;
                        if (!imageSizes.contains(Integer.valueOf(bigImageSize))) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Saving image with size " + bigImageSize + ".");
                            }
                            if (bigImageSize < originalSize) {
                                // image must be resized
                                MyTunesRssUtils.resizeImageWithMaxSize(image, MyTunesRssUtils.getSaveImageFile(imageHash, 2048, "image/jpg"), 2048, (float)MyTunesRss.CONFIG.getJpegQuality(), "track=" + myFile.getAbsolutePath());
                            } else {
                                MyTunesRssUtils.saveImage(imageHash, bigImageSize, bigImage.getMimeType(), bigImage.getData());
                            }
                        }
                    }
                }
                if (image != null && image.isFolderImage() && StringUtils.isNotBlank(cacheKey)) {
                    LOGGER.debug("Inserting image hash \"" + imageHash + "\" for folder with hash \"" + cacheKey + "\" into cache.");
                    myFolderImageCache.put(cacheKey, imageHash);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not extractor resize image from file \"" + myFile.getAbsolutePath() + "\" or store resulting thumbnail.", e);
        } catch (RuntimeException e) {
            LOGGER.warn("Unknown problem handling images for \"" + myFile.getAbsolutePath() + "\".", e);
        } finally {
            if (image != null) {
                image.deleteImageFile();
            }
            try {
                if (StringUtils.isNotBlank(imageHash) && MyTunesRssUtils.getImageSizes(imageHash).size() == 0) {
                    // We actually have no images, e.g. all resizings and even storing the original failed. So we
                    // delete empty directories we created and set the image hash to a blank string for the database.
                    try {
                        ;
                        for (File dir = MyTunesRssUtils.getImageDir(imageHash); dir != null; dir = dir.getParentFile()) {
                            if (!dir.delete()) {
                                // directory probably not empty, break loop
                                break;
                            }
                        }
                    } catch (RuntimeException e) {
                        LOGGER.warn("Could not delete empty image thumbnail directories.", e);
                    }
                    imageHash = "";
                }
                new UpdateImageForTrackStatement(myTrackId, imageHash).execute(connection);
            } catch (SQLException e) {
                LOGGER.error("Could not mark track \"" + myTrackId + "\". for having no image.", e);
            }
        }
    }

    private ExtendedImage getLocalFileImage() throws IOException {
        ExtendedImage image = null;
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
            image = readImageFromTrackFile();
            if (image != null && !MyTunesRssUtils.isImageUsable(image)) {
                LOGGER.debug("Image from track file not readable.");
                // image not readable, we don't have one
                image = null;
            }
        }
        return image;
    }

    private ExtendedImage readImageFromTrackFile() {
        Image image = null;
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
        return image != null ? new ExtendedImage(image.getMimeType(), image.getData(), false) : null;
    }

    private ExtendedImage readImageFromImageFile(File imageFile) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading image information from file \"" + imageFile.getAbsolutePath() + "\".");
        }
        return new ExtendedImage(MyTunesRssUtils.guessContentType(imageFile), FileUtils.readFileToByteArray(imageFile), true);
    }

    private File findImageFile(File file) {
        if (IMAGE_TO_MIME.keySet().contains(StringUtils.lowerCase(FilenameUtils.getExtension(file.getName())))) {
            return file;
        } else {
            for (String imagePattern : ((CommonTrackDatasourceConfig)MyTunesRss.CONFIG.getDatasource(mySourceId)).getTrackImagePatterns()) {
                LOGGER.debug("Trying image pattern \"" + imagePattern + "\".");
                DirectoryScanner directoryScanner = new DirectoryScanner();
                directoryScanner.setBasedir(file.getParentFile());
                directoryScanner.setIncludes(new String[]{imagePattern});
                directoryScanner.setCaseSensitive(false);
                directoryScanner.scan();
                for (String includedFile : directoryScanner.getIncludedFiles()) {
                    File imageFile = new File(file.getParentFile(), includedFile);
                    LOGGER.debug("Checking image file \"" + imageFile.getAbsolutePath() + "\".");
                    if (imageFile.isFile()) {
                        return imageFile;
                    }
                }
            }
            // last resort: if there is only one image file in the folder, return it.
            File[] imagesInFolder = myUseSingleImageInFolder ? getImagesInFolder(file.getParentFile()) : null;
            return imagesInFolder != null && imagesInFolder.length == 1 ? imagesInFolder[0] : null;
        }
    }

    private File[] getImagesInFolder(File folder) {
        LOGGER.debug("Fetching images for folder \"" + folder.getAbsolutePath() + "\".");
        File[] files = folder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && IMAGE_TO_MIME.keySet().contains(StringUtils.lowerCase(FilenameUtils.getExtension(file.getName())));
            }
        });
        LOGGER.debug("Found \"" + (files != null ? files.length : "0") + "\" images.");
        return files;
    }

    private ExtendedImage findItunesArtwork() throws IOException {
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
                            if (itcFile != null && itcFile.isFile()) {
                                LOGGER.debug("Reading atoms from ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                Mp4Atom itemAtom = MyTunesRss.MP4_PARSER.parseAndGet(itcFile, "item");
                                if (itemAtom != null) {
                                    LOGGER.debug("Found item atom in ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                    int offset = CamelUtils.getIntValue(itemAtom.getData(), 0, 4, false, Endianness.Big);
                                    Iterator<ImageReader> iter = ImageIO.getImageReaders(new MemoryCacheImageInputStream(new ByteArrayInputStream(itemAtom.getData(), offset - 8, itemAtom.getData().length - (offset - 8))));
                                    while (iter.hasNext()) {
                                        ImageReader reader = iter.next();
                                        LOGGER.debug("Trying to read image using image reader of type \"" + reader.getClass().getName() + "\".");
                                        try {
                                            String mimeType = reader.getOriginatingProvider().getMIMETypes()[0];
                                            LOGGER.debug("Extracting image of type \"" + mimeType + "\" from ITC file \"" + itcFile.getAbsolutePath() + "\".");
                                            return new ExtendedImage(mimeType, ArrayUtils.subarray(itemAtom.getData(), offset - 8, itemAtom.getData().length - (offset - 8)), false);
                                        } catch (Exception e) {
                                            LOGGER.info("Could not read image using image reader of type \"" + reader.getClass().getName() + "\".", e);
                                        }
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
