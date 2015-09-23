/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure.v4;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp3.structure.Header;

import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.camel.mp3.structure.HeaderV4
 */
public class HeaderV4 extends Header {
    private ExtendedHeaderV4 myExtendedHeader;

    public HeaderV4(Header header, InputStream stream) throws IOException, IllegalHeaderException {
        super(header);
        if (isExtendedHeader()) {
            myExtendedHeader = new ExtendedHeaderV4(stream);
            setBodySize(getBodySize() - getExtendedHeader().getSize());
        }
    }

    public boolean isExtendedHeader() {
        return (getFlags() & 0x40) == 0x40;
    }

    public boolean isExperimental() {
        return (getFlags() & 0x20) == 0x20;
    }

    public boolean isFooter() {
        return (getFlags() & 0x10) == 0x10;
    }

    public ExtendedHeaderV4 getExtendedHeader() {
        return myExtendedHeader;
    }
}