/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp3.structure.v2.HeaderV2;
import de.codewave.camel.mp3.structure.v3.HeaderV3;
import de.codewave.camel.mp3.structure.v4.HeaderV4;

import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.camel.mp3.structure.Header
 */
public class Header {
    private static final int MAGIC_HEADER_START = 0x494433; // "ID3" string

    private int myVersion;
    private int myRevision;
    private byte myFlags;
    private int myBodySize;

    public static Header createHeader(InputStream stream) throws IOException, IllegalHeaderException {
        Header header = null;
        try {
            header = new Header(stream);
        } catch (IOException e) {
            return null;
        } catch (IllegalHeaderException e) {
            return null;
        }
        if (header.getVersion() == 2) {
            return new HeaderV2(header);
        } else if (header.getVersion() == 3) {
            return new HeaderV3(header, stream);
        } else if (header.getVersion() == 4) {
            return new HeaderV4(header, stream);
        }
        return null;
    }

    public static void skipImmediateHeader(InputStream stream) throws IOException, IllegalHeaderException {
        if (!stream.markSupported()) {
            throw new IllegalArgumentException("Stream must support mark.");
        }
        stream.mark(3);
        int magic = 0;
        for (int i = 0; i < 3; i++) {
            int value = stream.read();
            if (value != -1) {
                magic = (magic << 8) + value;
            }
        }
        if (magic == MAGIC_HEADER_START) {
            stream.reset();
            stream.mark(20);
            Header header = Header.createHeader(stream);
            if (header != null) {
                int skipSize = header.getBodySize();
                for (long skipped = stream.skip(skipSize); skipped > 0 && skipSize > 0; skipped = stream.skip(skipSize)) {
                    skipSize -= skipped;
                }
            } else {
                stream.reset();
            }
        } else {
            stream.reset();
        }
    }

    protected Header(Header header) {
        myVersion = header.getVersion();
        myRevision = header.getRevision();
        myFlags = header.getFlags();
        myBodySize = header.getBodySize();
    }

    private Header(InputStream stream) throws IOException, IllegalHeaderException {
        int magic = 0;
        int value = 0;
        do {
            do {
                value = stream.read();
                magic = (magic << 8) + value;
            } while (magic != MAGIC_HEADER_START && value > -1);
            if (magic == MAGIC_HEADER_START) {
                byte[] data = new byte[7];
                stream.read(data);
                myVersion = data[0];
                myRevision = data[1];
                myFlags = data[2];
                myBodySize = CamelUtils.getIntValue(data, 3, 4, true, Endianness.Big);
                if (myVersion < 2 || myVersion > 4) {
                    magic = 0; // set magic to 0 to keep scanning for header
                }
            }
        } while (value > -1 && magic != MAGIC_HEADER_START);
        if (value == -1 && magic != MAGIC_HEADER_START) {
            throw new IOException("No valid ID3v2 header found in stream.");

        }
    }

    public int getVersion() {
        return myVersion;
    }

    public int getBodySize() {
        return myBodySize;
    }

    public int getRevision() {
        return myRevision;
    }

    public byte getFlags() {
        return myFlags;
    }

    public String getShortVersionIdentifier() {
        return "ID3v2." + myVersion;
    }

    public String getLongVersionIdentifier() {
        return "ID3v2." + myVersion + "." + myRevision;
    }

    public boolean isUnsynchronization() {
        return (myFlags & 0x80) == 0x80;
    }

    public void setBodySize(int bodySize) {
        myBodySize = bodySize;
    }

    @Override
    public String toString() {
        return getShortVersionIdentifier() + " Header: bodySize=" + getBodySize() + ", flags=0x" + Integer.toHexString(getFlags());
    }
}