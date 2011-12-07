package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class LoginEvent implements StatisticsEvent {
    @XmlElement(name = "user")
    public String myUser;
    @XmlTransient
    public StatEventType getType() {
        return StatEventType.LOGIN;
    }
}