/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssCommandCallBuilder;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.mytunesrss.statistics.LoginEvent;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.utils.servlet.ServletUtils;
import org.apache.commons.lang.StringUtils;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.sreg.SRegRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Command handler for submission of login form.
 */
public class DoLoginWithOpenIdCommandHandler extends DoLoginCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoLoginWithOpenIdCommandHandler.class);

    public void execute() throws Exception {
        if (!MyTunesRss.CONFIG.isDisableWebLogin() && !isSessionAuthorized()) {
            String openId = getRequest().getParameter("openId");
            try {
                ConsumerManager manager = new ConsumerManager();
                List discoveries = manager.discover(openId);
                DiscoveryInformation discovered = manager.associate(discoveries);
                getSession().setAttribute("openIdConsumerManager", manager);
                getSession().setAttribute("openidDiscovered", discovered);
                MyTunesRssCommandCallBuilder callBuilder = new MyTunesRssCommandCallBuilder(MyTunesRssCommand.ValidateOpenId);
                callBuilder.addParam("lc", StringUtils.trimToNull(getRequest().getParameter("lc")));
                callBuilder.addParam("openId", StringUtils.trimToNull(getRequest().getParameter("openId")));
                callBuilder.addParam("rememberLogin", StringUtils.trimToNull(getRequest().getParameter("rememberLogin")));
                AuthRequest authReq = manager.authenticate(discovered, callBuilder.getCall(getRequest()));
                FetchRequest fetchRequest = FetchRequest.createFetchRequest();
                fetchRequest.addAttribute("email", "http://schema.openid.net/contact/email", true, 1);
                authReq.addExtension(fetchRequest);
                redirect(authReq.getDestinationUrl(true));
                return; // done
            } catch (DiscoveryException e) {
                LOGGER.debug("No open id login possible with username \"" + openId);
            } catch (MessageException e) {
                LOGGER.debug("No open id login possible with username \"" + openId);
            } catch (ConsumerException e) {
                LOGGER.debug("No open id login possible with username \"" + openId);
            } catch (IOException e) {
                LOGGER.debug("No open id login possible with username \"" + openId);
            }
            removeLoginSessionAttributes();
        }
        redirect(MyTunesRssWebUtils.getResourceCommandCall(getRequest(), MyTunesRssResource.Login));
    }

    protected void removeLoginSessionAttributes() {
        getSession().removeAttribute("openIdConsumerManager");
        getSession().removeAttribute("openidDiscovered");
    }
}