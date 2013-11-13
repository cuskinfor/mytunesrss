/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.HandlePhotoImagesStatement
 */
public class HandlePhotoImagesStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlePhotoImagesStatement.class);
    private static Map<String, String> IMAGE_TO_MIME = new HashMap<String, String>();

    static {
        IMAGE_TO_MIME.put("jpg", "image/jpeg");
        IMAGE_TO_MIME.put("gif", "image/gif");
        IMAGE_TO_MIME.put("png", "image/png");
    }

    private File myFile;
    private String myPhotoId;
    private String myImageHash;

    public HandlePhotoImagesStatement(File file, String photoId) {
        myPhotoId = photoId;
        myFile = file;
    }

    public void execute(Connection connection) throws SQLException {
        try {
            Image image = getImage();
            if (image != null && image.getData() != null && image.getData().length > 0) {
                String imageHash = MyTunesRssBase64Utils.encode(MyTunesRss.MD5_DIGEST.get().digest(image.getData()));
                List<Integer> imageSizes = new GetImageSizesQuery(imageHash).execute(connection).getResults();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Image with hash \"" + imageHash + "\" has " + imageSizes.size() + " entries in database.");
                }
                if (!imageSizes.contains(Integer.valueOf(128))) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Inserting image with size 128.");
                    }
                    Image image128 = MyTunesRssUtils.resizeImageWithMaxSize(myFile, 128, (float)MyTunesRss.CONFIG.getJpegQuality(), "photo=" + myFile.getAbsolutePath());
                    new InsertImageStatement(imageHash, 128, image128.getMimeType(), image128.getData()).execute(connection);
                }
                myImageHash = imageHash;
            }
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not extract image from file \"" + myFile.getAbsolutePath() + "\".", e);
            }
        } finally {
            if (myImageHash != null) {
                new UpdateImageForPhotoStatement(myPhotoId, myImageHash).execute(connection);
            }
        }
    }

    private Image getImage() throws IOException {
        if (myFile.isFile()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading image information from file \"" + myFile.getAbsolutePath() + "\".");
            }
            return new Image(IMAGE_TO_MIME.get(FilenameUtils.getExtension(myFile.getName()).toLowerCase()), FileUtils.readFileToByteArray(myFile));
        } else {
            return null;
        }
    }

    /**
     * Get the photo's thumbnail image hash. Only useful after the {@link #execute(java.sql.Connection)} method has been
     * called.
     *
     * @return The hash of the photo's thumbnail image.
     */
    public String getImageHash() {
        return myImageHash;
    }
}
