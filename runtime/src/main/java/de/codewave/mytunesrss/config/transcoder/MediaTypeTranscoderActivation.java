/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config.transcoder;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.seamless.util.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class MediaTypeTranscoderActivation extends TranscoderActivation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaTypeTranscoderActivation.class);

    private List<MediaType> myMediaTypes;

    public MediaTypeTranscoderActivation() {
        super(false);
    }

    public MediaTypeTranscoderActivation(List<MediaType> mediaTypes, boolean negation) {
        super(negation);
        myMediaTypes = new ArrayList<>(mediaTypes);
    }

    @Override
    public boolean matches(Track track) {
        boolean b = applyNegation(myMediaTypes.contains(track.getMediaType()));
        LOGGER.debug("Media type activation (negation \"" + isNegation() + "\") for \"" + track.getFilename() + "\": " + b);
        return b;
    }

    @Override
    public boolean isActive(Track track) {
        return true;
    }

    @Override
    public void writeTo(Document settings, Element config) {
        super.writeTo(settings, config);
        if (myMediaTypes != null && !myMediaTypes.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (MediaType mediaType : myMediaTypes) {
                builder.append(",").append(mediaType);
            }
            config.appendChild(DOMUtils.createTextElement(settings, "mediatypes", builder.substring(1)));
        }
    }

    @Override
    public void readFrom(JXPathContext config) {
        super.readFrom(config);
        myMediaTypes = new ArrayList<>();
        for (String mediaType : StringUtils.split(JXPathUtils.getStringValue(config, "mediatypes", ""), ",")) {
            myMediaTypes.add(MediaType.valueOf(mediaType));
        }
    }

}
