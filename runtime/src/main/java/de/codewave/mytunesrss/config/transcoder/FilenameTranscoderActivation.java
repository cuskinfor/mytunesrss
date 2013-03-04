package de.codewave.mytunesrss.config.transcoder;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.regex.Pattern;

public class FilenameTranscoderActivation extends TranscoderActivation {

    private String myPattern;
    private Pattern myCompiledPattern;

    public FilenameTranscoderActivation() {
        super(false);
    }

    public FilenameTranscoderActivation(String pattern, boolean negation) {
        super(negation);
        myPattern = pattern;
        myCompiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean matches(Track track) {
        return applyNegation(myCompiledPattern.matcher(track.getFilename()).matches());
    }

    @Override
    public void writeTo(Document settings, Element config) {
        super.writeTo(settings, config);
        config.appendChild(DOMUtils.createTextElement(settings, "pattern", myPattern));
    }

    @Override
    public void readFrom(JXPathContext config) {
        super.readFrom(config);
        myPattern = JXPathUtils.getStringValue(config, "pattern", "");
    }

}
