package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import mx4j.tools.adaptor.http.HttpAdaptor;
import mx4j.tools.adaptor.http.XSLTProcessor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 13.02.2007
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class MyTunesRssJmxUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssJmxUtils.class);
    private static ObjectName HTTP_ADAPTOR_NAME;
    private static ObjectName SERVER_CONFIG_NAME;
    private static ObjectName APPLICATION_NAME;
    private static ObjectName DATABASE_CONFIG_NAME;
    private static ObjectName DATAIMPORT_CONFIG_NAME;
    private static ObjectName DIRECTORIES_CONFIG_NAME;
    private static ObjectName USER_CONFIG_NAME;
    private static ObjectName MISC_CONFIG_NAME;
    private static ObjectName ADDONS_CONFIG_NAME;
    private static ObjectName STREAMING_CONFIG_NAME;
    private static ObjectName CONTENT_CONFIG_NAME;
    private static ObjectName ADMIN_NOTIFY_CONFIG_NAME;
    private static ObjectName STATISTIC_CONFIG_NAME;
    private static ObjectName REMOTE_CONTROL_CONFIG_NAME;
    private static boolean INITIALIZED;

    static {
        try {
            HTTP_ADAPTOR_NAME = new ObjectName("MyTunesRSS:type=jmxAdaptor,name=HttpAdaptor");
            SERVER_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Server");
            APPLICATION_NAME = new ObjectName("MyTunesRSS:type=config,name=Application");
            DATABASE_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Database");
            DATAIMPORT_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=DataImport");
            DIRECTORIES_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=DataSources");
            USER_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Users");
            MISC_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Miscellaneous");
            ADDONS_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Addons");
            STREAMING_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Streaming");
            CONTENT_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Content");
            ADMIN_NOTIFY_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=AdminNotification");
            STATISTIC_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=Statistic");
            REMOTE_CONTROL_CONFIG_NAME = new ObjectName("MyTunesRSS:type=config,name=RemoteControl");
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
                server.registerMBean(new ApplicationConfig(), APPLICATION_NAME);
                server.registerMBean(new ServerConfig(), SERVER_CONFIG_NAME);
                server.registerMBean(new DatabaseConfig(), DATABASE_CONFIG_NAME);
                server.registerMBean(new DatasourcesConfig(), DIRECTORIES_CONFIG_NAME);
                server.registerMBean(new DataImportConfig(), DATAIMPORT_CONFIG_NAME);
                server.registerMBean(new UserConfig(), USER_CONFIG_NAME);
                server.registerMBean(new MiscConfig(), MISC_CONFIG_NAME);
                server.registerMBean(new AddonsConfig(), ADDONS_CONFIG_NAME);
                server.registerMBean(new StreamingConfig(), STREAMING_CONFIG_NAME);
                server.registerMBean(new ContentConfig(), CONTENT_CONFIG_NAME);
                server.registerMBean(new AdminNotifyConfig(), ADMIN_NOTIFY_CONFIG_NAME);
                server.registerMBean(new StatisticConfig(), STATISTIC_CONFIG_NAME);
                server.registerMBean(new RemoteControlConfig(), REMOTE_CONTROL_CONFIG_NAME);
                registerUsers();
                HttpAdaptor adaptor = new HttpAdaptor();
                ObjectName name = HTTP_ADAPTOR_NAME;
                server.registerMBean(adaptor, name);
                adaptor.setPort(MyTunesRss.JMX_PORT > 0 && MyTunesRss.JMX_PORT < 65536 ? MyTunesRss.JMX_PORT : MyTunesRss.CONFIG.getJmxPort());
                adaptor.setHost(MyTunesRss.JMX_HOST != null ? MyTunesRss.JMX_HOST : MyTunesRss.CONFIG.getJmxHost());
                String username = MyTunesRss.CONFIG.getJmxUser();
                String password = MyTunesRss.CONFIG.getJmxPassword();
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    adaptor.setAuthenticationMethod("basic");
                    adaptor.addAuthorization(username, password);
                }
                if (!MyTunesRss.REGISTRATION.isDisableJmxHtml()) {
                    adaptor.setProcessor(new XSLTProcessor());
                }
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stopping JMX server.");
            }
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            try {
                server.invoke(HTTP_ADAPTOR_NAME, "stop", null, null);
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not stop JMX server.", e);
                }
            }
            for (ObjectName name : new ObjectName[] {HTTP_ADAPTOR_NAME, SERVER_CONFIG_NAME, APPLICATION_NAME, DATABASE_CONFIG_NAME,
                                                     DIRECTORIES_CONFIG_NAME, DATAIMPORT_CONFIG_NAME, USER_CONFIG_NAME, MISC_CONFIG_NAME,
                                                     ADDONS_CONFIG_NAME, CONTENT_CONFIG_NAME, ADMIN_NOTIFY_CONFIG_NAME, STATISTIC_CONFIG_NAME,
                                                     STREAMING_CONFIG_NAME, REMOTE_CONTROL_CONFIG_NAME}) {
                try {
                    server.unregisterMBean(name);
                } catch (Exception e) {
                    LOG.error("Could not unregister mbean \"" + name.getCanonicalName() + "\".", e);
                }
            }
            unregisterUsers();
        }
    }

    static void unregisterUsers() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            ObjectName name = null;
            try {
                name = new ObjectName("MyTunesRSS:type=user,name=" + ObjectName.quote(user.getName()));
                server.unregisterMBean(name);
            } catch (Exception e) {
                if (name != null) {
                    LOG.error("Could not unregister mbean \"" + name.getCanonicalName() + "\".", e);
                } else {
                    LOG.error("Could not create user mbean object name for \"" + user.getName() + "\".", e);
                }
            }
        }
    }
}



