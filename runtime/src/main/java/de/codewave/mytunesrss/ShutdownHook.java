package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ShutdownHook implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

    @Override
    public void run() {
        LOGGER.info("Running shutdown hook.");
        // try to kill all still running processes
        LOGGER.info("Trying to kill " + MyTunesRss.SPAWNED_PROCESSES.size() + " previously spawned processes.");
        for (Process process : MyTunesRss.SPAWNED_PROCESSES) {
            process.destroy();
        }
        // try to do the best to shutdown the store in a clean way to keep H2 databases intact
        if (MyTunesRss.STORE != null && MyTunesRss.STORE.isInitialized()) {
            LOGGER.info("Destroying still initialized store.");
            MyTunesRss.STORE.destroy();
        }
        // try to kill internal mysql database
        if (MyTunesRss.CONFIG.getDatabaseType() == DatabaseType.mysqlinternal) {
            LOGGER.info("Trying to shutdown internal mysql server.");
            try {
                Class clazz = Class.forName("com.mysql.management.driverlaunched.ServerLauncherSocketFactory", true, MyTunesRss.EXTRA_CLASSLOADER);
                clazz.getMethod("shutdown", File.class, File.class).invoke(null, MyTunesRss.INTERNAL_MYSQL_SERVER_PATH, null);
            } catch (Exception e) {
                LOGGER.warn("Could not shutdown internal mysql server.", e);
            }
        }
        if (MyTunesRss.UPNP_SERVICE != null) {
            // try to shutdown UPnP stuff
            MyTunesRss.UPNP_SERVICE.shutdownMediaServer();
            MyTunesRss.UPNP_SERVICE.shutdown();
        }
    }
}
