/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.bonjour;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;

public class BonjourDevice {
    private String id;
    private String name;
    private InetAddress inetAddress;
    private int port;

    public BonjourDevice(String name, InetAddress inetAddress, int port) {
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(inetAddress.getAddress());
            messageDigest.update(String.valueOf(port).getBytes("UTF-8"));
            id = String.format("%032X", new BigInteger(1, messageDigest.digest()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String getName() {
        return name;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "BonjourDevice{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", inetAddress='" + inetAddress + '\'' +
                ", port=" + port +
                '}';
    }
}
