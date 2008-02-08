package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.*;

import javax.management.*;

import org.apache.log4j.*;
import org.apache.commons.lang.*;

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

    public boolean isDebugLogging() {
        return MyTunesRss.CONFIG.isDebugLogging();
    }

    public void setDebugLogging(boolean debugLogging) {
        MyTunesRssUtils.setCodewaveLogLevel(debugLogging ? Level.DEBUG : Level.INFO);
        MyTunesRss.CONFIG.setDebugLogging(debugLogging);
        onChange();
    }

    public String sendSupportRequest(String name, String email, String comment, boolean includeItunesXml) {
        MyTunesRss.CONFIG.setSupportName(name);
        MyTunesRss.CONFIG.setSupportEmail(email);
        SendSupportRequestTask requestTask = new SendSupportRequestTask(name, email, comment, includeItunesXml);
        if (!MyTunesRss.CONFIG.isProxyServer() ||
                (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getProxyHost()) && MyTunesRss.CONFIG.getProxyPort() > 0)) {
            requestTask.execute();
            if (requestTask.isSuccess()) {
                return MyTunesRssUtils.getBundleString("info.supportRequestSent");
            } else {
                return MyTunesRssUtils.getBundleString("error.couldNotSendSupportRequest");
            }
        } else {
            return MyTunesRssUtils.getBundleString("error.illegalProxySettings");
        }
    }
}