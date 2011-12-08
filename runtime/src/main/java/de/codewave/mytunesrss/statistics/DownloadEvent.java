package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class DownloadEvent extends AbstractEvent {
    @XmlElement(name = "user")
    public String myUser;

    @XmlElement(name = "bytes")
    public  long myBytes;

    public DownloadEvent() {
        // default constructor for JAXB
    }

    public DownloadEvent(String user, long bytes) {
        myUser = user;
        myBytes = bytes;
    }

    public StatEventType getType() {
        return StatEventType.LOGIN;
    }
}