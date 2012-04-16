/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */
package de.codewave.mytunesrss.config;

import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

@XmlRootElement
public class TranscoderConfig {

    public static final Collection<TranscoderConfig> DEFAULT_TRANSCODERS = new HashSet<TranscoderConfig>();

    static {
        TranscoderConfig mp3Audio = new TranscoderConfig();
        mp3Audio.setName("MP3 Audio");
        mp3Audio.setPattern("^.+\\.(mp4|m4a|m4b|wav)$");
        mp3Audio.setMp4Codecs("alac,mp4a");
        mp3Audio.setTargetSuffix("mp3");
        mp3Audio.setTargetContentType("audio/mp3");
        mp3Audio.setTargetMux(null);
        mp3Audio.setOptions("acodec=mp3,ab=128,samplerate=44100,channels=2");
        DEFAULT_TRANSCODERS.add(mp3Audio);
    }

    private String myName;

    private String myOptions;

    private String myPattern;

    private String myTargetSuffix;

    private String myTargetContentType;

    private String myTargetMux;

    private Pattern myCompiledPattern;

    private String myMp4Codecs;

    private String[] myMp4CodecsSplitted;

    public TranscoderConfig() {
        // intentionally left blank
    }

    public TranscoderConfig(JXPathContext context) {
        setName(JXPathUtils.getStringValue(context, "name", null));
        setOptions(JXPathUtils.getStringValue(context, "options", null));
        setPattern(JXPathUtils.getStringValue(context, "pattern", null));
        setMp4Codecs(JXPathUtils.getStringValue(context, "mp4codecs", null));
        setMp4Codecs(JXPathUtils.getStringValue(context, "mp4codecs", null));
        setTargetSuffix(JXPathUtils.getStringValue(context, "targetsuffix", null));
        setTargetContentType(JXPathUtils.getStringValue(context, "targetcontenttype", null));
        setTargetMux(JXPathUtils.getStringValue(context, "targetmux", null));
    }

    public void writeTo(Document settings, Element config) {
        config.appendChild(DOMUtils.createTextElement(settings, "name", getName()));
        config.appendChild(DOMUtils.createTextElement(settings, "options", getOptions()));
        config.appendChild(DOMUtils.createTextElement(settings, "pattern", getPattern()));
        config.appendChild(DOMUtils.createTextElement(settings, "mp4codecs", getMp4Codecs()));
        config.appendChild(DOMUtils.createTextElement(settings, "targetsuffix", getTargetSuffix()));
        config.appendChild(DOMUtils.createTextElement(settings, "targetcontenttype", getTargetContentType()));
        config.appendChild(DOMUtils.createTextElement(settings, "targetmux", getTargetMux()));
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public String getOptions() {
        return myOptions;
    }

    public void setOptions(String options) {
        myOptions = options;
    }

    public String getPattern() {
        return myPattern;
    }

    public void setPattern(String pattern) {
        myPattern = StringUtils.defaultString(pattern, "");
        myCompiledPattern = Pattern.compile(myPattern, Pattern.CASE_INSENSITIVE);
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

    public String getTargetMux() {
        return myTargetMux;
    }

    public void setTargetMux(String targetMux) {
        myTargetMux = targetMux;
    }

    public String getMp4Codecs() {
        return myMp4Codecs;
    }

    public void setMp4Codecs(String mp4Codecs) {
        myMp4Codecs = StringUtils.defaultString(StringUtils.remove(StringUtils.lowerCase(mp4Codecs), ' '), "");
        myMp4CodecsSplitted = StringUtils.split(myMp4Codecs, ',');
    }

    public boolean isValidFor(String file, String mp4codec) {
        if (myCompiledPattern.matcher(file).matches()) {
            if (StringUtils.isBlank(myMp4Codecs) || StringUtils.isBlank(mp4codec)) {
                return true;
            }
            return ArrayUtils.contains(myMp4CodecsSplitted, StringUtils.trim(StringUtils.lowerCase(mp4codec)));
        }
        return false;
    }

    @Override
    public String toString() {
        return StringUtils.isNotBlank(myName) ? myName : super.toString();
    }
}
