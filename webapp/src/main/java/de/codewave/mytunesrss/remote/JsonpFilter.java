package de.codewave.mytunesrss.remote;

import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class JsonpFilter implements Filter {

    public void destroy() {
        // intentionally left blank
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            String jsonp = request.getParameter("jsonp");
            if (StringUtils.isNotEmpty(jsonp)) {
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                final ServletOutputStream servletOutputStream = new ServletOutputStream() {
                    public void write(int b) throws IOException {
                        buffer.write(b);
                    }
                };
                final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(buffer));
                HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse)response) {
                    public ServletOutputStream getOutputStream() {
                        return servletOutputStream;
                    }

                    public PrintWriter getWriter() {
                        return printWriter;
                    }
                };
                chain.doFilter(request, responseWrapper);
                response.setContentLength(buffer.toByteArray().length + 2 + jsonp.getBytes("UTF-8").length);
                response.getOutputStream().write(jsonp.getBytes("UTF-8"));
                response.getOutputStream().write("(".getBytes("UTF-8"));
                response.getOutputStream().write(buffer.toByteArray());
                response.getOutputStream().write(")".getBytes("UTF-8"));
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
