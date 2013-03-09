package de.codewave.mytunesrss.config.transcoder;

import de.codewave.camel.mp3.Mp3Info;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp3.exception.Mp3Exception;
import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.io.IOException;

public class Mp3BitRateTranscoderActivation extends TranscoderActivation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp3BitRateTranscoderActivation.class);

    private int myMinBitRate;
    private int myMaxBitRate;

    public Mp3BitRateTranscoderActivation() {
        super(false);
    }

    public Mp3BitRateTranscoderActivation(int minBitRate, int maxBitRate, boolean negation) {
        super(negation);
        myMinBitRate = minBitRate;
        myMaxBitRate = maxBitRate;
    }

    @Override
    public boolean matches(Track track) {
        boolean b = false;
        try {
            FileInputStream inputStream = new FileInputStream(track.getFile());
            try {
                Mp3Info mp3Info = Mp3Utils.getMp3Info(inputStream);
                b = mp3Info.getAvgBitrate() == 0 || applyNegation(myMinBitRate <= mp3Info.getMinBitrate() && myMaxBitRate >= mp3Info.getMaxBitrate());
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Could not get mp3 info for track \"" + track.getFilename() + "\".", e);
        } catch (Mp3Exception e) {
            LOGGER.warn("Could not get mp3 info for track \"" + track.getFilename() + "\".", e);
        }
        LOGGER.debug("MP3 bitrate activation (min \"" + myMinBitRate + "\", max \"" + myMaxBitRate + "\", negation \"" + isNegation() + "\") for \"" + track.getFilename() + "\": " + b);
        return b;
    }

    @Override
    public boolean isActive(Track track) {
        return FileSupportUtils.isMp3(track.getFile());
    }

    @Override
    public void writeTo(Document settings, Element config) {
        super.writeTo(settings, config);
        config.appendChild(DOMUtils.createIntElement(settings, "min", myMinBitRate));
        config.appendChild(DOMUtils.createIntElement(settings, "max", myMaxBitRate));
    }

    @Override
    public void readFrom(JXPathContext config) {
        super.readFrom(config);
        myMinBitRate = JXPathUtils.getIntValue(config, "min", 0);
        myMaxBitRate = JXPathUtils.getIntValue(config, "max", Integer.MAX_VALUE);
    }

    public int getMinBitRate() {
        return myMinBitRate;
    }

    public int getMaxBitRate() {
        return myMaxBitRate;
    }
}
