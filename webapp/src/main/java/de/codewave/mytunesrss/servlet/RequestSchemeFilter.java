package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

public class RequestSchemeFilter implements Filter {
    public void destroy() {// intentionally left blank
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        if ("http".equalsIgnoreCase(req.getScheme()) && StringUtils.isNotBlank(MyTunesRss.CONFIG.getTomcatProxyScheme())) {
            chain.doFilter(wrapRequest(req, MyTunesRss.CONFIG.getTomcatProxyScheme()), resp);
        } else if ("https".equalsIgnoreCase(req.getScheme()) && StringUtils.isNotBlank(MyTunesRss.CONFIG.getTomcatSslProxyScheme())) {
            chain.doFilter(wrapRequest(req, MyTunesRss.CONFIG.getTomcatSslProxyScheme()), resp);
        } else {
            chain.doFilter(req, resp);
        }
    }

    private ServletRequest wrapRequest(ServletRequest req, final String scheme) {
        return new HttpServletRequestWrapper((HttpServletRequest)req) {
            @Override
            public String getScheme() {
                return scheme;
            }
        };
    }

    public void init(FilterConfig config) throws ServletException {// intentionally left blank
    }

}
