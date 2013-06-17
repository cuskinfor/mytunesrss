/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.meta;

import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * de.codewave.mytunesrss.meta.MyTunesRssMp3Utils
 */
public class MyTunesRssExifUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssExifUtils.class);

    private static ThreadLocal<SimpleDateFormat> EXIF_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            //sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
        }
    };

    /**
     * Get the creation date of the image file from the EXIF data. The timestamp returned is the date
     * in the GMT time zone.
     *
     * @param file A file.
     * @return The creation date in GMT or NULL if no date was found or could not be extracted.
     */
    public static Long getCreateDate(File file) {
        try {
            IImageMetadata imageMeta = Sanselan.getMetadata(file);
            TiffImageMetadata tiffImageMetadata = null;
            if (imageMeta instanceof JpegImageMetadata) {
                JpegImageMetadata jpegMeta = (JpegImageMetadata) imageMeta;
                tiffImageMetadata = jpegMeta.getExif();
            } else if (imageMeta instanceof TiffImageMetadata) {
                tiffImageMetadata = (TiffImageMetadata) imageMeta;
            }
            if (tiffImageMetadata != null) {
                TiffField dateField = tiffImageMetadata.findField(TiffConstants.EXIF_TAG_CREATE_DATE);
                if (dateField == null) {
                    // fallback to modify date if create date is not available
                    dateField = tiffImageMetadata.findField(TiffConstants.EXIF_TAG_MODIFY_DATE);
                }
                if (dateField != null) {
                    String value = (String) dateField.getValue();
                    if (StringUtils.isNotBlank(value) && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("EXIF create date for \"" + file.getAbsolutePath() + "\" is \"" + value + "\".");
                    }
                    return EXIF_DATE_FORMAT.get().parse(value).getTime();
                }
            }
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Could not read EXIF data from \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        return null;
    }

    public static List<TiffField> getExifData(File file) {
        try {
            IImageMetadata imageMeta = Sanselan.getMetadata(file);
            TiffImageMetadata tiffImageMetadata = null;
            if (imageMeta instanceof JpegImageMetadata) {
                JpegImageMetadata jpegMeta = (JpegImageMetadata) imageMeta;
                tiffImageMetadata = jpegMeta.getExif();
            } else if (imageMeta instanceof TiffImageMetadata) {
                tiffImageMetadata = (TiffImageMetadata) imageMeta;
            }
            if (tiffImageMetadata != null) {
                return tiffImageMetadata.getAllFields();
            }
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Could not read EXIF data from \"" + file.getAbsolutePath() + "\".", e);
            }
        }
        return Collections.emptyList();
    }
}