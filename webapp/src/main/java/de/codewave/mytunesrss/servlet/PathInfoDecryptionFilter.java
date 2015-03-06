package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.servlet.EncodingFilter
 */
public class PathInfoDecryptionFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(PathInfoDecryptionFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (MyTunesRss.CONFIG.getPathInfoKey() != null) {
            servletRequest = new RequestWrapper((HttpServletRequest) servletRequest);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    @Override
    public void destroy() {
        // intentionally left blank
    }

    private static class RequestWrapper extends HttpServletRequestWrapper {
        public RequestWrapper(HttpServletRequest servletRequest) {
            super(servletRequest);
        }

        @Override
        public String getPathInfo() {
            SecretKey key = MyTunesRss.CONFIG.getPathInfoKey();
            String pathInfo = ((HttpServletRequest) getRequest()).getPathInfo();
            String pathInfoToReturn = pathInfo;
            if (StringUtils.isNotEmpty(pathInfo)) {
                String[] splitted = StringUtils.split(pathInfo, "/");
                try {
                    Cipher cipher = Cipher.getInstance(key.getAlgorithm());
                    cipher.init(Cipher.DECRYPT_MODE, key);
                    for (int i = 0; i < splitted.length; i++) {
                        if (splitted[i].startsWith("{") && splitted[i].endsWith("}")) {
                            splitted[i] = new String(cipher.doFinal(MyTunesRssBase64Utils.decode(splitted[i].substring(1, splitted[i].length() - 1))),
                                    "UTF-8");
                        }
                    }
                    pathInfoToReturn = "/" + StringUtils.join(splitted, "/");
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not descrypt path info.", e);
                    }
                }
            }
            return pathInfoToReturn != null ? pathInfoToReturn.replace((char)1, (char)0x2f).replace((char)2, (char)0x5c) : null;
        }
    }
}
