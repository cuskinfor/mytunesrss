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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String encoding = filterConfig.getInitParameter("encoding");
        if (StringUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        myEncoding = encoding;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (StringUtils.isBlank(servletRequest.getCharacterEncoding())) {
            servletRequest.setCharacterEncoding(myEncoding);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // intentionally left blank
    }
}
