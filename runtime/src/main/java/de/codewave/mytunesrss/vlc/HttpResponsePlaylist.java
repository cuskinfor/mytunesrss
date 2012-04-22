/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.vlc;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class HttpResponsePlaylist {
    private String myType;
    private String myName;
    private String myId;
    private int myDuration;
    private String myUri;
    private String myCurrent;
    private List<HttpResponsePlaylist> myChildren;

    public String getType() {
        return myType;
    }

    public void setType(String type) {
        myType = type;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public int getDuration() {
        return myDuration;
    }

    public void setDuration(int duration) {
        myDuration = duration;
    }

    public String getUri() {
        return myUri;
    }

    public void setUri(String uri) {
        myUri = uri;
    }

    public String getCurrent() {
        return myCurrent;
    }

    public void setCurrent(String current) {
        myCurrent = current;
    }

    public List<HttpResponsePlaylist> getChildren() {
        return myChildren;
    }

    public void setChildren(List<HttpResponsePlaylist> children) {
        myChildren = children;
    }
}
