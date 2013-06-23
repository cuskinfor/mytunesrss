/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.MyTunesRss;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;

public class RouterConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterConfig.class);

    private int myAdminPort;
    private int myUserHttpPort;
    private int myUserHttpsPort;

    public synchronized void addAdminPortMapping(int port) {
        if (MyTunesRss.CONFIG.isUpnpAdmin()) {
            myAdminPort = port;
            addPortMapping(port, "MyTunesRSS Admin");
        }
    }

    public synchronized void addUserPortMappings() {
        if (MyTunesRss.CONFIG.isUpnpUserHttp()) {
            myUserHttpPort = MyTunesRss.CONFIG.getPort();
            addPortMapping(myUserHttpPort, "MyTunesRSS User HTTP");
        }
        if (MyTunesRss.CONFIG.isUpnpUserHttps()) {
            myUserHttpsPort = MyTunesRss.CONFIG.getSslPort();
            addPortMapping(myUserHttpsPort, "MyTunesRSS User HTTPS");
        }
    }

    public synchronized void deleteAdminPortMapping() {
        if (myAdminPort > 0) {
            deletePortMapping(myAdminPort);
        }
        myAdminPort = 0;
    }

    public synchronized void deleteUserPortMappings() {
        if (myUserHttpPort > 0) {
            deletePortMapping(myUserHttpPort);
        }
        if (myUserHttpsPort > 0) {
            deletePortMapping(myUserHttpsPort);
        }
        myUserHttpPort = 0;
        myUserHttpsPort = 0;
    }

    private void addPortMapping(final int port, final String name) {
        MyTunesRss.EXECUTOR_SERVICE.submitRouterConfig(new Runnable() {
            public void run() {
                GatewayDevice gatewayDevice = getGatewayDevice();
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
        });
    }

    private void deletePortMapping(final int port) {
        MyTunesRss.EXECUTOR_SERVICE.submitRouterConfig(new Runnable() {
            public void run() {
                GatewayDevice gatewayDevice = getGatewayDevice();
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
        });
    }

    private GatewayDevice getGatewayDevice() {
        GatewayDiscover gatewayDiscover = new GatewayDiscover();
        try {
            gatewayDiscover.discover();
            return gatewayDiscover.getValidGateway();
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
        return null;
    }
}
