package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class UploadEvent implements StatisticsEvent {
    @XmlElement(name = "user")
    public String myUser;
    @XmlElement(name = "bytes")
    public long myBytes;
    @XmlTransient
    public StatEventType getType() {
        return StatEventType.UPLOAD;
    }
}