/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.servlet.EncodingFilter
 */
public class EncodingFilter implements Filter {
    private String myEncoding;

    public void init(FilterConfig filterConfig) throws ServletException {
        String encoding = filterConfig.getInitParameter("encoding");
        if (StringUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        myEncoding = encoding;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (StringUtils.isBlank(servletRequest.getCharacterEncoding())) {
            servletRequest.setCharacterEncoding(myEncoding);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        // intentionally left blank
    }
}
