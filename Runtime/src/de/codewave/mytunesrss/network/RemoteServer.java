/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.network;

/**
 * de.codewave.mytunesrss.network.RemoteServer
 */
public class RemoteServer {
    private String myName;
    private String myAddress;
    private int myPort;

    public RemoteServer(String name, String address, int port) {
        myName = name;
        myAddress = address;
        myPort = port;
    }

    public String getAddress() {
        return myAddress;
    }

    public String getName() {
        return myName;
    }

    public int getPort() {
        return myPort;
    }
}