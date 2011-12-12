package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class DownloadEvent extends AbstractEvent {
    @XmlElement(name = "user")
    public String myUser;

    @XmlElement(name = "bytes")
    public long myBytes;

    @XmlElement(name = "track_id")
    public String myTrackId;

    public DownloadEvent() {
        // default constructor for JAXB
    }

    public DownloadEvent(String user, String trackId, long bytes) {
        myUser = user;
        myTrackId = trackId;
        myBytes = bytes;
    }

    public StatEventType getType() {
        return StatEventType.DOWNLOAD;
    }
}