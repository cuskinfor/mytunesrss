package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;

import javax.management.NotCompliantMBeanException;

/**
 * de.codewave.mytunesrss.jmx.Application
 */
public class Application extends MyTunesRssMBean implements ApplicationMBean {

  Application() throws NotCompliantMBeanException {
    super(ApplicationMBean.class);
  }

  public String getVersion() {
        return MyTunesRss.VERSION;
    }

    public String quit() {
        MyTunesRss.ERROR_QUEUE.clear();
        if (MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.stopWebserver();
        }
        if (MyTunesRss.ERROR_QUEUE.isEmpty()) {
            MyTunesRss.QUIT_REQUEST = true;
        }
        return MyTunesRss.ERROR_QUEUE.popLastError();
    }
}