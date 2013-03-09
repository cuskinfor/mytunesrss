package de.codewave.mytunesrss.config.transcoder;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class TranscoderActivation implements Cloneable {

    private boolean myNegation;

    public TranscoderActivation(boolean negation) {
        myNegation = negation;
    }

    protected boolean applyNegation(boolean value) {
        return myNegation ? !value : value;
    }

    public abstract boolean matches(Track track);

    public abstract boolean isActive(Track track);

    public void writeTo(Document settings, Element config) {
        config.appendChild(DOMUtils.createBooleanElement(settings, "negation", myNegation));
        config.appendChild(DOMUtils.createTextElement(settings, "type", Activation.forActivation(this).name()));
    }

    public void readFrom(JXPathContext config) {
        myNegation = JXPathUtils.getBooleanValue(config, "negation", false);
    }

    public boolean isNegation() {
        return myNegation;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
