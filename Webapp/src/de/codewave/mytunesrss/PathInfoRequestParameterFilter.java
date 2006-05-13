/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.PathInfoRequestParameterFilter
 */
public class PathInfoRequestParameterFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
            ServletException {
        filterChain.doFilter(new MergedParametersHttpServletRequest((HttpServletRequest)servletRequest), servletResponse);
    }

    public void destroy() {
        // intentionally left blank
    }

    public static class MergedParametersHttpServletRequest extends HttpServletRequestWrapper {
        private Map<String, String[]> myParameters;

        public MergedParametersHttpServletRequest(HttpServletRequest servletRequest) {
            super(servletRequest);
            myParameters = new HashMap<String, String[]>(super.getParameterMap());
            String pathInfo = servletRequest.getPathInfo();
            if (StringUtils.isNotEmpty(pathInfo)) {
                for (StringTokenizer tokenizer = new StringTokenizer(pathInfo, "/"); tokenizer.hasMoreTokens(); ) {
                    String token = tokenizer.nextToken();
                    int index = token.indexOf('=');
                    if (index > 0 && index < token.length() - 1) {
                        String key = token.substring(0, index);
                        String value = token.substring(index + 1);
                        if (value.indexOf('=') == -1) {
                            String[] existingValues = myParameters.get(key);
                            if (existingValues == null) {
                                myParameters.put(key, new String[] {value});
                            } else {
                                String[] newValues = new String[existingValues.length + 1];
                                System.arraycopy(existingValues, 0, newValues, 0, existingValues.length);
                                newValues[newValues.length - 1] = value;
                                myParameters.put(key, newValues);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public String getParameter(String string) {
            String[] parameters = getParameterValues(string);
            return parameters != null && parameters.length > 0 ? parameters[0] : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(myParameters);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(getParameterMap().keySet());
        }

        @Override
        public String[] getParameterValues(String string) {
            Collection<String[]> values = getParameterMap().values();
            return values.toArray(new String[values.size()]);
        }
    }

}