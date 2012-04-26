/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.jmdns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JmDnsServiceListener implements ServiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmDnsServiceListener.class);

    private ConcurrentHashMap<String, JmDnsDevice> myDevices = new ConcurrentHashMap<String, JmDnsDevice>();

    public void serviceAdded(ServiceEvent event) {
        LOGGER.debug("JmDNS service added.");
        ServiceInfo serviceInfo = event.getInfo();
        if (serviceInfo == null || serviceInfo.getInetAddress() == null) {
            serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName(), 2000);
        }
        JmDnsDevice device = new JmDnsDevice(event.getName(), serviceInfo.getInetAddress(), serviceInfo.getPort());
        LOGGER.debug("Adding device with id \"" + device.getId() + "\".");
        myDevices.putIfAbsent(device.getId(), device);
    }

    public void serviceRemoved(ServiceEvent event) {
        LOGGER.debug("JmDNS service removed.");
        ServiceInfo serviceInfo = event.getInfo();
        if (serviceInfo == null || serviceInfo.getInetAddress() == null) {
            serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName(), 2000);
        }
        String id = new JmDnsDevice(event.getName(), serviceInfo.getInetAddress(), serviceInfo.getPort()).getId();
        LOGGER.debug("Adding device with id \"" + id + "\".");
        myDevices.remove(id);
    }

    public void serviceResolved(ServiceEvent event) {
        LOGGER.debug("JmDNS service resolved.");
    }

    public Collection<JmDnsDevice> getDevices() {
        return new HashSet<JmDnsDevice>(myDevices.values());
    }
}
