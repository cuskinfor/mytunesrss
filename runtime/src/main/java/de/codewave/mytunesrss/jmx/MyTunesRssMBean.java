package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;

import javax.management.*;
import java.util.MissingResourceException;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       Cologne Systems GmbH<br> <b>Creation Date:</b>
 * 01.03.2007
 *
 * @author Michael Descher
 * @version 1.0
 */
public class MyTunesRssMBean extends StandardMBean {

    public MyTunesRssMBean(Class mbeanInterface) throws NotCompliantMBeanException {
        super(mbeanInterface);
    }

    protected String getDescription(MBeanInfo info) {
        return getResourceString("class");
    }

    protected String getDescription(MBeanAttributeInfo info) {
        return getResourceString(info.getName());
    }

    private String getResourceString(String suffix) {
        String key = getClass().getName().substring(getClass().getPackage().getName().length() + 1) + "." + suffix;
        try {
            return MyTunesRss.JMX_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return "???" + key + "???";
        }
    }

    protected String getDescription(MBeanOperationInfo info) {
        return getResourceString(info.getName());
    }

    protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        return getResourceString(op.getName() + ".param" + sequence);
    }

    protected void onChange() {
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.CONFIGURATION_CHANGED);
    }
}



