/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.utils.servlet.ServletRequestParameterTrimmer
 */
public class ServletRequestParameterTrimmer implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            filterChain.doFilter(new TrimmedParametersHttpServletRequest((HttpServletRequest)servletRequest), servletResponse);
        } else {
            filterChain.doFilter(new TrimmedParametersServletRequest(servletRequest), servletResponse);
        }
    }

    public void destroy() {
        // intentionally left blank
    }

    public static class TrimmedParametersServletRequest extends ServletRequestWrapper {
        private ParameterTrimmer myParameterTrimmer;

        public TrimmedParametersServletRequest(ServletRequest servletRequest) {
            super(servletRequest);
            myParameterTrimmer = new ParameterTrimmer(servletRequest);
        }

        @Override
        public String getParameter(String string) {
            return myParameterTrimmer.getParameter(string);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return myParameterTrimmer.getParameterMap();
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return myParameterTrimmer.getParameterNames();
        }

        @Override
        public String[] getParameterValues(String string) {
            return myParameterTrimmer.getParameterValues(string);
        }
    }

    public static class TrimmedParametersHttpServletRequest extends HttpServletRequestWrapper {
        private ParameterTrimmer myParameterTrimmer;

        public TrimmedParametersHttpServletRequest(HttpServletRequest servletRequest) {
            super(servletRequest);
            myParameterTrimmer = new ParameterTrimmer(servletRequest);
        }

        @Override
        public String getParameter(String string) {
            return myParameterTrimmer.getParameter(string);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return myParameterTrimmer.getParameterMap();
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return myParameterTrimmer.getParameterNames();
        }

        @Override
        public String[] getParameterValues(String string) {
            return myParameterTrimmer.getParameterValues(string);
        }
    }

    public static class ParameterTrimmer {
        private Map<String, String[]> myTrimmedParameters;

        public ParameterTrimmer(ServletRequest servletRequest) {
            init(servletRequest);
        }

        private void init(ServletRequest servletRequest) {
            Map<String, String[]> originalParameters = servletRequest.getParameterMap();
            myTrimmedParameters = new HashMap<String, String[]>(originalParameters.size());
            for (Map.Entry<String, String[]> entry : originalParameters.entrySet()) {
                String[] trimmedValues = new String[entry.getValue().length];
                myTrimmedParameters.put(entry.getKey(), trimmedValues);
                for (int i = 0; i < entry.getValue().length; i++) {
                    trimmedValues[i] = entry.getValue()[i].trim();
                }
            }
        }

        public String getParameter(String string) {
            String[] values = getParameterValues(string);
            return values != null && values.length > 0 ? values[0] : null;
        }

        public Map<String, String[]> getParameterMap() {
            return new HashMap<String, String[]>(myTrimmedParameters);
        }

        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(myTrimmedParameters.keySet());
        }

        public String[] getParameterValues(String string) {
            return myTrimmedParameters.get(string);
        }
    }
}