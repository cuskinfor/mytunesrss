/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.mp3;

import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.farng.mp3.*;
import org.farng.mp3.id3.*;

import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.mp3.ID3Utils
 */
public class ID3Utils {
    private static final Log LOG = LogFactory.getLog(ID3Utils.class);

    public static Image getImage(Track track) {
        MP3File mp3 = null;
        try {
            mp3 = new MP3File(track.getFile());
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create MP3 file for \"" + track.getFile() + "\".", e);
            }
        } catch (TagException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create MP3 file for \"" + track.getFile() + "\".", e);
            }
        }
        if (mp3 != null && mp3.hasID3v2Tag()) {
            AbstractID3v2 id3v2Tag = mp3.getID3v2Tag();
            Iterator<ID3v2_2Frame> apicFrames = (Iterator<ID3v2_2Frame>)id3v2Tag.getFrameOfType("APIC");
            if (apicFrames != null && apicFrames.hasNext()) {
                ID3v2_2Frame apicFrame = apicFrames.next();
                String mimeType = (String)apicFrame.getBody().getObject("MIME Type");
                byte[] data = (byte[])apicFrame.getBody().getObject("Picture Data");
                if (StringUtils.isNotEmpty(mimeType) && data != null && data.length > 0) {
                    return new Image(mimeType, data);
                }
            }
        }
        return null;
    }
}