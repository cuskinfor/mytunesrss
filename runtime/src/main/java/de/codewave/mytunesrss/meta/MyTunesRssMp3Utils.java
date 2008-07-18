/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.meta;

import de.codewave.camel.mp3.Id3v2Tag;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp3.framebody.v2.PICFrameBody;
import de.codewave.camel.mp3.framebody.v3.APICFrameBody;
import de.codewave.camel.mp3.structure.Frame;
import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * de.codewave.mytunesrss.meta.MyTunesRssMp3Utils
 */
public class MyTunesRssMp3Utils {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssMp3Utils.class);

    public static Image getImage(Track track) {
        File file = track.getFile();
        return getImage(file);
    }

    public static Image getImage(File file) {
        if (FileSupportUtils.isMp3(file)) {
            try {
                Id3v2Tag id3v2Tag = Mp3Utils.readId3v2Tag(file);
                return getImage(id3v2Tag);
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Could not extract artwork for \"" + file + "\".", e);
                }
            }
        }
        return null;
    }

    public static Image getImage(Id3v2Tag id3v2Tag) {
        if (id3v2Tag != null && id3v2Tag.getFrames() != null) {
            for (Frame frame : id3v2Tag.getFrames()) {
                if ("APIC".equals(frame.getId())) {
                    APICFrameBody frameBody = new APICFrameBody(frame);
                    return new Image(frameBody.getMimeType(), frameBody.getPictureData());
                } else if ("PIC".equals(frame.getId())) {
                    PICFrameBody frameBody = new PICFrameBody(frame);
                    return new Image(frameBody.getMimeType(), frameBody.getPictureData());
                }
            }
        }
        return null;
    }
}