package de.codewave.mytunesrss.config.transcoder;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.xml.DOMUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.regex.Pattern;

public class FilenameTranscoderActivation extends TranscoderActivation {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilenameTranscoderActivation.class);

    private String myPattern;
    private Pattern myCompiledPattern;

    public FilenameTranscoderActivation() {
        super(false);
    }

    public FilenameTranscoderActivation(String pattern, boolean negation) {
        super(negation);
        myPattern = pattern;
        compilePattern();
    }

    private void compilePattern() {
        myCompiledPattern = Pattern.compile(myPattern, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean matches(Track track) {
        boolean b = applyNegation(myCompiledPattern.matcher(track.getFilename()).matches());
        LOGGER.debug("Filename activation (pattern \"" + myPattern + "\", negation \"" + isNegation() + "\") for \"" + track.getFilename() + "\": " + b);
        return b;
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
        compilePattern();
    }

    public String getPattern() {
        return myPattern;
    }
}
