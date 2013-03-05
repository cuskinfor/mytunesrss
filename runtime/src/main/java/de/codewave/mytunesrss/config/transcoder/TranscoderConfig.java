/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */
package de.codewave.mytunesrss.config.transcoder;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement
public class TranscoderConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscoderConfig.class);
    public static final Collection<TranscoderConfig> DEFAULT_TRANSCODERS = new HashSet<TranscoderConfig>();

    static {
        TranscoderConfig mp3Audio = new TranscoderConfig();
        mp3Audio.setName("MP3 Audio (128 kbit)");
        mp3Audio.setTranscoderActivations(Arrays.asList(
                new FilenameTranscoderActivation("^.+\\.(mp4|m4a|m4b|wav)$", false),
                new Mp4CodecTranscoderActivation("alac,mp4a", false)));
        mp3Audio.setTargetSuffix("mp3");
        mp3Audio.setTargetContentType("audio/mp3");
        mp3Audio.setTargetMux(null);
        mp3Audio.setOptions("acodec=mp3,ab=128,samplerate=44100,channels=2");
        DEFAULT_TRANSCODERS.add(mp3Audio);
        TranscoderConfig mp3Audio128 = new TranscoderConfig();
        mp3Audio128.setName("MP3 Audio (max 128 kbit)");
        mp3Audio128.setTranscoderActivations(Arrays.asList(
                new FilenameTranscoderActivation("^.+\\.(mp3|mp4|m4a|m4b|wav)$", false),
                new Mp3BitRateTranscoderActivation(0, 128000, true),
                new Mp4CodecTranscoderActivation("alac,mp4a", false)));
        mp3Audio128.setTargetSuffix("mp3");
        mp3Audio128.setTargetContentType("audio/mp3");
        mp3Audio128.setTargetMux(null);
        mp3Audio128.setOptions("acodec=mp3,ab=128,samplerate=44100,channels=2");
        DEFAULT_TRANSCODERS.add(mp3Audio128);
    }

    private String myName;

    private String myOptions;

    private List<TranscoderActivation> myTranscoderActivations = new ArrayList<TranscoderActivation>();

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
        List<TranscoderActivation> activations = new ArrayList<TranscoderActivation>();
        Iterator<JXPathContext> activationContextIterator = JXPathUtils.getContextIterator(context, "activations/activation");
        while (activationContextIterator.hasNext()) {
            JXPathContext activationContext = activationContextIterator.next();
            String type = JXPathUtils.getStringValue(activationContext, "type", null);
            try {
                TranscoderActivation activation = Activation.valueOf(type).newActivationInstance();
                activation.readFrom(activationContext);
                activations.add(activation);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Ignoring activation type \"" + type + "\".", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("Ignoring activation type \"" + type + "\".", e);
            } catch (InstantiationException e) {
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
        return new ArrayList<TranscoderActivation>(myTranscoderActivations);
    }

    public void setTranscoderActivations(List<TranscoderActivation> transcoderActivations) {
        myTranscoderActivations = new ArrayList<TranscoderActivation>(transcoderActivations);
    }

    public boolean isValidFor(Track track) {
        for (TranscoderActivation activation : myTranscoderActivations) {
            if (!activation.matches(track)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return StringUtils.isNotBlank(myName) ? myName : super.toString();
    }
}
