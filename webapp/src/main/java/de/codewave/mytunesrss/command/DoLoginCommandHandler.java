/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.servlet.ServletUtils;

import java.io.IOException;

/**
 * Command handler for submission of login form.
 */
public class DoLoginCommandHandler extends MyTunesRssCommandHandler {

    public void execute() throws Exception {
        String userName = getRequest().getParameter("username");
        String password = getRequest().getParameter("password");
        if (!MyTunesRss.CONFIG.isDisableWebLogin() && password != null && !isSessionAuthorized()) {
            byte[] passwordHash = MyTunesRss.SHA1_DIGEST.get().digest(password.getBytes("UTF-8"));
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

    private void handleLoginError(String userName) throws IOException {
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
            MyTunesRssWebUtils.rememberLogin(getResponse(), userName, getAuthUser().getPasswordHash());
        } else {
            MyTunesRssWebUtils.forgetLogin(getResponse());
        }
        MyTunesRssWebUtils.setCookieLanguage(getRequest(), getResponse(), lc);
        MyTunesRssWebUtils.saveWebConfig(getRequest(), getResponse(), getAuthUser(), webConfig);
        redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ShowPortal));
    }
}