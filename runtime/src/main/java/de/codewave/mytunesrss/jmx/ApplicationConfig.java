package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.task.SendSupportRequestTask;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotCompliantMBeanException;

/**
 * de.codewave.mytunesrss.jmx.Application
 */
public class ApplicationConfig extends MyTunesRssMBean implements ApplicationConfigMBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    ApplicationConfig() throws NotCompliantMBeanException {
        super(ApplicationConfigMBean.class);
    }

    public String getVersion() {
        return MyTunesRss.VERSION;
    }

    public String quit() {
        LOGGER.debug("JMX call to Application#quit");
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
        if (MyTunesRss.REGISTRATION.isExpirationDate()) {
            return MyTunesRssUtils.getBundleString("jmx.registrationWithExpiration",
                                                   MyTunesRss.REGISTRATION.getName(),
                                                   MyTunesRss.REGISTRATION.getExpiration(MyTunesRssUtils.getBundleString("common.dateFormat")));
        } else {
            return MyTunesRssUtils.getBundleString("jmx.registration", MyTunesRss.REGISTRATION.getName());
        }
    }

    public String getCodewaveLogLevel() {
        return MyTunesRss.CONFIG.getCodewaveLogLevel().toString();
    }

    public void setCodewaveLogLevel(String level) {
        MyTunesRssUtils.setCodewaveLogLevel(Level.toLevel(StringUtils.upperCase(level)));
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

    public String getSystemInfo() {
        return MyTunesRssUtils.getSystemInfo();
    }

    public String getApplicationState() {
        StringBuilder builder = new StringBuilder();
        builder.append("webserver=").append(MyTunesRss.WEBSERVER.isRunning() ? "started" : "stopped");
        return builder.toString();
    }
}