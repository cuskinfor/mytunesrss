package de.codewave.mytunesrss.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class BandwidthLimitFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BandwidthLimitFilter.class);

    private static final ThreadLocal<Integer> LIMIT = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0; // no limit
        }
    };

    /**
     * Set the stream limit in bytes per second.
     *
     * @param limit Limit in bytes per second.
     */
    public static void setLimit(int limit) {
        LIMIT.set(limit);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to initialize
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        setLimit(0); // no limit
        try {
            chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {

                private ServletOutputStream throttlingStream;

                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    final int limit = LIMIT.get();
                    if (limit > 0) {
                        if (throttlingStream == null) {
                            throttlingStream = new ThrottlingServletOutputStream(super.getOutputStream(), limit);
                        }
                        return throttlingStream;
                    } else {
                        return super.getOutputStream();
                    }
                }
            });
        } finally {
            setLimit(0); // no limit
        }
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

    private static class ThrottlingServletOutputStream extends ServletOutputStream {
        private int myWritten;
        private long myStartTime;
        private final ServletOutputStream myDelegate;
        private final int myLimit;
        private final int myLimitBytes;

        public ThrottlingServletOutputStream(ServletOutputStream delegate, int limit) {
            myDelegate = delegate;
            myLimit = limit;
            myLimitBytes = limit * 1024;
        }

        @Override
        public void write(int i) throws IOException {
            if (myStartTime == 0) {
                myStartTime = System.currentTimeMillis();
            } else if (myWritten >= myLimitBytes) {
                for (long timeToWait = 1000L - (System.currentTimeMillis() - myStartTime); timeToWait > 0; timeToWait = 1000L - (System.currentTimeMillis() - myStartTime)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Throttling (" + myLimit + " KB/s): waiting " + timeToWait + " ms.");
                    }
                    myDelegate.flush();
                    try {
                        Thread.sleep(timeToWait);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
                myStartTime = System.currentTimeMillis();
                myWritten = 0;
            }
            myDelegate.write(i);
            myWritten++;
        }
    }
}
