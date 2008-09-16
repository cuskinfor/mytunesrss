/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.WebConfig;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.commanDoLoginCommandHandlerer
 */
public class DoLoginCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        String userName = getRequest().getParameter("username");
        String password = getRequest().getParameter("password");
        if (password != null && !isSessionAuthorized()) {
            byte[] passwordHash = MyTunesRss.SHA1_DIGEST.digest(password.getBytes("UTF-8"));
            if (isAuthorized(userName, passwordHash)) {
                authorize(WebAppScope.Session, userName);
                WebConfig webConfig = getWebConfig();
                Boolean rememberLogin = getBooleanRequestParameter("rememberLogin", false);
                webConfig.setLoginStored(rememberLogin);
                webConfig.setUserName(userName);
                webConfig.setPasswordHash(passwordHash);
                webConfig.save(getRequest(), getResponse());
                if (getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) != null) {
                    restartMyTunesRssCom();
                } else {
                    forward(MyTunesRssCommand.ShowPortal);
                }
            } else {
                addError(new BundleError("error.loginDenied"));
                MyTunesRss.ADMIN_NOTIFY.notifyLoginFailure(userName, getRequest().getRemoteHost());
                forward(MyTunesRssResource.Login);
            }
        } else if (!isSessionAuthorized()) {
            forward(MyTunesRssResource.Login);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}