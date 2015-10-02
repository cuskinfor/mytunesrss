package de.codewave.mytunesrss;

import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

public class JettyJasperInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            new JettyJasperInitializer().onStartup(null, sce.getServletContext());
        } catch (ServletException e) {
            throw new RuntimeException("Could not initialize context!", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
