/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure.v3;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp3.structure.Header;

import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.camel.mp3.structure.HeaderV3
 */
public class HeaderV3 extends Header {
    private ExtendedHeaderV3 myExtendedHeader;

    public HeaderV3(Header header, InputStream stream) throws IOException, IllegalHeaderException {
        super(header);
        if (isExtendedHeader()) {
            myExtendedHeader = new ExtendedHeaderV3(stream);
            setBodySize(getBodySize() - getExtendedHeader().getSize());
        }
    }

    public boolean isExtendedHeader() {
        return (getFlags() & 0x40) == 0x40;
    }

    public boolean isExperimental() {
        return (getFlags() & 0x20) == 0x20;
    }

    public ExtendedHeaderV3 getExtendedHeader() {
        return myExtendedHeader;
    }
}