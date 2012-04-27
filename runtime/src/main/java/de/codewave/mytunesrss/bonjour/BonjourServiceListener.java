/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.bonjour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class BonjourServiceListener implements ServiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BonjourServiceListener.class);

    private ConcurrentHashMap<String, BonjourDevice> myDevices = new ConcurrentHashMap<String, BonjourDevice>();

    public void serviceAdded(ServiceEvent event) {
        LOGGER.debug("Bonjour service added.");
        ServiceInfo serviceInfo = event.getInfo();
        if (serviceInfo == null || serviceInfo.getInetAddress() == null) {
            serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName(), 2000);
        }
        BonjourDevice device = new BonjourDevice(event.getName(), serviceInfo.getInetAddress(), serviceInfo.getPort());
        LOGGER.debug("Adding device with id \"" + device.getId() + "\".");
        myDevices.putIfAbsent(device.getId(), device);
    }

    public void serviceRemoved(ServiceEvent event) {
        removeDevice(event);
    }

    protected BonjourDevice removeDevice(ServiceEvent event) {
        LOGGER.debug("Bonjour service removed.");
        ServiceInfo serviceInfo = event.getInfo();
        if (serviceInfo == null || serviceInfo.getInetAddress() == null) {
            serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName(), 2000);
        }
        String id = new BonjourDevice(event.getName(), serviceInfo.getInetAddress(), serviceInfo.getPort()).getId();
        LOGGER.debug("Adding device with id \"" + id + "\".");
        return myDevices.remove(id);
    }

    public void serviceResolved(ServiceEvent event) {
        LOGGER.debug("Bonjour service resolved.");
    }

    public Collection<BonjourDevice> getDevices() {
        return new HashSet<BonjourDevice>(myDevices.values());
    }
}
