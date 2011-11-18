/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.servlet.ServletUtils;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.ParameterList;

public class ValidateOpenIdCommandHandler extends DoLoginWithOpenIdCommandHandler {
    @Override
    public void execute() throws Exception {
        ConsumerManager manager = (ConsumerManager) getSession().getAttribute("openIdConsumerManager");
        ParameterList openidResp = new ParameterList(getRequest().getParameterMap());
        DiscoveryInformation discovered = (DiscoveryInformation) getSession().getAttribute("openidDiscovered");
        StringBuffer receivingURL = getRequest().getRequestURL();
        String queryString = getRequest().getQueryString();
        if (queryString != null && queryString.length() > 0) {
            receivingURL.append("?").append(getRequest().getQueryString());
        }
        VerificationResult verification = manager.verify(receivingURL.toString(), openidResp, discovered);
        Identifier verified = verification.getVerifiedId();
        if (verified != null && isActiveUser(verified.getIdentifier())) {
            doLoginUser(verified.getIdentifier(), getRequest().getParameter("lc"), getBooleanRequestParameter("rememberLogin", false));
        } else if (verified != null) {
            handleLoginError(verified.getIdentifier());
        } else {
            addError(new BundleError("error.loginDenied"));
            MyTunesRss.ADMIN_NOTIFY.notifyLoginFailure(getRequest().getParameter("openId"), ServletUtils.getBestRemoteAddress(getRequest()));
            redirect(MyTunesRssWebUtils.getResourceCommandCall(getRequest(), MyTunesRssResource.Login));
        }
        removeLoginSessionAttributes();
    }
}
