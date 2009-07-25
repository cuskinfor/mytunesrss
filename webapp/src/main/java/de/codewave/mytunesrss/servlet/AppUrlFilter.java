package de.codewave.mytunesrss.servlet;

import de.codewave.utils.servlet.ServletUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AppUrlFilter implements Filter {
    public void destroy() {
        // intentionally left blank
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        req.setAttribute("appUrl", ServletUtils.getApplicationUrl((HttpServletRequest) req));
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {
        // intentionally left blank
    }

}
