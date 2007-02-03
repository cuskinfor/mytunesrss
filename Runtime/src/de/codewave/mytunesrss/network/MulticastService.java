/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.network;

import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.util.*;

import de.codewave.mytunesrss.*;

/**
 * de.codewave.mytunesrss.network.MulticastService
 */
public class MulticastService implements Runnable {
    private static final Log LOG = LogFactory.getLog(MulticastService.class);

    private static final String QUERY = "mtr-qs";
    private static final String MULTICAST_IP = "225.24.2.72";
    private static final int MULTICAST_PORT = 24272;

    public static List<RemoteServer> getOtherInstances() {
        List<RemoteServer> otherInstances = new ArrayList<RemoteServer>();
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket();
            byte[] buffer = QUERY.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
            //socket.setLoopbackMode(true);
            socket.send(sendPacket);
            try {
                socket.setSoTimeout(3000);
                buffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    socket.receive(receivePacket);
                    String answer = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                    String name = answer.substring(answer.indexOf(':') + 1);
                    int port = Integer.parseInt(answer.substring(0, answer.indexOf(':')));
                    String address = receivePacket.getAddress().getHostName();
                    otherInstances.add(new RemoteServer(name, address, port));
                }
            } catch (SocketTimeoutException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No more remote servers found.", e);
                }
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get multicast answers.", e);
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        return otherInstances;
    }

    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(InetAddress.getByName(MULTICAST_IP));
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(receivePacket);
                    String data = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                    if (QUERY.equals(data)) {
                        byte[] answer = (MyTunesRss.CONFIG.getPort() + ":" + MyTunesRss.CONFIG.getServerName()).getBytes("UTF-8");
                        DatagramPacket sendPacket = new DatagramPacket(answer, answer.length, receivePacket.getAddress(), receivePacket.getPort());
                        socket.send(sendPacket);
                    }
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not send multicas answer.", e);
                    }
                }
            }
        } catch (UnknownHostException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not initialize multicast service.", e);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not initialize multicast service.", e);
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}