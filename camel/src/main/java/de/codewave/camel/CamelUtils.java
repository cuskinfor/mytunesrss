/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;

/**
 * de.codewave.camel.mp3.Mp3Utils
 */
public class CamelUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelUtils.class);

    public static final String DEFAULT_CHARSET = getDefaultCharset();

    private static String getDefaultCharset() {
        try {
            return Charset.forName("windows-1252").name();
        } catch (UnsupportedCharsetException|IllegalCharsetNameException ignore) {
            LOGGER.info("Could not find charset \"windows-1252\".");
        }
        try {
            return Charset.forName("ISO-8859-1").name();
        } catch (UnsupportedCharsetException|IllegalCharsetNameException ignore) {
            LOGGER.info("Could not find charset \"ISO-8859-1\".");
        }
        return Charset.defaultCharset().name();
    }

    public static int getIntValue(byte[] buffer, int offset, int length, boolean synchSafe, Endianness endianness) {
        int value = 0;
        if (endianness == Endianness.Big) {
            for (int i = 0; i < length; i++) {
                value <<= (synchSafe ? 7 : 8);
                value |= (((int)buffer[offset + i]) & 0xff);
            }
        } else {
            for (int i = length - 1; i >= 0; i--) {
                value <<= (synchSafe ? 7 : 8);
                value |= (((int)buffer[offset + i]) & 0xff);
            }
        }
        return value;
    }

    public static long getLongValue(byte[] buffer, int offset, int length, boolean synchSafe, Endianness endianness) {
        long value = 0;
        if (endianness == Endianness.Big) {
            for (int i = 0; i < length; i++) {
                value <<= (synchSafe ? 7 : 8);
                value |= (((long)buffer[offset + i]) & 0xff);
            }
        } else {
            for (int i = length - 1; i >= 0; i--) {
                value <<= (synchSafe ? 7 : 8);
                value |= (((long)buffer[offset + i]) & 0xff);
            }
        }
        return value;
    }

    public static void setLongValue(byte[] buffer, int offset, int length, Endianness endianness, long value) {
        if (endianness == Endianness.Little) {
            for (int i = 0; i < length; i++) {
                buffer[offset + i] = (byte)(value & 0xff);
                value >>= 8;
            }
        } else {
            for (int i = length - 1; i >= 0; i--) {
                buffer[offset + i] = (byte)(value & 0xff);
                value >>= 8;
            }
        }
    }

    public static String getString(byte[] buffer, int offset, int length, String charset) {
        try {
            // find first 0x00 byte as delimiter
            int i;
            for (i = 0; i < length && buffer[offset + i] != 0x00; i++);
            return new String(buffer, offset, i, charset);
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not create string using charset \"" + charset + "\". Using platform default.", e);
            }
            return new String(buffer, offset, length);
        }
    }

    public static void setString(byte[] buffer, int offset, int length, String charset, String value) throws UnsupportedEncodingException {
        byte[] bytes = value.getBytes(charset);
        if (bytes.length > length) {
            throw new RuntimeException("Cannot write " + bytes.length + " bytes into a " + length + " byte buffer.");
        }
        // prepend 0x00 bytes
        for (int i = 0; i < (length - bytes.length); i++) {
            buffer[offset + i] = 0;
        }
        // now copy
        System.arraycopy(bytes, 0, buffer, offset + (length - bytes.length), length);
    }

    public static String getTerminatedOrMaxLengthString(byte[] buffer, int offset, int maxLength, String charset) {
        int terminator = offset;
        for (terminator = offset; terminator < offset + maxLength; terminator++) {
            if (buffer[terminator] == 0x00) {
                break;
            }
        }
        return getString(buffer, offset, terminator - offset, charset);
    }
}
