package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.bonjour.BonjourDevice;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bonjour endpoint description.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class BonjourDeviceRepresentation implements RestRepresentation {

    private String myId;
    private String myName;
    private String myHost;
    private Integer myPort;

    public BonjourDeviceRepresentation() {
    }

    public BonjourDeviceRepresentation(BonjourDevice bonjourDevice) {
        myId = bonjourDevice.getId();
        myName = bonjourDevice.getName();
        myHost = bonjourDevice.getInetAddress().getCanonicalHostName();
        myPort = bonjourDevice.getPort();
    }

    /**
     * The ID of the endpoint.
     */
    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    /**
     * The name of the endpoint.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * The host name of the endpoint.
     */
    public String getHost() {
        return myHost;
    }

    public void setHost(String host) {
        myHost = host;
    }

    /**
     * The port of the endpoint.
     */
    public Integer getPort() {
        return myPort;
    }

    public void setPort(Integer port) {
        myPort = port;
    }
}
