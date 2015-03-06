package de.codewave.mytunesrss.statistics;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;

public abstract class AbstractEvent implements StatisticsEvent {
    private long myEventTime = System.currentTimeMillis();
    private StatEventType myEventType;

    protected AbstractEvent(StatEventType eventType) {
        myEventType = eventType;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public long getEventTime() {
        return myEventTime;
    }

    public void setEventTime(long eventTime) {
        myEventTime = eventTime;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public StatEventType getType() {
        return myEventType;
    }

    @Override
    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().withAnnotationIntrospector(new JaxbAnnotationIntrospector());
        return mapper.writeValueAsString(this);
    }
}
