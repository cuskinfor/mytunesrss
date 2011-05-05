package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.server.MyTunesRssSessionInfo;
import de.codewave.utils.servlet.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class QuotaLimitFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuotaLimitFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final AtomicBoolean quotaExceeded = new AtomicBoolean(false);
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response) {
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return new ServletOutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        if (checkQuota()) {
                            getResponse().getOutputStream().write(b);
                        }
                    }
                };
            }
            
            @Override
            public PrintWriter getWriter() throws IOException {
                return new PrintWriter(getResponse().getWriter()) {
                    @Override
                    public void write(int c) {
                        if (checkQuota()) {
                            super.write(c);
                        }
                    }
                };
            }

            private boolean checkQuota() {
                MyTunesRssSessionInfo sessionInfo = (MyTunesRssSessionInfo) SessionManager.getSessionInfo((HttpServletRequest) request);
                if (sessionInfo == null || sessionInfo.getUser() == null || !sessionInfo.getUser().isQuotaExceeded() || getResponse().isCommitted()) {
                    if (sessionInfo != null) {
                        sessionInfo.add(1);
                    }
                    return true;
                }
                quotaExceeded.set(true);
                return false;
            }
        };
        chain.doFilter(request, responseWrapper);
        if (quotaExceeded.get()) {
            LOGGER.info("Quota exceeded: " + ((HttpServletRequest) request).getRequestURI());
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_CONFLICT, "QUOTA EXCEEDED");
        }
    }

    public void destroy() {
        // intentionally left blank
    }
}
