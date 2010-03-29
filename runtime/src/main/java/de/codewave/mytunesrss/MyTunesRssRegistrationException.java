/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public class MyTunesRssRegistrationException extends Exception {

    public static enum Error {
        InvalidFile(), LicenseExpired();
    }

    private Error myErrror;

    public MyTunesRssRegistrationException(Error error) {
        myErrror = error;
    }

    public Error getErrror() {
        return myErrror;
    }
}
