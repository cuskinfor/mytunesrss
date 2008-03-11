package de.codewave.mytunesrss.command;

import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.command.DisplayFilter
 */
public class DisplayFilter {
    enum Type {
        Audio, Video, All
    }

    enum Protection {
        Protected, Unprotected, All
    }

    private String myTextFilter;
    private Type myType;
    private Protection myProtection;

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

    public Type getType() {
        return myType;
    }

    public void setType(Type type) {
        myType = type;
    }
}