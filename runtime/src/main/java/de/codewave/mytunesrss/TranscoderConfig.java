/*
 * TranscoderConfig.java 07.05.2009
 * 
 * Copyright (c) 2009 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package de.codewave.mytunesrss;

import java.io.File;

import org.apache.commons.lang.StringUtils;

public class TranscoderConfig {
    private String myName;

    private String myBinary;

    private String myOptions;

    private String myMimeType;

    private String myMp4Codecs;

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public String getBinary() {
        return myBinary;
    }

    public void setBinary(String binary) {
        myBinary = binary;
    }

    public String getOptions() {
        return myOptions;
    }

    public void setOptions(String options) {
        myOptions = options;
    }

    public String getMimeType() {
        return myMimeType;
    }

    public void setMimeType(String mimeType) {
        myMimeType = mimeType;
    }

    public String getMp4Codecs() {
        return myMp4Codecs;
    }

    public void setMp4Codecs(String mp4Codecs) {
        myMp4Codecs = mp4Codecs;
    }

    public boolean isValidBinary() {
        return StringUtils.isNotBlank(myBinary) && new File(myBinary).isFile();
    }
}
