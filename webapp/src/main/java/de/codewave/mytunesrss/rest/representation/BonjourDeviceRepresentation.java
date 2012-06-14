package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.bonjour.BonjourDevice;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bonjour endpoint description.
 */
@XmlRootElement
public class BonjourDeviceRepresentation {

    private String myId;
    private String myName;
    private String myHost;
    private int myPort;

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
    public int getPort() {
        return myPort;
    }

    public void setPort(int port) {
        myPort = port;
    }
}
