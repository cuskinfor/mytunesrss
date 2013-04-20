/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

public class BadRequestException extends Exception {

    private MyTunesRssResource myResource;

    public BadRequestException() {
    }

    public BadRequestException(String s) {
        super(s);
    }

    public BadRequestException(MyTunesRssResource resource) {
        myResource = resource;
    }

    public BadRequestException(MyTunesRssResource resource, String s) {
        super(s);
        myResource = resource;
    }

    public MyTunesRssResource getResource() {
        return myResource;
    }
}
