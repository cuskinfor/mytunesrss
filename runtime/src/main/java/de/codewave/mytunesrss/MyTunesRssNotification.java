/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public class MyTunesRssNotification {
    private String myHeader;
    private String myDetail;
    private Throwable myThrowable;

    public MyTunesRssNotification(String header, String detail, Throwable throwable) {
        myHeader = header;
        myDetail = detail;
        myThrowable = throwable;
    }

    public String getHeader() {
        return myHeader;
    }

    public String getDetail() {
        return myDetail;
    }

    public Throwable getThrowable() {
        return myThrowable;
    }
}
