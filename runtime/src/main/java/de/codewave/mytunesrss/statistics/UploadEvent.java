package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class UploadEvent extends AbstractEvent {
    @XmlElement(name = "user")
    public String myUser;

    @XmlElement(name = "bytes")
    public long myBytes;

    public UploadEvent() {
        // default constructor for JAXB
    }

    public UploadEvent(String user, long bytes) {
        myUser = user;
        myBytes = bytes;
    }

    public StatEventType getType() {
        return StatEventType.UPLOAD;
    }
}