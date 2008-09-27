package de.codewave.mytunesrss.remote;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * If a GET request with the request parameter "body" reaches this filter, the chain is called with a request wrapper which sets the request method to
 * POST and returns the value of the "body" parameter in the body of the request.
 */
public class PostViaGetFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostViaGetFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && StringUtils.isNotBlank(request.getParameter("body")) &&
                "GET".equals(((HttpServletRequest)request).getMethod())) {
            LOGGER.debug("POST via GET servlet filter.");
            HttpServletRequestWrapper wrapper = new PostViaGetRequestWrapper((HttpServletRequest)request, request.getParameter("body"));
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
        // intentionally left blank
    }
}