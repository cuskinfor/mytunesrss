/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * de.codewave.camel.mp3.framebody.FrameBodyParser
 */
public class FrameBodyParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameBodyParser.class);

    private byte[] myBodyData;
    private int myCursor;
    public static final int ENCODING_ISO_8859_1 = 0;
    public static final int ENCODING_UTF_16_BOM = 1;
    public static final int ENCODING_UTF_16_NO_BOM = 2;
    public static final int ENCODING_UTF_8 = 3;
    private int myBeginOffset;

    public FrameBodyParser(byte[] bodyData, int beginOffset) {
        myBodyData = bodyData;
        myBeginOffset = 0;
        reset();
    }

    public void reset() {
        myCursor = myBeginOffset;
    }

    public synchronized int getInteger(int length) {
        int value = CamelUtils.getIntValue(myBodyData, myCursor, length, false, Endianness.Big);
        myCursor += length;
        return value;
    }

    public synchronized String getString() {
        return getMaxLengthString(ENCODING_ISO_8859_1, 0);
    }

    public synchronized String getString(int encoding) {
        return getMaxLengthString(encoding, 0);
    }

    public synchronized String getMaxLengthString(int encoding, int maxLength) {
        int maxTerminator = myCursor + maxLength;
        int terminator = myCursor;
        if (isIllegalEncoding(encoding)) {
            // use ISO-8859-1 for illegal encoding
            encoding = ENCODING_ISO_8859_1;
        }
        if (encoding == ENCODING_ISO_8859_1 || encoding == ENCODING_UTF_8) {
            while (terminator < myBodyData.length && (maxLength <= 0 || terminator < maxTerminator) && myBodyData[terminator] != 0) {
                terminator++;
            }
        } else {
            while (terminator < myBodyData.length - 1 && (maxLength <= 0 || terminator < maxTerminator - 1) &&
                    (myBodyData[terminator] != 0 || myBodyData[terminator + 1] != 0)) {
                terminator += 2;
            }
        }
        String value = getFixedLengthString(encoding, terminator - myCursor);
        if (maxLength == 0 || terminator < maxTerminator) {
            myCursor++;
        }
        return value;
    }

    private boolean isIllegalEncoding(int encoding) {
        return encoding != ENCODING_ISO_8859_1 && encoding != ENCODING_UTF_16_BOM && encoding != ENCODING_UTF_16_NO_BOM && encoding != ENCODING_UTF_8;
    }

    public synchronized String getFixedLengthString(int length) {
        return getFixedLengthString(ENCODING_ISO_8859_1, length);
    }

    public synchronized String getFixedLengthString(int encoding, int length) {
        String value;
        try {
            value = new String(myBodyData, myCursor, length, getCharset(encoding));
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not create string using charset \"" + getCharset(encoding) + "\". Using platform default.", e);
            }
            value = new String(myBodyData, myCursor, length);
        }
        myCursor += length;
        return value;
    }

    public synchronized byte[] getBytes() {
        return getBytes(myBodyData.length - myCursor);
    }

    public synchronized byte[] getBytes(int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(myBodyData, myCursor, bytes, 0, length);
        myCursor += length;
        return bytes;
    }

    private String getCharset(int encoding) {
        switch (encoding) {
            case ENCODING_ISO_8859_1:
                return CamelUtils.DEFAULT_CHARSET;
            case ENCODING_UTF_16_BOM:
                return "UTF-16";// BOM
            case ENCODING_UTF_16_NO_BOM:
                return "UTF-16";// no BOM
            case ENCODING_UTF_8:
                return "UTF-8";
            default:
                LOGGER.debug("Illegal encoding in ID3 tag, using ISO-8859-1.");
                return CamelUtils.DEFAULT_CHARSET;
        }
    }
}
