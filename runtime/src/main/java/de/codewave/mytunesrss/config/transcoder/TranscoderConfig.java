/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */
package de.codewave.mytunesrss.config.transcoder;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement
public class TranscoderConfig implements Cloneable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscoderConfig.class);
    private static final Collection<TranscoderConfig> DEFAULT_TRANSCODERS = new HashSet<>();
    private static final Collection<TranscoderConfig> MEDIA_SERVER_TRANSCODERS = new HashSet<>();
    private static final TranscoderConfig MEDIA_SERVER_MP3_128;
    private static final TranscoderConfig MEDIA_SERVER_MP3_192;
    private static final TranscoderConfig MEDIA_SERVER_MP3_320;
    private static final TranscoderConfig MEDIA_SERVER_MPEG2_VIDEO;

    static {
        MEDIA_SERVER_MP3_128 = new TranscoderConfig();
        MEDIA_SERVER_MP3_128.setName("_MSA128");
        MEDIA_SERVER_MP3_128.setTranscoderActivations(Arrays.asList(
                new MediaTypeTranscoderActivation(Arrays.asList(MediaType.Audio), false),
                new FilenameTranscoderActivation("^.+\\.mp3$", true),
                new Mp3BitRateTranscoderActivation(0, 131072, true),
                new Mp3BitRateTranscoderActivation(196608, Integer.MAX_VALUE, true),
                new Mp4CodecTranscoderActivation("alac,mp4a", false)));
        MEDIA_SERVER_MP3_128.setTargetSuffix("mp3");
        MEDIA_SERVER_MP3_128.setTargetContentType("audio/mpeg");
        MEDIA_SERVER_MP3_128.setTargetMux(null);
        MEDIA_SERVER_MP3_128.setOptions("acodec=mp3,ab=128,samplerate=44100,channels=2");
        MEDIA_SERVER_MP3_192 = new TranscoderConfig();
        MEDIA_SERVER_MP3_192.setName("_MSA192");
        MEDIA_SERVER_MP3_192.setTranscoderActivations(Arrays.asList(
                new MediaTypeTranscoderActivation(Arrays.asList(MediaType.Audio), false),
                new FilenameTranscoderActivation("^.+\\.mp3$", true),
                new Mp3BitRateTranscoderActivation(0, 196608, true),
                new Mp3BitRateTranscoderActivation(327680, Integer.MAX_VALUE, true),
                new Mp4CodecTranscoderActivation("alac,mp4a", false)));
        MEDIA_SERVER_MP3_192.setTargetSuffix("mp3");
        MEDIA_SERVER_MP3_192.setTargetContentType("audio/mpeg");
        MEDIA_SERVER_MP3_192.setTargetMux(null);
        MEDIA_SERVER_MP3_192.setOptions("acodec=mp3,ab=192,samplerate=44100,channels=2");
        MEDIA_SERVER_MP3_320 = new TranscoderConfig();
        MEDIA_SERVER_MP3_320.setName("_MSA256");
        MEDIA_SERVER_MP3_320.setTranscoderActivations(Arrays.asList(
                new MediaTypeTranscoderActivation(Arrays.asList(MediaType.Audio), false),
                new FilenameTranscoderActivation("^.+\\.mp3$", true),
                new Mp3BitRateTranscoderActivation(0, 327680, true),
                new Mp4CodecTranscoderActivation("alac,mp4a", false)));
        MEDIA_SERVER_MP3_320.setTargetSuffix("mp3");
        MEDIA_SERVER_MP3_320.setTargetContentType("audio/mpeg");
        MEDIA_SERVER_MP3_320.setTargetMux(null);
        MEDIA_SERVER_MP3_320.setOptions("acodec=mp3,ab=320,samplerate=44100,channels=2");
        MEDIA_SERVER_MPEG2_VIDEO = new TranscoderConfig();
        MEDIA_SERVER_MPEG2_VIDEO.setName("_MSVMPG2");
        MEDIA_SERVER_MPEG2_VIDEO.setTranscoderActivations(Collections.singletonList(new MediaTypeTranscoderActivation(Arrays.asList(MediaType.Video), false)));
        MEDIA_SERVER_MPEG2_VIDEO.setTargetSuffix("m2v");
        MEDIA_SERVER_MPEG2_VIDEO.setTargetContentType("video/mp2v");
        MEDIA_SERVER_MPEG2_VIDEO.setTargetMux("ts{use-key-frames}");
        MEDIA_SERVER_MPEG2_VIDEO.setOptions("venc=ffmpeg,vcodec=mp2v,vb=4096,acodec=mp3,ab=128,samplerate=44100,channels=2,deinterlace,audio-sync");
        MEDIA_SERVER_TRANSCODERS.add(MEDIA_SERVER_MP3_128);
        MEDIA_SERVER_TRANSCODERS.add(MEDIA_SERVER_MPEG2_VIDEO);
    }

    static {
        TranscoderConfig mp3Audio = new TranscoderConfig();
        mp3Audio.setName("MP3 Audio 128");
        mp3Audio.setTranscoderActivations(Arrays.asList(
                new FilenameTranscoderActivation("^.+\\.(mp4|m4a|m4b|wav)$", false),
                new Mp4CodecTranscoderActivation("alac,mp4a", false)));
        mp3Audio.setTargetSuffix("mp3");
        mp3Audio.setTargetContentType("audio/mpeg");
        mp3Audio.setTargetMux(null);
        mp3Audio.setOptions("acodec=mp3,ab=128,samplerate=44100,channels=2");
        DEFAULT_TRANSCODERS.add(mp3Audio);
        TranscoderConfig mp3Audio128 = new TranscoderConfig();
        mp3Audio128.setName("MP3 Audio max128");
        mp3Audio128.setTranscoderActivations(Arrays.asList(
                new FilenameTranscoderActivation("^.+\\.(mp3|mp4|m4a|m4b|wav)$", false),
                new Mp3BitRateTranscoderActivation(0, 131072, true),
                new Mp4CodecTranscoderActivation("alac,mp4a", false)));
        mp3Audio128.setTargetSuffix("mp3");
        mp3Audio128.setTargetContentType("audio/mpeg");
        mp3Audio128.setTargetMux(null);
        mp3Audio128.setOptions("acodec=mp3,ab=128,samplerate=44100,channels=2");
        DEFAULT_TRANSCODERS.add(mp3Audio128);
    }

    public static Collection<TranscoderConfig> getDefaultTranscoders() {
        Set<TranscoderConfig> deepClone = new HashSet<>(DEFAULT_TRANSCODERS.size());
        for (TranscoderConfig config : DEFAULT_TRANSCODERS) {
            try {
                deepClone.add((TranscoderConfig) config.clone());
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Could not clone transcoder configuration.", e);
            }
        }
        return deepClone;
    }

    public static ImmutableCollection<TranscoderConfig> getMediaServerTranscoders() {
        return ImmutableSet.copyOf(MEDIA_SERVER_TRANSCODERS);
    }

    private String myName;

    private String myOptions;

    private List<TranscoderActivation> myTranscoderActivations = new ArrayList<>();

    private String myTargetSuffix;

    private String myTargetContentType;

    private String myTargetMux;

    public TranscoderConfig() {
        // intentionally left blank
    }

    public TranscoderConfig(JXPathContext context) {
        setName(JXPathUtils.getStringValue(context, "name", null));
        setOptions(JXPathUtils.getStringValue(context, "options", null));
        setTargetSuffix(JXPathUtils.getStringValue(context, "targetsuffix", null));
        setTargetContentType(JXPathUtils.getStringValue(context, "targetcontenttype", null));
        setTargetMux(JXPathUtils.getStringValue(context, "targetmux", null));
        List<TranscoderActivation> activations = new ArrayList<>();
        Iterator<JXPathContext> activationContextIterator = JXPathUtils.getContextIterator(context, "activations/activation");
        while (activationContextIterator.hasNext()) {
            JXPathContext activationContext = activationContextIterator.next();
            String type = JXPathUtils.getStringValue(activationContext, "type", null);
            try {
                TranscoderActivation activation = Activation.valueOf(type).newActivationInstance();
                activation.readFrom(activationContext);
                activations.add(activation);
            } catch (IllegalArgumentException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Ignoring activation type \"" + type + "\".", e);
            }
        }
        if (activations.isEmpty()) {
            // read old version pattern and mp4 codecs
            String oldPattern = JXPathUtils.getStringValue(context, "pattern", "");
            String oldMp4Codecs = JXPathUtils.getStringValue(context, "mp4codecs", "");
            activations.add(new FilenameTranscoderActivation(oldPattern, false));
            activations.add(new Mp4CodecTranscoderActivation(oldMp4Codecs, false));
        }
        setTranscoderActivations(activations);
    }

    public void writeTo(Document settings, Element config) {
        config.appendChild(DOMUtils.createTextElement(settings, "name", getName()));
        config.appendChild(DOMUtils.createTextElement(settings, "options", getOptions()));
        config.appendChild(DOMUtils.createTextElement(settings, "targetsuffix", getTargetSuffix()));
        config.appendChild(DOMUtils.createTextElement(settings, "targetcontenttype", getTargetContentType()));
        config.appendChild(DOMUtils.createTextElement(settings, "targetmux", getTargetMux()));
        Element activations = settings.createElement("activations");
        config.appendChild(activations);
        for (TranscoderActivation activation : getTranscoderActivations()) {
            Element activationElement = settings.createElement("activation");
            activations.appendChild(activationElement);
            activation.writeTo(settings, activationElement);
        }
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

    public List<TranscoderActivation> getTranscoderActivations() {
        return new ArrayList<>(myTranscoderActivations);
    }

    public void setTranscoderActivations(List<? extends TranscoderActivation> transcoderActivations) {
        myTranscoderActivations = new ArrayList<>(transcoderActivations);
    }

    public boolean isValidFor(Track track) {
        boolean active = false;
        List<TranscoderActivation> activations = new ArrayList<>(myTranscoderActivations);
        activations.add(new ProtectionTranscoderActivation(true)); // never activate for protected tracks
        for (TranscoderActivation activation : activations) {
            if (activation.isActive(track)) {
                active = true;
                if (!activation.matches(track)) {
                    return false; // all active activations have to match
                }
            }
        }
        return active; // only TRUE if at least one activation was active
    }

    @Override
    public String toString() {
        return StringUtils.isNotBlank(myName) ? myName : super.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        TranscoderConfig clone = (TranscoderConfig) super.clone();
        clone.myTranscoderActivations = new ArrayList<>();
        for (TranscoderActivation activation : myTranscoderActivations) {
            clone.myTranscoderActivations.add((TranscoderActivation) activation.clone());
        }
        return clone;
    }

    public String getCacheFilePrefix() {
        return StringUtils.replaceChars(myName, ' ', '_');
    }
}
