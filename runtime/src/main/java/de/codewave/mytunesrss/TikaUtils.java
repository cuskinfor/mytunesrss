package de.codewave.mytunesrss;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;

public class TikaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TikaUtils.class);

    private static final TikaConfig TIKA_CONFIG;

    static {
        try {
            TIKA_CONFIG = new TikaConfig();
        } catch (TikaException | IOException e) {
            throw new RuntimeException("Could not initialize TIKA config!", e);
        }
    }

    public static de.codewave.mytunesrss.config.MediaType getMediaType(File file) {
        return de.codewave.mytunesrss.config.MediaType.get(getContentType(file));
    }

    public static de.codewave.mytunesrss.config.MediaType getMediaType(String filename, TikaInputStream tikaInputStream) {
        return de.codewave.mytunesrss.config.MediaType.get(getContentType(filename, tikaInputStream));
    }

    public static String getContentType(File file) {
        Metadata metadata = new Metadata();
        try (TikaInputStream tikaInputStream = TikaInputStream.get(file, metadata)) {
            MediaType mediaType = TIKA_CONFIG.getDetector().detect(tikaInputStream, metadata);
            return mediaType.getBaseType().toString();
        } catch (IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not get content type from \"" + file.getAbsolutePath() + "\".", e);
            } else {
                LOGGER.warn("Could not get content type from \"" + file.getAbsolutePath() + "\".");
            }
        }
        return null;
    }

    public static String getContentType(String filename, TikaInputStream tikaInputStream) {
        Metadata metadata = new Metadata();
        metadata.set(TikaMetadataKeys.RESOURCE_NAME_KEY, filename);
        try {
            MediaType mediaType = TIKA_CONFIG.getDetector().detect(tikaInputStream, metadata);
            return mediaType.getBaseType().toString();
        } catch (IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not get content type from stream with (virtual) file name \"" + filename + "\".", e);
            } else {
                LOGGER.warn("Could not get content type from stream with (virtual) file name \"" + filename + "\".");
            }
        }
        return null;
    }

    public static Metadata extractMetadata(File file) {
        Metadata metadata = new Metadata();
        try (TikaInputStream tikaInputStream = TikaInputStream.get(file, metadata)) {
            MediaType mediaType = TIKA_CONFIG.getDetector().detect(tikaInputStream, metadata);
            metadata.set(Metadata.CONTENT_TYPE, mediaType.getBaseType().toString());
            TIKA_CONFIG.getParser().parse(tikaInputStream, new DefaultHandler(), metadata, new ParseContext());
        } catch (SAXException|TikaException|IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn("Could not extract metadata from \"" + file.getAbsolutePath() + "\".", e);
            } else {
                LOGGER.warn("Could not extract metadata from \"" + file.getAbsolutePath() + "\".");
            }
        }
        return metadata;
    }

    public static Metadata extractMetadata(String filename, TikaInputStream tikaInputStream) {
        Metadata metadata = new Metadata();
        metadata.set(TikaMetadataKeys.RESOURCE_NAME_KEY, filename);
        try {
            MediaType mediaType = TIKA_CONFIG.getDetector().detect(tikaInputStream, metadata);
            metadata.set(Metadata.CONTENT_TYPE, mediaType.getBaseType().toString());
            TIKA_CONFIG.getParser().parse(tikaInputStream, new DefaultHandler(), metadata, new ParseContext());
        } catch (SAXException|TikaException|IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn("Could not extract metadata from stream with (virtual) file name \"" + filename + "\".", e);
            } else {
                LOGGER.warn("Could not extract metadata from stream with (virtual) file name \"" + filename + "\".");
            }
        }
        return metadata;
    }

}
