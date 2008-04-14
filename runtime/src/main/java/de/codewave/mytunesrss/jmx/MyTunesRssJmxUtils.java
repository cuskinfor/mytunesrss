package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import mx4j.tools.adaptor.http.*;
import org.apache.commons.lang.*;
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
    private static ObjectName DATABASE_CONFIG_NAME;
    private static ObjectName DIRECTORIES_CONFIG_NAME;
    private static ObjectName USER_CONFIG_NAME;
    private static ObjectName MISC_CONFIG_NAME;
    private static ObjectName ADDONS_CONFIG_NAME;
    private static ObjectName STREAMING_CONFIG_NAME;
    private static ObjectName CONTENT_CONFIG_NAME;
    private static boolean INITIALIZED;

    static {
        try {
            HTTP_ADAPTOR_NAME = new ObjectName("MyTunesRSS:type=jmxAdaptor,name=HttpAdaptor");
            SERVER_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Server");
            APPLICATION_NAME = new ObjectName("MyTunesRSS:type=config,name=Application");
            DATABASE_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Database");
            DIRECTORIES_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Directories");
            USER_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Users");
            MISC_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Miscellaneous");
            ADDONS_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Addons");
            STREAMING_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Streaming");
            CONTENT_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Content");
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
                server.registerMBean(new DatabaseConfig(), DATABASE_CONFIG_NAME);
                server.registerMBean(new DirectoriesConfig(), DIRECTORIES_CONFIG_NAME);
                server.registerMBean(new UserConfig(), USER_CONFIG_NAME);
                server.registerMBean(new MiscConfig(), MISC_CONFIG_NAME);
                server.registerMBean(new AddonsConfig(), ADDONS_CONFIG_NAME);
                server.registerMBean(new StreamingConfig(), STREAMING_CONFIG_NAME);
                server.registerMBean(new ContentConfig(), CONTENT_CONFIG_NAME);
                registerUsers();
                HttpAdaptor adaptor = new HttpAdaptor();
                ObjectName name = HTTP_ADAPTOR_NAME;
                server.registerMBean(adaptor, name);
                int port;
                try {
                    port = Integer.parseInt(System.getProperty("jmx.port", "8500"));
                } catch (NumberFormatException e) {
                    port = 8500;
                }
                adaptor.setPort(port);
                adaptor.setHost(System.getProperty("jmx.host", "0.0.0.0"));
                String username = System.getProperty("jmx.username");
                String password = System.getProperty("jmx.password");
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    adaptor.setAuthenticationMethod("basic");
                    adaptor.addAuthorization(username, password);
                }
                adaptor.setProcessor(new XSLTProcessor());
                adaptor.start();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not start JMX server.", e);
                }
            }
        }
    }

    static void registerUsers()
            throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            server.registerMBean(new EditUserConfig(user.getName()), new ObjectName("MyTunesRSS:type=user,name=" + ObjectName.quote(user.getName())));
        }
    }

    public static void stopJmxServer() {
        if (INITIALIZED) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Stopping JMX server.");
                }
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                server.invoke(HTTP_ADAPTOR_NAME, "stop", null, null);
                server.unregisterMBean(HTTP_ADAPTOR_NAME);
                server.unregisterMBean(SERVER_CONFIG_NAME);
                server.unregisterMBean(APPLICATION_NAME);
                server.unregisterMBean(DATABASE_CONFIG_NAME);
                server.unregisterMBean(DIRECTORIES_CONFIG_NAME);
                server.unregisterMBean(USER_CONFIG_NAME);
                server.unregisterMBean(MISC_CONFIG_NAME);
                server.unregisterMBean(ADDONS_CONFIG_NAME);
                server.unregisterMBean(CONTENT_CONFIG_NAME);
                unregisterUsers();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not stop JMX server.", e);
                }
            }
        }
    }

    static void unregisterUsers() throws InstanceNotFoundException, MBeanRegistrationException, MalformedObjectNameException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            server.unregisterMBean(new ObjectName("MyTunesRSS:type=user,name=" + user.getName()));
        }
    }
}



