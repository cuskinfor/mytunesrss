/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.musicfile;

import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;

/**
 * de.codewave.mytunesrss.musicfile.MusicFile
 */
public class MusicFile implements Serializable {
    private static final Log LOG = LogFactory.getLog(MusicFile.class);

    private String myAlbum;
    private String myArtist;
    private File myFile;
    private String myName;
    private String myId;
    private int myTrackNumber;
    private String myVirtualFileName;
    private String myFakeMp3Suffix;
    private String myFakeM4aSuffix;


    public MusicFile(String fakeMp3Suffix, String fakeM4aSuffix) {
        myFakeMp3Suffix = fakeMp3Suffix;
        myFakeM4aSuffix = fakeM4aSuffix;
    }

    public String getAlbum() {
        return myAlbum;
    }

    public synchronized void setAlbum(String album) {
        myAlbum = album;
        myVirtualFileName = null;
    }

    public String getArtist() {
        return myArtist;
    }

    public synchronized void setArtist(String artist) {
        myArtist = artist;
        myVirtualFileName = null;
    }

    public File getFile() {
        return myFile;
    }

    public long getFileLength() {
        return myFile != null && myFile.exists() && myFile.isFile() ? myFile.length() : 0;
    }

    public void setFile(File file) {
        myFile = file;
    }

    public String getName() {
        return myName;
    }

    public synchronized void setName(String name) {
        myName = name;
        myVirtualFileName = null;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public int getTrackNumber() {
        return myTrackNumber;
    }

    public String getTextualTrackNumber() {
        if (myTrackNumber > 9) {
            return "" + myTrackNumber;
        } else if (myTrackNumber > 0) {
            return "0" + myTrackNumber;
        }
        return null;
    }

    public synchronized void setTrackNumber(int trackNumber) {
        myTrackNumber = trackNumber;
        myVirtualFileName = null;
    }

    @Override
    public String toString() {
        return String.format("Title '%s' on album '%s' (track %d) by artist '%s' with id '%s' can be found at '%s'",
                             myName,
                             myAlbum,
                             myTrackNumber,
                             myArtist,
                             myId,
                             myFile != null ? myFile.getAbsolutePath() : "?unknown location?");
    }

    public boolean isValid() {
        return (myName != null && myArtist != null && myId != null && myAlbum != null && myFile != null && (isMP3() || isM4A()));
    }

    private boolean isMP3() {
        String name = myFile.getName();
        return StringUtils.isNotEmpty(name) && name.toLowerCase().endsWith(".mp3");
    }

    private boolean isM4A() {
        String name = myFile.getName();
        return StringUtils.isNotEmpty(name) && name.toLowerCase().endsWith(".m4a");
    }

    public synchronized String getVirtualFileName() {
        if (myVirtualFileName == null) {
            myVirtualFileName = myArtist + " - " + myAlbum + " - ";
            String trackNumber = getTextualTrackNumber();
            if (trackNumber != null) {
                myVirtualFileName += trackNumber + " - ";
            }
            String suffix = myFile.getName().substring(myFile.getName().lastIndexOf(".") + 1);
            if ("mp3".equals(suffix.toLowerCase())) {
                if (StringUtils.isNotEmpty(myFakeMp3Suffix)) {
                    suffix = myFakeMp3Suffix;
                }
            } else {
                if (StringUtils.isNotEmpty(myFakeM4aSuffix)) {
                    suffix = myFakeM4aSuffix;
                }
            }
            myVirtualFileName += myName + "." + suffix;
            myVirtualFileName = myVirtualFileName.replace(' ', '_');
        }
        return myVirtualFileName;
    }

    public String getContentType() {
        if (isMP3()) {
            return "audio/mp3";
        }
        return "audio/mp4";
    }

    public void setFile(String location) {
        if (location.toLowerCase().startsWith("file://localhost")) {
            try {
                String pathname = URLDecoder.decode(location.substring("file://localhost".length()), "UTF-8");
                File file = new File(pathname);
                if (file.exists() && file.isFile()) {
                    setFile(file);
                } else {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("File \"" + pathname + "\" omitted (not found or is not a file).");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create location for \"" + location + "\".", e);
                }
            }
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Location \"" + location + "\" not recognized.");
            }
        }
    }
}