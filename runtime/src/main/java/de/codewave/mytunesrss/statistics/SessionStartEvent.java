package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class SessionStartEvent extends AbstractEvent {
    @XmlElement(name = "user")
    public String myUser;

    @XmlElement(name = "sessionid")
    public String mySessionId;

    public SessionStartEvent() {
        // default constructor for JAXB
    }

    public SessionStartEvent(String user, String sessionId) {
        myUser = user;
        mySessionId = sessionId;
    }

    public StatEventType getType() {
        return StatEventType.SESSION_START;
    }
}