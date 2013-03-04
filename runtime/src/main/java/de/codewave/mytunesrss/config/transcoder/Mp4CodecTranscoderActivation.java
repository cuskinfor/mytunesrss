package de.codewave.mytunesrss.config.transcoder;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Mp4CodecTranscoderActivation extends TranscoderActivation {

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
        return applyNegation(FileSupportUtils.isMp4(track.getFile()) && ArrayUtils.contains(myCodecs, StringUtils.lowerCase(track.getMp4Codec())));
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
}
