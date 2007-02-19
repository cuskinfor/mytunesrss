package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;

/**
 * de.codewave.mytunesrss.jmx.Application
 */
public class Application implements ApplicationMBean {
    public String getVersion() {
        return MyTunesRss.VERSION;
    }

    public String quit() {
        MyTunesRss.ERROR_QUEUE.clear();
        MyTunesRss.QUIT_REQUEST = true;
        return MyTunesRss.ERROR_QUEUE.popLastError();
    }
}