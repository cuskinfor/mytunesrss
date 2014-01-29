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
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class BonjourServiceListener implements ServiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BonjourServiceListener.class);

    private ConcurrentHashMap<String, BonjourDevice> myDevices = new ConcurrentHashMap<>();

    public void serviceAdded(ServiceEvent event) {
        LOGGER.debug("Bonjour service added.");
        ServiceInfo serviceInfo = event.getInfo();
        if (serviceInfo == null || serviceInfo.getInetAddresses() == null || serviceInfo.getInetAddresses().length == 0) {
            serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName(), 2000);
        }
        BonjourDevice device = null;
        for (InetAddress inetAddress : serviceInfo.getInetAddresses()) {
            if (isReachable(inetAddress, serviceInfo.getPort())) {
                device = new BonjourDevice(event.getName(), inetAddress, serviceInfo.getPort());
            }
        }
        LOGGER.debug("Adding device with id \"" + device.getId() + "\".");
        myDevices.putIfAbsent(device.getId(), device);
    }

    private boolean isReachable(InetAddress inetAddress, int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(inetAddress, port), 2000);
            socket.close();
            return true;
        } catch (IOException e) {
            LOGGER.info("Could not connect to \"" + inetAddress + "\" on port " + port + ".");
            return false;
        }
    }

    public void serviceRemoved(ServiceEvent event) {
        removeDevice(event);
    }

    protected void removeDevice(ServiceEvent event) {
        LOGGER.debug("Bonjour service removed.");
        ServiceInfo serviceInfo = event.getInfo();
        if (serviceInfo == null || serviceInfo.getInetAddresses() == null || serviceInfo.getInetAddresses().length == 0) {
            serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName(), 2000);
        }
        for (InetAddress inetAddress : serviceInfo.getInetAddresses()) {
            String id = new BonjourDevice(event.getName(), inetAddress, serviceInfo.getPort()).getId();
            LOGGER.debug("Removing device with id \"" + id + "\".");
            myDevices.remove(id);
        }
    }

    public void serviceResolved(ServiceEvent event) {
        LOGGER.debug("Bonjour service resolved.");
    }

    public Collection<BonjourDevice> getDevices() {
        return new HashSet<>(myDevices.values());
    }
}
