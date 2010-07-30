/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.mytunesrss.statistics.LoginEvent;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.utils.servlet.ServletUtils;

import javax.servlet.ServletException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.commanDoLoginCommandHandlerer
 */
public class DoLoginCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        String userName = getRequest().getParameter("username");
        String password = getRequest().getParameter("password");
        if (!MyTunesRss.CONFIG.isDisableWebLogin() && password != null && !isSessionAuthorized()) {
            byte[] passwordHash = MyTunesRss.SHA1_DIGEST.digest(password.getBytes("UTF-8"));
            if (isAuthorized(userName, password, passwordHash)) {
                authorize(WebAppScope.Session, userName);
                WebConfig webConfig = getWebConfig();
                Boolean rememberLogin = getBooleanRequestParameter("rememberLogin", false);
                String lc = getRequest().getParameter("lc");
                MyTunesRssWebUtils.setCookieLanguage(getRequest(), getResponse(), lc);
                webConfig.setLoginStored(rememberLogin);
                webConfig.setUserName(userName);
                webConfig.setPasswordHash(passwordHash);
                MyTunesRssWebUtils.saveWebConfig(getRequest(), getResponse(), getWebConfig().getUser(), webConfig);
                StatisticsEventManager.getInstance().fireEvent(new LoginEvent(getAuthUser()));
                if (getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) != null) {
                    restartMyTunesRssCom();
                } else {
                    forward(MyTunesRssCommand.ShowPortal);
                }
            } else {
                if (MyTunesRss.CONFIG.getUser(userName) != null && !MyTunesRss.CONFIG.getUser(userName).isActive()) {
                    addError(new BundleError("error.loginExpired"));
                    MyTunesRss.ADMIN_NOTIFY.notifyLoginExpired(userName, ServletUtils.getBestRemoteAddress(getRequest()));
                } else {
                    addError(new BundleError("error.loginDenied"));
                    MyTunesRss.ADMIN_NOTIFY.notifyLoginFailure(userName, ServletUtils.getBestRemoteAddress(getRequest()));
                }
                forward(MyTunesRssResource.Login);
            }
        } else if (!isSessionAuthorized()) {
            forward(MyTunesRssResource.Login);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}