package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.lang.StringUtils;

import javax.management.NotCompliantMBeanException;
import java.io.UnsupportedEncodingException;


/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       Cologne Systems GmbH<br> <b>Creation Date:</b>
 * 01.03.2007
 *
 * @author Michael Descher
 * @version 1.0
 */
public class RemoteControlConfig extends MyTunesRssMBean implements RemoteControlConfigMBean {
    RemoteControlConfig() throws NotCompliantMBeanException {
        super(RemoteControlConfigMBean.class);
    }


    public String getVideoLanClientHost() {
        return MyTunesRss.CONFIG.getVideoLanClientHost();
    }

    public void setVideoLanClientHost(String host) {
        MyTunesRss.CONFIG.setVideoLanClientHost(host);
        onChange();
    }

    public int getVideoLanClientPort() {
        return MyTunesRss.CONFIG.getVideoLanClientPort();
    }

    public void setVideoLanClientPort(int port) {
        MyTunesRss.CONFIG.setVideoLanClientPort(port);
        onChange();
    }
}