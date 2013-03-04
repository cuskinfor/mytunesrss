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

public class Mp3SampleRateTranscoderActivation extends TranscoderActivation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp3SampleRateTranscoderActivation.class);

    private int myMinSampleRate;
    private int myMaxSampleRate;

    public Mp3SampleRateTranscoderActivation() {
        super(false);
    }

    public Mp3SampleRateTranscoderActivation(int minSampleRate, int maxSampleRate, boolean negation) {
        super(negation);
        myMinSampleRate = minSampleRate;
        myMaxSampleRate = maxSampleRate;
    }

    @Override
    public boolean matches(Track track) {
        if (FileSupportUtils.isMp3(track.getFile())) {
            try {
                FileInputStream inputStream = new FileInputStream(track.getFile());
                try {
                    Mp3Info mp3Info = Mp3Utils.getMp3Info(inputStream);
                    return applyNegation(myMinSampleRate <= mp3Info.getMinSampleRate() && myMaxSampleRate >= mp3Info.getMaxSampleRate());
                } finally {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Could not get mp3 info for track \"" + track.getFilename() + "\".", e);
            } catch (Mp3Exception e) {
                LOGGER.warn("Could not get mp3 info for track \"" + track.getFilename() + "\".", e);
            }
        }
        return true;
    }

    @Override
    public void writeTo(Document settings, Element config) {
        super.writeTo(settings, config);
        config.appendChild(DOMUtils.createIntElement(settings, "min", myMinSampleRate));
        config.appendChild(DOMUtils.createIntElement(settings, "max", myMaxSampleRate));
    }

    @Override
    public void readFrom(JXPathContext config) {
        super.readFrom(config);
        myMinSampleRate = JXPathUtils.getIntValue(config, "min", 0);
        myMaxSampleRate = JXPathUtils.getIntValue(config, "max", Integer.MAX_VALUE);
    }
}
