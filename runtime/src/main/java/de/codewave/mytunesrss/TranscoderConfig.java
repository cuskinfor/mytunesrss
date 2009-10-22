/*
 * TranscoderConfig.java 07.05.2009
 *
 * Copyright (c) 2009 1&1 Internet AG. All rights reserved.
 *
 * $Id$
 */
package de.codewave.mytunesrss;

import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

public class TranscoderConfig {
    private String myName;

    private String myBinary;

    private String myOptions;

    private String mySuffixes;

    private String myTargetSuffix;

    private String myTargetContentType;

    private String[] mySuffixesSplitted;

    private String myMp4Codecs;

    private String[] myMp4CodecsSplitted;

    public TranscoderConfig() {
        // intentionally left blank
    }

    TranscoderConfig(JXPathContext context) {
        setName(JXPathUtils.getStringValue(context, "name", null));
        setBinary(JXPathUtils.getStringValue(context, "binary", null));
        setOptions(JXPathUtils.getStringValue(context, "options", null));
        setSuffixes(JXPathUtils.getStringValue(context, "suffixes", null));
        setMp4Codecs(JXPathUtils.getStringValue(context, "mp4codecs", null));
        setMp4Codecs(JXPathUtils.getStringValue(context, "mp4codecs", null));
        setTargetSuffix(JXPathUtils.getStringValue(context, "targetsuffix", null));
        setTargetContentType(JXPathUtils.getStringValue(context, "targetcontenttype", null));
    }

    public void writeTo(Document settings, Element config) {
        config.appendChild(DOMUtils.createTextElement(settings, "name", getName()));
        config.appendChild(DOMUtils.createTextElement(settings, "binary", getBinary()));
        config.appendChild(DOMUtils.createTextElement(settings, "options", getOptions()));
        config.appendChild(DOMUtils.createTextElement(settings, "suffixes", getSuffixes()));
        config.appendChild(DOMUtils.createTextElement(settings, "mp4codecs", getMp4Codecs()));
        config.appendChild(DOMUtils.createTextElement(settings, "targetsuffix", getTargetSuffix()));
        config.appendChild(DOMUtils.createTextElement(settings, "targetcontenttype", getTargetContentType()));
    }

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

    public String getSuffixes() {
        return mySuffixes;
    }

    public void setSuffixes(String suffixes) {
        mySuffixes = StringUtils.defaultString(StringUtils.remove(StringUtils.lowerCase(suffixes), ' '), "");
        mySuffixesSplitted = StringUtils.split(mySuffixes, ',');
    }

    public String getTargetSuffix() {
        return myTargetSuffix;
    }

    public void setTargetSuffix(String targetSuffix) {
        myTargetSuffix = targetSuffix;
    }

    public String getTargetContentType() {
        return myTargetContentType;
    }

    public void setTargetContentType(String targetContentType) {
        myTargetContentType = targetContentType;
    }

    public String getMp4Codecs() {
        return myMp4Codecs;
    }

    public void setMp4Codecs(String mp4Codecs) {
        myMp4Codecs = StringUtils.defaultString(StringUtils.remove(StringUtils.lowerCase(mp4Codecs), ' '), "");
        myMp4CodecsSplitted = StringUtils.split(myMp4Codecs, ',');
    }

    public boolean isValidBinary() {
        return StringUtils.isNotBlank(myBinary) && new File(myBinary).isFile();
    }

    public boolean isValidFor(String suffix, String mp4codec, MediaType mediaType) {
//        if (mediaType != MediaType.Audio) {
//            return false;
//        }
        if (ArrayUtils.contains(mySuffixesSplitted, StringUtils.trim(StringUtils.lowerCase(suffix)))) {
            if (StringUtils.isBlank(myMp4Codecs) || StringUtils.isBlank(mp4codec)) {
                return true;
            }
            return ArrayUtils.contains(myMp4CodecsSplitted, StringUtils.trim(StringUtils.lowerCase(mp4codec)));
        }
        return false;
    }
}
