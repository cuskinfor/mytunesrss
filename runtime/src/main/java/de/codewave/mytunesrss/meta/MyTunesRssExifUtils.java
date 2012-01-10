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
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * de.codewave.mytunesrss.meta.MyTunesRssMp3Utils
 */
public class MyTunesRssExifUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssExifUtils.class);

    private static ThreadLocal<SimpleDateFormat> EXIF_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
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
            if (imageMeta instanceof JpegImageMetadata) {
                JpegImageMetadata jpegMeta = (JpegImageMetadata) imageMeta;
                TiffField exifCreateDateTiffField = jpegMeta.findEXIFValue(TiffConstants.EXIF_TAG_CREATE_DATE);
                if (exifCreateDateTiffField != null) {
                    String value = (String) exifCreateDateTiffField.getValue();
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
}