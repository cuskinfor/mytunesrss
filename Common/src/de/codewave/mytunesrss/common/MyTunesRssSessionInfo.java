package de.codewave.mytunesrss.common;

import de.codewave.utils.servlet.*;

import javax.servlet.http.*;

/**
 * de.codewave.mytunesrss.common.MyTunesRssSessionInfo
 */
public class MyTunesRssSessionInfo extends SessionManager.SessionInfo implements FileSender.ByteSentCounter {
    private long myBytesStreamed;

    public MyTunesRssSessionInfo(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    public long getBytesStreamed() {
        return myBytesStreamed;
    }

    public void addBytesStreamed(long bytes) {
        myBytesStreamed += bytes;
    }

    public void add(int i) {
        addBytesStreamed(i);
    }
}