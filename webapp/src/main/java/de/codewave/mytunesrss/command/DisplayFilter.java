package de.codewave.mytunesrss.command;

import org.apache.commons.lang.StringUtils;
import de.codewave.mytunesrss.MediaType;

/**
 * de.codewave.mytunesrss.command.DisplayFilter
 */
public class DisplayFilter {
    enum Protection {
        Protected, Unprotected, All
    }

    private String myTextFilter;
    private MediaType myMediaType;
    private Protection myProtection;
    private int myMinYear = -1;
    private int myMaxYear = -1;

    public Protection getProtection() {
        return myProtection;
    }

    public void setProtection(Protection protection) {
        myProtection = protection;
    }

    public String getTextFilter() {
        return StringUtils.isNotEmpty(myTextFilter) ? myTextFilter : null;
    }

    public void setTextFilter(String textFilter) {
        myTextFilter = textFilter;
    }

    public MediaType getMediaType() {
        return myMediaType;
    }

    public void setMediaType(MediaType mediaType) {
        myMediaType = mediaType;
    }

    public int getMinYear() {
        return myMinYear;
    }

    public void setMinYear(int minYear) {
        myMinYear = Math.max(minYear, -1);
    }

    public int getMaxYear() {
        return myMaxYear;
    }

    public void setMaxYear(int maxYear) {
        myMaxYear = Math.max(maxYear, -1);
    }
}
