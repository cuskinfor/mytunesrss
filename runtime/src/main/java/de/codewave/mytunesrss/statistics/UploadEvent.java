package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UploadEvent extends AbstractEvent {
    @XmlElement(name = "user")
    public String myUser;

    @XmlElement(name = "bytes")
    public long myBytes;

    public UploadEvent() {
        super(StatEventType.UPLOAD);
    }

    public UploadEvent(String user, long bytes) {
        super(StatEventType.UPLOAD);
        myUser = user;
        myBytes = bytes;
    }
}