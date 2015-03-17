package de.codewave.mytunesrss;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomHeaderFilter implements Filter {
    // Cache headers from config for 5 minutes
    public static final long HEADER_CACHE_DURATION = 300000L;

    // Cached headers
    private volatile HashMap<String, String> myCustomWebHeaders;

    // Last cache timestamp
    private volatile long myLastHeaderRefresh;

    public void destroy() {
        // nothing to do
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        if (resp instanceof HttpServletResponse) {
            if (System.currentTimeMillis() - myLastHeaderRefresh > HEADER_CACHE_DURATION) {
                cacheHeadersFromConfig();
            }
            for (Map.Entry<String, String> header : myCustomWebHeaders.entrySet()) {
                ((HttpServletResponse)resp).addHeader(header.getKey(), header.getValue());
            }
        }
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {
        cacheHeadersFromConfig();
    }

    private synchronized void cacheHeadersFromConfig() {
        myLastHeaderRefresh = System.currentTimeMillis();
        myCustomWebHeaders = new HashMap<>(MyTunesRss.CONFIG.getCustomWebHeaders());
    }

}
