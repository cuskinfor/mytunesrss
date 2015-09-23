package de.codewave.camel.mp4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NumericAtom extends Mp4Atom {
    private static final Logger LOGGER = LoggerFactory.getLogger(NumericAtom.class);

    NumericAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    public long getValue() {
        byte[] data = getFirstChild("data").getData();
        long value = CamelUtils.getLongValue(data, 8, 4, false, Endianness.Big);
        LOGGER.debug("Value from \"" + getId() + "\" is \"" + value + "\".");
        return value;
    }
}
