/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp3.framebody.FrameBody;
import de.codewave.camel.mp3.structure.Frame;
import de.codewave.camel.mp3.structure.Header;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * de.codewave.camel.mp3.Id3v2Tag
 */
public class Id3v2Tag implements Id3Tag {
    private static final Logger LOGGER = LoggerFactory.getLogger(Id3v2Tag.class);
    private static final Properties FRAMEBODY_CLASSES = new Properties();

    static {
        try {
            FRAMEBODY_CLASSES.load(Id3v2Tag.class.getResourceAsStream("/de/codewave/camel/mp3/framebody.properties"));
        } catch (IOException e) {
            LOGGER.error("Could not read framebody class mappings.");
        }
    }

    private Header myHeader;
    private List<Frame> myFrames = new ArrayList<Frame>();

    public static Id3v2Tag createTag(InputStream stream) {
        Id3v2Tag tag = null;
        try {
            tag = new Id3v2Tag(new BufferedInputStream(stream));
        } catch (IOException e) {
            return null;
        } catch (IllegalHeaderException e) {
            return null;
        }
        return tag.getHeader() != null ? tag : null;
    }

    private Id3v2Tag(InputStream stream) throws IOException, IllegalHeaderException {
        myHeader = Header.createHeader(stream);
        if (myHeader != null) {
            if (myHeader.getBodySize() <= Mp3Utils.MAX_BUFFER_SIZE) {
                byte[] frameData = new byte[myHeader.getBodySize()];
                stream.read(frameData);
                myFrames = readFrames(frameData, getHeader().getVersion());
                if (getHeader().getVersion() == 4) {
                    readVersion4AsVersion3(frameData);
                }
            } else {
                throw new IllegalHeaderException("Maximum frame data size exceeded: " + myHeader.getBodySize());
            }
        }
    }

    private List<Frame> readFrames(byte[] frameData, int version) throws IOException {
        List<Frame> frames = new ArrayList<Frame>();
        Frame frame = null;
        int offset = 0;
        do {
            frame = Frame.createFrame(version, frameData, offset);
            if (frame != null && !"\u0000\u0000\u0000\u0000".equals(frame.getId())) {
                offset += frame.getFrameSize();
                if (frame.getBodySize() > 0) {
                    frames.add(frame);
                }
            }
        } while (frame != null && !"\u0000\u0000\u0000\u0000".equals(frame.getId()));
        return frames;
    }

    private void readVersion4AsVersion3(byte[] frameData) throws IOException {
        List<Frame> frames = readFrames(frameData, 3);
        if (myFrames.size() < frames.size() || (myFrames.size() == frames.size() && calcTotalSize(myFrames) < calcTotalSize(frames))) {
            myFrames = frames;// v2.3 style frames seem to be used in the v2.4 tag
        }
    }

    private long calcTotalSize(List<Frame> frames) {
        long total = 0;
        for (Frame frame : frames) {
            total += frame.getFrameSize();
        }
        return total;
    }

    public List<Frame> getFrames() {
        return new ArrayList<Frame>(myFrames);
    }

    public Frame getFrame(String id) {
        for (Frame frame : myFrames) {
            if (id.equals(frame.getId())) {
                return frame;
            }
        }
        return null;
    }

    public List<Frame> getFrames(String id) {
        List<Frame> frames = new ArrayList<Frame>();
        for (Frame frame : myFrames) {
            if (id.equals(frame.getId())) {
                frames.add(frame);
            }
        }
        return frames;
    }

    public Header getHeader() {
        return myHeader;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(myHeader.toString());
        for (Frame frame : myFrames) {
            buffer.append("\n").append(frame);
        }
        return buffer.toString();
    }

    public String getShortVersionIdentifier() {
        return myHeader.getShortVersionIdentifier();
    }

    public String getLongVersionIdentifier() {
        return myHeader.getLongVersionIdentifier();
    }

    public boolean isId3v1() {
        return false;
    }

    public boolean isId3v2() {
        return true;
    }

