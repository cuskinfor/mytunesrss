/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.servlet.ServletUtils;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;

import java.util.List;

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
        try {
            if (verified != null) {
                AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();
                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
                    String email = fetchResp.getAttributeValue("email");
                    for (User user : MyTunesRss.CONFIG.getUsers()) {
                        if (email.equalsIgnoreCase(user.getName()) && user.isActive()) {
                            doLoginUser(user.getName(), getRequest().getParameter("lc"), getBooleanRequestParameter("rememberLogin", false));
                            return; // done
                        }
                    }
                }
            }
            handleLoginError(getRequest().getParameter("openId"));
        } finally {
            removeLoginSessionAttributes();
        }
    }
}
