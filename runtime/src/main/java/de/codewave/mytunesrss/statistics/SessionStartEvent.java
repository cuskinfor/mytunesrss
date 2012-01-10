package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SessionStartEvent extends AbstractEvent {
    @XmlElement(name = "user")
    public String myUser;

    @XmlElement(name = "sessionid")
    public String mySessionId;

    public SessionStartEvent() {
        super(StatEventType.SESSION_START);
    }

    public SessionStartEvent(String user, String sessionId) {
        super(StatEventType.SESSION_START);
        myUser = user;
        mySessionId = sessionId;
    }
}