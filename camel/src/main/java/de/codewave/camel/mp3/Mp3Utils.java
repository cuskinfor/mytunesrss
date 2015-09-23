/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp3.exception.Mp3Exception;
import de.codewave.camel.mp3.structure.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * de.codewave.camel.mp3.Mp3Utils
 */
public class Mp3Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mp3Utils.class);
    public static final Properties GENRES = new Properties();
    public static final int MAX_BUFFER_SIZE = 1024 * 1024 * 10;// maximum of 20 MB for frames or headers

    static {
        try {
            GENRES.load(Mp3Utils.class.getResourceAsStream("genre.properties"));
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not load ID3 tag genre information.", e);
            }
        }
    }

    public static Id3v2Tag readId3v2Tag(File file) throws IOException, IllegalHeaderException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            return Id3v2Tag.createTag(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static Id3v2Tag readId3v2Tag(URL url) throws IOException, IllegalHeaderException {
        InputStream stream = null;
        try {
            stream = url.openStream();
            return Id3v2Tag.createTag(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static Id3v2Tag readId3v2Tag(InputStream stream) throws IOException, IllegalHeaderException {
        return Id3v2Tag.createTag(stream);
    }

    public static Id3v1Tag readId3v1Tag(File file) throws IOException, IllegalHeaderException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            return Id3v1Tag.createTag(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }

        }
    }

    public static Id3v1Tag readId3v1Tag(URL url) throws IOException, IllegalHeaderException {
        InputStream stream = null;
        try {
            stream = url.openStream();
            return Id3v1Tag.createTag(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }

        }
    }

    public static Id3v1Tag readId3v1Tag(InputStream stream) throws IOException, IllegalHeaderException {
        return Id3v1Tag.createTag(stream);
    }

    public static Id3Tag readId3Tag(File file) throws IOException, IllegalHeaderException {
        Id3Tag tag = readId3v2Tag(file);
        if (tag == null) {
            tag = readId3v1Tag(file);
        }
        return tag;
    }

    public static Id3Tag readId3Tag(URL url) throws IOException, IllegalHeaderException {
        Id3Tag tag = readId3v2Tag(url);
        if (tag == null) {
            tag = readId3v1Tag(url);
        }
        return tag;
    }

    public static String translateGenre(String genreCode) {
        if (genreCode.length() == 1) {
            genreCode = "00" + genreCode;
        } else if (genreCode.length() == 2) {
            genreCode = "0" + genreCode;
        }
        String translation = GENRES.getProperty("genre." + genreCode);
        return translation != null ? translation : "";
    }

    public static Mp3Info getMp3Info(InputStream inputStream) throws IOException, Mp3Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        try {
            Header.skipImmediateHeader(bufferedInputStream);
        } catch (IllegalHeaderException e) {
            throw new Mp3Exception("Could not calculate duration from audio frames.", e);
        }
        double millis = 0;
        List<Long> bitRates = new ArrayList<Long>();
        List<Long> sampleRates = new ArrayList<Long>();
        long minBitRate = Long.MAX_VALUE;
        long maxBitRate = Long.MIN_VALUE;
        long minSampleRate = Long.MAX_VALUE;
        long maxSampleRate = Long.MIN_VALUE;
        for (Mp3Header mp3Header = nextMp3Header(bufferedInputStream); mp3Header != null; mp3Header = nextMp3Header(bufferedInputStream)) {
            bitRates.add(mp3Header.getBitRate());
            sampleRates.add(mp3Header.getSampleRate());
            minBitRate = Math.min(minBitRate, mp3Header.getBitRate());
            maxBitRate = Math.max(maxBitRate, mp3Header.getBitRate());
            minSampleRate = Math.min(minSampleRate, mp3Header.getSampleRate());
            maxSampleRate = Math.max(maxSampleRate, mp3Header.getSampleRate());
            millis += mp3Header.getFrameDurationMillis();
            long skipSize = mp3Header.getFrameLength() - 4; // 4 byte less since header was already read, although I don't understand this behaviour
            for (long skipped = bufferedInputStream.skip(skipSize); skipped > 0 && skipSize > 0; skipped = bufferedInputStream.skip(skipSize)) {
                skipSize -= skipped;
            }
        }
        bufferedInputStream.close();
        Collections.sort(bitRates);
        Collections.sort(sampleRates);
        return new Mp3Info(minBitRate, maxBitRate, bitRates.get(bitRates.size() / 2), minSampleRate, maxSampleRate, sampleRates.get(sampleRates.size() / 2), (int)(millis / 1000d));
    }

    private static Mp3Header nextMp3Header(BufferedInputStream bufferedInputStream) throws IOException {
        for (int firstSyncByte = bufferedInputStream.read(); firstSyncByte != -1; firstSyncByte = bufferedInputStream.read()) {
            if (firstSyncByte == 255) {
                bufferedInputStream.mark(3);
                long sync = 0xFF000000L | ((long)bufferedInputStream.read()) << 16 | ((long)bufferedInputStream.read()) << 8 | (long)bufferedInputStream.read();
                if (Mp3Header.isValidSync(sync)) {
                    return new Mp3Header(sync);
                } else {
                    bufferedInputStream.reset();
                }
            }
        }
        return null;
    }
}
