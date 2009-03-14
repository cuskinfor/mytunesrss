package de.codewave.mytunesrss.remote;

import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JsonpFilter implements Filter {
    public void destroy() {
        // intentionally left blank
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && "GET".equals(((HttpServletRequest) request).getMethod())) {
            String jsonp = request.getParameter("jsonp");
            if (response instanceof HttpServletResponse && StringUtils.isNotEmpty(jsonp)) {
                chain.doFilter(request, new JsonpFilterResponseWrapper((HttpServletResponse)response, jsonp));
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

}
