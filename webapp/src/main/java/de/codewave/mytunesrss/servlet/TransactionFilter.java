package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.utils.sql.DataStore;
import de.codewave.utils.sql.DataStoreSession;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.servlet.TransactionFilter
 */
public class TransactionFilter implements Filter {
    private static final ThreadLocal<DataStoreSession> TRANSACTIONS = new ThreadLocal<>();

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        ServletContext servletContext = ((HttpServletRequest)servletRequest).getSession().getServletContext();
        DataStore store = (DataStore)servletContext.getAttribute(MyTunesRssDataStore.class.getName());
        DataStoreSession session = store.getTransaction();
        setTransaction(session);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            removeTransaction();
            session.commit();
        }
    }

    public void destroy() {
        // intentionally left blank
    }

    private void setTransaction(DataStoreSession session) {
        TRANSACTIONS.set(session);
    }

    private void removeTransaction() {
        TRANSACTIONS.remove();
    }

    public static DataStoreSession getTransaction() {
        return TRANSACTIONS.get();
    }
}