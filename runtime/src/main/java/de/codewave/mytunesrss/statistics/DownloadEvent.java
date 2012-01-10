package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DownloadEvent extends AbstractEvent {
    @XmlElement(name = "user")
    public String myUser;

    @XmlElement(name = "bytes")
    public long myBytes;

    @XmlElement(name = "track_id")
    public String myTrackId;

    public DownloadEvent() {
        super(StatEventType.DOWNLOAD);
    }

    public DownloadEvent(String user, String trackId, long bytes) {
        super(StatEventType.DOWNLOAD);
        myUser = user;
        myTrackId = trackId;
        myBytes = bytes;
    }
}