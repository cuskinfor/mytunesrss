/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of an EXIF field.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExifFieldRepresentation implements RestRepresentation {
    private String myName;
    private String myValue;

    public ExifFieldRepresentation() {
    }

    public ExifFieldRepresentation(String name, String value) {
        myName = name;
        myValue = value;
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
