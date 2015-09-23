package de.codewave.camel.mp4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TextAtom extends Mp4Atom {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextAtom.class);

    TextAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    public String getText() {
        String data = getFirstChild("data").getDataAsString(8, "UTF-8");
        LOGGER.debug("Text from \"" + getId() + "\" is \"" + data + "\".");
        return data;
    }
}
