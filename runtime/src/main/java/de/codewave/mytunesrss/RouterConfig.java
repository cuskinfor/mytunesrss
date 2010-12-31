/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wetorrent.upnp.GatewayDevice;
import org.wetorrent.upnp.GatewayDiscover;
import org.wetorrent.upnp.PortMappingEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RouterConfig implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterConfig.class);

    private AtomicReference<GatewayDevice> myGatewayDevice = new AtomicReference<GatewayDevice>();

    private AtomicInteger myAdminPort = new AtomicInteger();
    private AtomicInteger myUserHttpPort = new AtomicInteger();
    private AtomicInteger myUserHttpsPort = new AtomicInteger();

    public void addAdminPortMapping(int port) {
        if (MyTunesRss.CONFIG.isUpnpAdmin()) {
            myAdminPort.set(port);
            addPortMapping(port, "MyTunesRSS Admin");
        }
    }

    public void addUserPortMappings() {
        if (MyTunesRss.CONFIG.isUpnpUserHttp()) {
            myUserHttpPort.set(MyTunesRss.CONFIG.getPort());
            addPortMapping(myUserHttpPort.get(), "MyTunesRSS User HTTP");
        }
        if (MyTunesRss.CONFIG.isUpnpUserHttps()) {
            myUserHttpsPort.set(MyTunesRss.CONFIG.getSslPort());
            addPortMapping(myUserHttpsPort.get(), "MyTunesRSS User HTTPS");
        }
    }

    public void deleteAdminPortMapping() {
        int port = myAdminPort.getAndSet(0);
        if (port > 0) {
            deletePortMapping(port);
        }
    }

    public void deleteUserPortMappings() {
        int port = myUserHttpPort.getAndSet(0);
        if (port > 0) {
            deletePortMapping(port);
        }
        port = myUserHttpsPort.getAndSet(0);
        if (port > 0) {
            deletePortMapping(port);
        }
    }

    private void addPortMapping(int port, String name) {
        GatewayDevice gatewayDevice = myGatewayDevice.get();
        if (gatewayDevice != null) {
            InetAddress localAddress = gatewayDevice.getLocalAddress();
            PortMappingEntry portMapping = new PortMappingEntry();
            try {
                if (gatewayDevice.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                    gatewayDevice.deletePortMapping(port, "TCP");
                }
                gatewayDevice.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", name);
            } catch (IOException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Could not add port mapping.", e);
                }
            } catch (SAXException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Could not add port mapping.", e);
                }
            }
        }
    }

    private void deletePortMapping(int port) {
        GatewayDevice gatewayDevice = myGatewayDevice.get();
        if (gatewayDevice != null) {
            PortMappingEntry portMapping = new PortMappingEntry();
            try {
                if (gatewayDevice.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                    gatewayDevice.deletePortMapping(port, "TCP");
                }
            } catch (IOException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Could not remove port mapping.", e);
                }
            } catch (SAXException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Could not remove port mapping.", e);
                }
            }
        }
    }

    /**
     * Runnable worker method which discovers a valid gateway device.
     */
    public void run() {
        GatewayDiscover gatewayDiscover = new GatewayDiscover();
        try {
            gatewayDiscover.discover();
            GatewayDevice validGateway = gatewayDiscover.getValidGateway();
            if (validGateway != null) {
                myGatewayDevice.set(validGateway);
            }
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not discover gateway device.", e);
            }
        } catch (SAXException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not discover gateway device.", e);
            }
        } catch (ParserConfigurationException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not discover gateway device.", e);
            }
        }
    }
}
