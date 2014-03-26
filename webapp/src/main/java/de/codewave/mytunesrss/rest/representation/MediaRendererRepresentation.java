/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.bonjour.BonjourDevice;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.fourthline.cling.model.meta.RemoteDevice;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Media renderer description.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MediaRendererRepresentation implements RestRepresentation {

    private String myId;
    private String myName;

    public MediaRendererRepresentation() {
    }

    public MediaRendererRepresentation(RemoteDevice device) {
        if (IncludeExcludeInterceptor.isAttr("id")) {
            myId = device.getIdentity().getUdn().getIdentifierString();
        }
        if (IncludeExcludeInterceptor.isAttr("name")) {
            myName = device.getDetails().getFriendlyName();
        }
    }

    /**
     * The ID of the media renderer (UPnP device UDN).
     */
    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    /**
     * The name of the media renderer (UPnP device friendly name).
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

}
