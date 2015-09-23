/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure.v2;

import de.codewave.camel.mp3.structure.Header;

/**
 * de.codewave.camel.mp3.structure.HeaderV2
 */
public class HeaderV2 extends Header {
    public HeaderV2(Header header) {
        super(header);
    }

    public boolean isCompression() {
        return (getFlags() & 0x40) == 0x40;
    }
}