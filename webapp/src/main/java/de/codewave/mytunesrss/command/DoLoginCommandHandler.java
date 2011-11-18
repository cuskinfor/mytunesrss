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

    private static final Logger LOGGER = LoggerFactory.getLogger(DoLoginCommandHandler.class);

    public void execute() throws Exception {
        if (!isOpenIdLogin()) {
            String userName = getRequest().getParameter("username");
            String password = getRequest().getParameter("password");
            if (!MyTunesRss.CONFIG.isDisableWebLogin() && password != null && !isSessionAuthorized()) {
                byte[] passwordHash = MyTunesRss.SHA1_DIGEST.digest(password.getBytes("UTF-8"));
                if (isAuthorized(userName, password, passwordHash)) {
                    doLoginUser(userName, passwordHash, getRequest().getParameter("lc"), getBooleanRequestParameter("rememberLogin", false));
                } else {
                    handleLoginError(userName);
                }
            } else if (!isSessionAuthorized()) {
                redirect(MyTunesRssWebUtils.getResourceCommandCall(getRequest(), MyTunesRssResource.Login));
            } else {
                redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ShowPortal));
            }
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

    protected void doLoginUser(String userName, byte[] passwordHash, String lc, Boolean rememberLogin) throws IOException {
        authorize(WebAppScope.Session, userName);
        WebConfig webConfig = getWebConfig();
        if (rememberLogin && passwordHash != null) {
            MyTunesRssWebUtils.rememberLogin(getRequest(), getResponse(), userName, passwordHash);
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

    private boolean isOpenIdLogin() {
        if (!MyTunesRss.CONFIG.isDisableWebLogin()) {
            String openId = getRequest().getParameter("username");
            try {
                ConsumerManager manager = new ConsumerManager();
                List discoveries = manager.discover(openId);
                DiscoveryInformation discovered = manager.associate(discoveries);
                getSession().setAttribute("openIdConsumerManager", manager);
                getSession().setAttribute("openidDiscovered", discovered);
                getSession().setAttribute("login.openId", openId);
                getSession().setAttribute("login.lc", getRequest().getParameter("lc"));
                getSession().setAttribute("login.rememberLogin", getBooleanRequestParameter("rememberLogin", false));
                AuthRequest authReq = manager.authenticate(discovered, MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ValidateOpenId));
                redirect(authReq.getDestinationUrl(true));
                return true;
            } catch (DiscoveryException e) {
                LOGGER.debug("No open id login possible with username \"" + openId);
            } catch (MessageException e) {
                LOGGER.debug("No open id login possible with username \"" + openId);
            } catch (ConsumerException e) {
                LOGGER.debug("No open id login possible with username \"" + openId);
            } catch (IOException e) {
                LOGGER.debug("No open id login possible with username \"" + openId);
            }
        }
        removeLoginSessionAttributes();
        return false;
    }

    protected void removeLoginSessionAttributes() {
        getSession().removeAttribute("openIdConsumerManager");
        getSession().removeAttribute("openidDiscovered");
        getSession().removeAttribute("login.openId");
        getSession().removeAttribute("login.lc");
        getSession().removeAttribute("login.rememberLogin");
    }
}