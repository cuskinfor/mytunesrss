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
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Command handler for submission of login form.
 */
public class DoLoginCommandHandler extends MyTunesRssCommandHandler {

    public void execute() throws Exception {
        String userName = getRequest().getParameter("username");
        String password = getRequest().getParameter("password");
        if (!MyTunesRss.CONFIG.isDisableWebLogin() && password != null && !isSessionAuthorized()) {
            byte[] passwordHash = MyTunesRss.SHA1_DIGEST.digest(password.getBytes("UTF-8"));
            if (isAuthorized(userName, password, passwordHash) && !MyTunesRss.CONFIG.getUser(userName).isEmptyPassword()) {
                doLoginUser(userName, getRequest().getParameter("lc"), getBooleanRequestParameter("rememberLogin", false));
            } else {
                handleLoginError(userName);
            }
        } else if (!isSessionAuthorized()) {
            redirect(MyTunesRssWebUtils.getResourceCommandCall(getRequest(), MyTunesRssResource.Login));
        } else {
            redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ShowPortal));
        }
    }

    protected void handleLoginError(String userName) throws IOException {
        if (MyTunesRss.CONFIG.getUser(userName) != null && !MyTunesRss.CONFIG.getUser(userName).isActive()) {
            addError(new BundleError("error.loginExpired"));
            MyTunesRss.ADMIN_NOTIFY.notifyLoginExpired(userName, ServletUtils.getBestRemoteAddress(getRequest()));
        } else {
            addError(new BundleError("error.loginDenied"));
            MyTunesRss.ADMIN_NOTIFY.notifyLoginFailure(userName, ServletUtils.getBestRemoteAddress(getRequest()));
        }
        redirect(MyTunesRssWebUtils.getResourceCommandCall(getRequest(), MyTunesRssResource.Login));
    }

    protected void doLoginUser(String userName, String lc, Boolean rememberLogin) throws IOException {
        authorize(WebAppScope.Session, userName);
        WebConfig webConfig = getWebConfig();
        if (rememberLogin) {
            MyTunesRssWebUtils.rememberLogin(getRequest(), getResponse(), userName, getAuthUser().getPasswordHash());
        } else {
            MyTunesRssWebUtils.forgetLogin(getRequest(), getResponse());
        }
        MyTunesRssWebUtils.setCookieLanguage(getRequest(), getResponse(), lc);
        MyTunesRssWebUtils.saveWebConfig(getRequest(), getResponse(), getAuthUser(), webConfig);
        StatisticsEventManager.getInstance().fireEvent(new LoginEvent(getAuthUser()));
        if (getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) != null) {
            restartMyTunesRssCom();
        } else {
            redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ShowPortal));
        }
    }
}