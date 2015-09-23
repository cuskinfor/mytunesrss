package de.codewave.camel.mp4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class YearAtom extends TextAtom {
    YearAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    public Integer getYear() {
        String text = getText();
        if (StringUtils.isNotBlank(text)) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return Integer.parseInt(text.substring(0, StringUtils.indexOfAnyBut(text, "1234567890")));
            }
        }
        return null;
    }

}