    public FrameBody getFrameBody(String v2FrameId, String v3FrameId) {
        if (myHeader.getVersion() == 2 && v2FrameId != null) {
            Frame frame = getFrame(v2FrameId);
            return frame != null ? createFrameBody(v2FrameId, frame) : null;
        } else if (myHeader.getVersion() > 2 && v3FrameId != null) {
            Frame frame = getFrame(v3FrameId);
            return frame != null ? createFrameBody(v3FrameId, frame) : null;
        }
        return null;
    }

    public List<FrameBody> getFrameBodies(String v2FrameId, String v3FrameId) {
        List<FrameBody> bodies = new ArrayList<FrameBody>();
        List<Frame> frames = null;
        String usedFrameId = null;
        if (myHeader.getVersion() == 2) {
            frames = getFrames(v2FrameId);
            usedFrameId = v2FrameId;
        } else if (myHeader.getVersion() > 2) {
            frames = getFrames(v3FrameId);
            usedFrameId = v3FrameId;
        }
        if (frames != null) {
            for (Frame frame : frames) {
                bodies.add(createFrameBody(usedFrameId, frame));
            }
        }
        return bodies;
    }

    public String getFrameBodyToString(String v2FrameId, String v3FrameId) {
        FrameBody frameBody = getFrameBody(v2FrameId, v3FrameId);
        return frameBody != null ? frameBody.toString() : null;
    }

    public String getFrameBodiesToString(String v2FrameId, String v3FrameId, String separator) {
        StringBuilder builder = new StringBuilder();
        for (FrameBody frameBody : getFrameBodies(v2FrameId, v3FrameId)) {
            builder.append(frameBody.toString()).append(separator);
        }
        return builder.substring(0, builder.length() - separator.length());
    }

    private FrameBody createFrameBody(String frameId, Frame frame) {
        String frameBodyClass = FRAMEBODY_CLASSES.getProperty(frameId);
        if (frameBodyClass != null && frameBodyClass.length() > 0) {
            try {
                return (FrameBody)Class.forName(frameBodyClass).getConstructor(Frame.class).newInstance(frame);
            } catch (Exception e) {
                LOGGER.error("Could not instantiate frame body class for frame \"" + frameId + "\".", e);
            }
        }
        return null;
    }

    public String getAlbum() {
        return getFrameBodyToString("TAL", "TALB");
    }
    
    public String getSortAlbum() {
        return getFrameBodyToString(null, "TSOA");
    }

    public String getArtist() {
        return getFrameBodyToString("TP1", "TPE1");
    }
    
    public String getSortArtist() {
        return getFrameBodyToString(null, "TSOP");
    }

    public String getAlbumArtist() {
        return getFrameBodyToString("TP2", "TPE2");
    }
    
    public String getSortAlbumArtist() {
        return getFrameBodyToString(null, "TSO2"); // iTunes specific tag
    }

    public String getTitle() {
        return getFrameBodyToString("TT2", "TIT2");
    }

    public int getTimeSeconds() {
        String value = getFrameBodyToString("TLE", "TLEN");
        if (value != null) {
            try {
                return Integer.parseInt(value.trim()) / 1000;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getComposer() {
        return getFrameBodyToString("TCM", "TCOM");
    }

    public int getTrackNumber() {
        String value = getFrameBodyToString("TRK", "TRCK");
        if (value != null) {
            int i = value.indexOf("/");
            if (i != -1) {
                value = value.substring(0, i);
            }
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getGenreAsString() {
        String value = getFrameBodyToString("TCO", "TCON");
        if (value != null && value.length() > 0) {
            if (value.startsWith("(") && !value.startsWith("((")) {
                int endBracket = value.indexOf(")");
                if (endBracket > 1) {
                    return Mp3Utils.translateGenre(value.substring(1, endBracket));
                }
            } else if (StringUtils.isNumeric(StringUtils.trimToNull(value))) {
                // this is against the spec but there are such ID3v2 tags out there
                String genre = Mp3Utils.translateGenre(StringUtils.trimToNull(value));
                if (StringUtils.isNotBlank(genre)) {
                    return genre;
                }
            }
        }
        return value != null ? value.trim() : null;
    }

    public String getYear() {
        return getFrameBodyToString("TYE", "TYER");
    }

    public String getPos() {
        return getFrameBodyToString("TPA", "TPOS");
    }
}
