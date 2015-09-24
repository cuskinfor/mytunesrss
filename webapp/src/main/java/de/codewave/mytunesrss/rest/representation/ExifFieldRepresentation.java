/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of an EXIF field.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExifFieldRepresentation implements RestRepresentation {
    /**
     * @exclude from swagger docs
     */
    private String myName;
    /**
     * @exclude from swagger docs
     */
    private String myValue;

    public ExifFieldRepresentation() {
    }

    public ExifFieldRepresentation(String name, String value) {
        if (IncludeExcludeInterceptor.isAttr("name")) {
            myName = name;
        }
        if (IncludeExcludeInterceptor.isAttr("value")) {
            myValue = value;
        }
    }

    /**
     * Name of the field.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * Value of the field.
     */
    public String getValue() {
        return myValue;
    }

    public void setValue(String value) {
        myValue = value;
    }
}
