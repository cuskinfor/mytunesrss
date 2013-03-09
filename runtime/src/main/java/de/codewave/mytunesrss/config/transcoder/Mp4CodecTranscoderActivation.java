package de.codewave.mytunesrss.config.transcoder;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;

public class Mp4CodecTranscoderActivation extends TranscoderActivation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp4CodecTranscoderActivation.class);

    private String[] myCodecs;

    public Mp4CodecTranscoderActivation() {
        super(false);
    }

    public Mp4CodecTranscoderActivation(String codecs, boolean negation) {
        super(negation);
        myCodecs = StringUtils.split(StringUtils.remove(StringUtils.lowerCase(codecs), ' '), ',');
    }

    @Override
    public boolean matches(Track track) {
        boolean b;
        String trackMp4Codec = StringUtils.trimToEmpty(StringUtils.lowerCase(track.getMp4Codec()));
        if (StringUtils.isNotBlank(trackMp4Codec)) {
            b = applyNegation(ArrayUtils.contains(myCodecs, trackMp4Codec));
        } else {
            b = applyNegation(false);
        }
        LOGGER.debug("MP4 codec activation (codecs \"" + Arrays.toString(myCodecs) + "\", negation \"" + isNegation() + "\") for \"" + track.getFilename() + "\": " + b);
        return b;
    }

    @Override
    public boolean isActive(Track track) {
        return FileSupportUtils.isMp4(track.getFile());
    }

    @Override
    public void writeTo(Document settings, Element config) {
        super.writeTo(settings, config);
        config.appendChild(DOMUtils.createTextElement(settings, "codecs", StringUtils.join(myCodecs, ',')));
    }

    @Override
    public void readFrom(JXPathContext config) {
        super.readFrom(config);
        myCodecs = StringUtils.split(JXPathUtils.getStringValue(config, "codecs", ""), ',');
    }

    public String[] getCodecs() {
        return myCodecs != null ? myCodecs.clone() : null;
    }
}
