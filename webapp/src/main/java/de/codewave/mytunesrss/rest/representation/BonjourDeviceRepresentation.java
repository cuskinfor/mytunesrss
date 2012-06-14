package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.bonjour.BonjourDevice;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BonjourDeviceRepresentation extends BonjourDevice {

    public BonjourDeviceRepresentation(BonjourDevice bonjourDevice) {
        super(bonjourDevice.getName(), bonjourDevice.getInetAddress(), bonjourDevice.getPort());
    }
}
