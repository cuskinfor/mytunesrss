package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;

import javax.management.*;

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
            if (MyTunesRss.HEADLESS) {
                MyTunesRss.QUIT_REQUEST = true;
            } else {
                MyTunesRssUtils.shutdownGracefully();
            }
        }
        return MyTunesRss.ERROR_QUEUE.popLastError();
    }

    public String getLicense() {
        if (MyTunesRss.REGISTRATION.isRegistered()) {
            if (MyTunesRss.REGISTRATION.isExpirationDate()) {
                return MyTunesRssUtils.getBundleString("jmx.registrationWithExpiration",
                                                       MyTunesRss.REGISTRATION.getName(),
                                                       MyTunesRss.REGISTRATION.getExpiration(MyTunesRssUtils.getBundleString("common.dateFormat")));
            } else {
                return MyTunesRssUtils.getBundleString("jmx.registration", MyTunesRss.REGISTRATION.getName());
            }
        } else {
            return MyTunesRssUtils.getBundleString("settings.unregistered");
        }
    }
}