/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.mp3;

import de.codewave.id3v2.*;
import de.codewave.id3v2.framebody.*;
import de.codewave.id3v2.structure.*;
import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.mp3.ID3Utils
 */
public class ID3Utils {
    private static final Log LOG = LogFactory.getLog(ID3Utils.class);

    public static Image getImage(Track track) {
        Id3v2Tag id3v2Tag = null;
        try {
            id3v2Tag = new Id3v2Tag(track.getFile().toURL());
            for (Frame frame : id3v2Tag.getFrames()) {
                if ("APIC".equals(frame.getId())) {
                    APICFrameBody frameBody = new APICFrameBody(frame);
                    return new Image(frameBody.getMimeType(), frameBody.getPictureData());
                } else if ("PIC".equals(frame.getId())) {
                    PICFrameBody frameBody = new PICFrameBody(frame);
                    return new Image(frameBody.getMimeType(), frameBody.getPictureData());
                }
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not extract artwork for \"" + track.getFile() + "\".", e);
            }
        }
        return null;
    }
}