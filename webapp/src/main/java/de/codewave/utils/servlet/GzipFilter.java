package de.codewave.utils.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter for gzipping the response in case gzip is supported (via the request headers).
 */
public class GzipFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GzipFilter.class);

    /**
     * Initialize the filter with some parameters from the filter configuration.
     *
     * @param filterConfig The filter configuration.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    /**
     * Filter worker method.
     *
     * @param request  A servlet request.
     * @param response A servlet response.
     * @param chain    The filter chain.
     *
     * @throws IOException      Any IO exception from the chain.
     * @throws ServletException Any servlet exception from the chain.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse && request instanceof HttpServletRequest && ((HttpServletRequest)request).getHeader(
                "Accept-Encoding") != null && ((HttpServletRequest)request).getHeader("Accept-Encoding").contains("gzip")) {
            LOGGER.debug("Using GZIP filter.");
            chain.doFilter(request, new GzipFilterResponseWrapper((HttpServletResponse)response));
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Destroy the servlet filter.
     */
    @Override
    public void destroy() {
        // intentionally left blank
    }
}