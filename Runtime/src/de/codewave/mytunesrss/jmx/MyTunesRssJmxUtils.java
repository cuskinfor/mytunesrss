package de.codewave.mytunesrss.jmx;

import mx4j.tools.adaptor.http.*;
import org.apache.commons.logging.*;

import javax.management.*;
import java.lang.management.*;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 13.02.2007
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class MyTunesRssJmxUtils {
    private static final Log LOG = LogFactory.getLog(MyTunesRssJmxUtils.class);
    private static ObjectName HTTP_ADAPTOR_NAME;
    private static ObjectName SERVER_CONFIG_NAME;
    private static ObjectName APPLICATION_NAME;
    private static boolean INITIALIZED;

    static {
        try {
            HTTP_ADAPTOR_NAME = new ObjectName("de.codewave.mytunesrss:name=HttpAdaptor");
            SERVER_CONFIG_NAME = new ObjectName("de.codewave.mytunesrss:name=Server");
            APPLICATION_NAME = new ObjectName("de.codewave.mytunesrss:name=Application");
            INITIALIZED = true;
        } catch (MalformedObjectNameException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not start JMX server.", e);
            }
        }
    }

    public static void startJmxServer() {
        if (INITIALIZED) {
            try {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                server.registerMBean(new Application(), APPLICATION_NAME);
                server.registerMBean(new ServerConfig(), SERVER_CONFIG_NAME);
                HttpAdaptor adaptor = new HttpAdaptor();
                ObjectName name = HTTP_ADAPTOR_NAME;
                server.registerMBean(adaptor, name);
                int port;
                try {
                    port = Integer.parseInt(System.getProperty("jmx.port"));
                } catch (NumberFormatException e) {
                    port = 8500;
                }
                adaptor.setPort(port);
                adaptor.setHost("0.0.0.0");
                adaptor.setProcessor(new XSLTProcessor());
                adaptor.start();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not start JMX server.", e);
                }
            }
        }
    }

    public static void stopJmxServer() {
        if (INITIALIZED) {
            try {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                server.invoke(HTTP_ADAPTOR_NAME, "stop", null, null);
                server.unregisterMBean(HTTP_ADAPTOR_NAME);
                server.unregisterMBean(SERVER_CONFIG_NAME);
                server.unregisterMBean(APPLICATION_NAME);
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not start JMX server.", e);
                }
            }
        }
    }
}



