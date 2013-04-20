/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

public class UnauthorizedException extends Exception {

    private MyTunesRssResource myResource;

    public UnauthorizedException() {
    }

    public UnauthorizedException(MyTunesRssResource resource) {
        myResource = resource;
    }

    public MyTunesRssResource getResource() {
        return myResource;
    }
}
