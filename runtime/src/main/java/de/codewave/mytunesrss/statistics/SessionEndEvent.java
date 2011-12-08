package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class SessionEndEvent extends AbstractEvent {
    @XmlElement(name = "user")
    public String myUser;

    @XmlElement(name = "sessionid")
    public String mySessionId;

    @XmlElement(name = "duration")
    public long myDuration;

    public SessionEndEvent() {
        // default constructor for JAXB
    }

    public SessionEndEvent(String user, String sessionId) {
        myUser = user;
        mySessionId = sessionId;
    }

    public StatEventType getType() {
        return StatEventType.LOGIN;
    }
}