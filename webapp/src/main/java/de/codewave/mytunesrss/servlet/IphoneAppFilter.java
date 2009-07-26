package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.Session;
import de.codewave.mytunesrss.remote.service.LoginService;
import de.codewave.utils.servlet.ServletUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class IphoneAppFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(IphoneAppFilter.class);

    public void destroy() {
        // intentionally left blank
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        MyTunesRssRemoteEnv.setRequest(httpServletRequest);
        try {
            httpServletRequest.setAttribute("appUrl", ServletUtils.getApplicationUrl((HttpServletRequest) req));
            httpServletRequest.setAttribute("globalConfig", MyTunesRss.CONFIG);
            if (MyTunesRssRemoteEnv.getSession().getId() == null && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAutoLogin())) {
                User user = MyTunesRss.CONFIG.getUser(MyTunesRss.CONFIG.getAutoLogin());
                new LoginService().login(user.getName(), new String(Hex.encodeHex(user.getPasswordHash())), user.getSessionTimeout());
            }
            httpServletRequest.setAttribute("remoteApiSession", MyTunesRssRemoteEnv.getSession().getId());
            chain.doFilter(req, resp);
        } catch (IllegalAccessException e) {
            LOGGER.error("Could not auto-login user \"" + MyTunesRss.CONFIG.getAutoLogin() + "\".", e);
        } finally {
            MyTunesRssRemoteEnv.removeRequest();
        }
    }

    public void init(FilterConfig config) throws ServletException {
        // intentionally left blank
    }

}
