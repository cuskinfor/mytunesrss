/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config.transcoder;

import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProtectionTranscoderActivation extends TranscoderActivation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtectionTranscoderActivation.class);

    public ProtectionTranscoderActivation() {
        super(false);
    }

    public ProtectionTranscoderActivation(boolean negation) {
        super(negation);
    }

    @Override
    public boolean matches(Track track) {
        boolean b = applyNegation(track.isProtected());
        LOGGER.debug("Protection activation (negation \"" + isNegation() + "\") for \"" + track.getFilename() + "\": " + b);
        return b;
    }

    @Override
    public boolean isActive(Track track) {
        return true;
    }

    @Override
    public void writeTo(Document settings, Element config) {
        super.writeTo(settings, config);
    }

    @Override
    public void readFrom(JXPathContext config) {
        super.readFrom(config);
    }

}
