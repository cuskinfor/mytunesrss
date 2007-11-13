package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.command.*;
import de.codewave.utils.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.servlet.TransactionFilter
 */
public class TransactionFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
            ServletException {
        ServletContext servletContext = ((HttpServletRequest)servletRequest).getSession().getServletContext();
        DataStore store = (DataStore)servletContext.getAttribute(MyTunesRssDataStore.class.getName());
        DataStoreSession session = store.getTransaction();
        MyTunesRssCommandHandler.setTransaction(session);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MyTunesRssCommandHandler.removeTransaction();
            session.commit();
        }
    }

    public void destroy() {
        // intentionally left blank
    }
}