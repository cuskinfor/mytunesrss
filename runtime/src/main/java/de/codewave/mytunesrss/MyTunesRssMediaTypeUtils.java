package de.codewave.mytunesrss;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MyTunesRssMediaTypeUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssMediaTypeUtils.class);
    
    private static final TikaConfig TIKA_CONFIG;
    static {
        try {
            TIKA_CONFIG = new TikaConfig();
        } catch (TikaException |IOException e) {
            throw new RuntimeException("Could not initialize TIKA config!", e);
        }
    }

    public static MediaType detectMediaType(File file) {
        try {
            Metadata metadata = new Metadata();
            return TIKA_CONFIG.getDetector().detect(TikaInputStream.get(file, metadata), metadata);
        } catch (FileNotFoundException ignored) {
            return null;
        } catch (IOException e) {
            LOGGER.warn("Could not detect media type of \"" + file.getAbsolutePath() + "\".", e);
        }
        return MediaType.OCTET_STREAM;
    }

    public static MediaType detectMediaType(String filename, InputStream inputStream) {
        try {
            Metadata metadata = new Metadata();
            if (StringUtils.isNotBlank(filename)) {
                metadata.set(Metadata.RESOURCE_NAME_KEY, filename);
            }
            return TIKA_CONFIG.getDetector().detect(TikaInputStream.get(inputStream), metadata);
        } catch (FileNotFoundException ignored) {
            return null;
        } catch (IOException e) {
            LOGGER.warn("Could not detect media type of stream (filename \"" + filename + "\").", e);
        }
        return MediaType.OCTET_STREAM;
    }

    public static boolean isImage(MediaType mediaType) {
        return mediaType != null && "image".equalsIgnoreCase(mediaType.getType());
    }

    public static boolean isAudio(MediaType mediaType) {
        return mediaType != null && "audio".equalsIgnoreCase(mediaType.getType());
    }

    public static boolean isVideo(MediaType mediaType) {
        return mediaType != null && "video".equalsIgnoreCase(mediaType.getType());
    }

    public static boolean isSupported(MediaType tikaMediaType) { 
        return isImage(tikaMediaType) || isAudio(tikaMediaType) || isVideo(tikaMediaType);
    }

}
