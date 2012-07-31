/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Representation of a photo's EXIF data.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExifRepresentation {
    private List<ExifFieldRepresentation> myFields;

    public ExifRepresentation() {
    }

    public ExifRepresentation(List<ExifFieldRepresentation> fields) {
        myFields = fields;
    }

    /**
     * List of EXIF fields.
     */
    public List<ExifFieldRepresentation> getFields() {
        return myFields;
    }

    public void setFields(List<ExifFieldRepresentation> fields) {
        myFields = fields;
    }
}
