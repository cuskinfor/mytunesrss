/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class MessageOfTheDayItem {

    @XmlAttribute(name = "min-version")
    private String minVersion;

    @XmlAttribute(name = "max-version")
    private String maxVersion;

    @XmlAttribute(name = "lang")
    private String language;

    @XmlValue
    private String value;

    public String getMinVersion() {
        return minVersion;
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    public String getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }
}
