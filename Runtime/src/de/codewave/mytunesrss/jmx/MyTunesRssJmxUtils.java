package de.codewave.mytunesrss.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       daGama Business Travel GmbH<br>
 * <b>Creation Date:</b> 13.02.2007
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class MyTunesRssJmxUtils {
  private static Log log = LogFactory.getFactory().getInstance(MyTunesRssJmxUtils.class);

  public static void startJmxServer(int jmxServerPort) {
    try {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      server.registerMBean(new ServerConfig(), new ObjectName("de.codewave.mytunesrss:name=Server"));

//      HtmlAdaptorServer adaptor = new HtmlAdaptorServer();
//      ObjectName adaptorName = new ObjectName("adaptor:proptocol=HTTP");
//      server.registerMBean(adaptor, adaptorName);
//      adaptor.start();

    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Could not start JMX server.", e);
      }
    }
  }
}



