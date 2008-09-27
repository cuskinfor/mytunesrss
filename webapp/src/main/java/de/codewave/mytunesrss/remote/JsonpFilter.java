package de.codewave.mytunesrss.remote;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class JsonpFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonpFilter.class);

    public void destroy() {
        // intentionally left blank
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            String jsonp = request.getParameter("jsonp");
            if (StringUtils.isNotEmpty(jsonp)) {
                String encoding = response.getCharacterEncoding() != null ? response.getCharacterEncoding() : "UTF-8";
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                final ServletOutputStream servletOutputStream = new ServletOutputStream() {
                    public void write(int b) throws IOException {
                        buffer.write(b);
                    }
                };
                final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(buffer, encoding));
                HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse)response) {
                    public ServletOutputStream getOutputStream() {
                        return servletOutputStream;
                    }

                    public PrintWriter getWriter() {
                        return printWriter;
                    }
                };
                chain.doFilter(request, responseWrapper);
                LOGGER.debug("JSONP: Original response has " + buffer.size() + "bytes. Adding " + (jsonp.getBytes(encoding).length + 2) +
                        " bytes for \"" + jsonp + "\". Using encoding \"" + encoding + "\".");
                response.setContentLength(buffer.size() + 2 + jsonp.getBytes(encoding).length);
                response.getOutputStream().write(jsonp.getBytes(encoding));
                response.getOutputStream().write("(".getBytes(encoding));
                response.getOutputStream().write(buffer.toByteArray());
                response.getOutputStream().write(")".getBytes(encoding));
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
