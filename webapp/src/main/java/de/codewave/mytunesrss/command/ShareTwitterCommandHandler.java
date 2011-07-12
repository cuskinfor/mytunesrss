/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.servlet.ServletException;
import java.io.IOException;

public class ShareTwitterCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws IOException, ServletException {
        if (isSessionAuthorized()) {
            if (getBooleanRequestParameter("initial", false)) {
                getSession().removeAttribute("twitterRequestToken");
                getSession().setAttribute("twitterBackUrl", getRequest().getParameter("backUrl"));
                getSession().setAttribute("twitterComment", getRequest().getParameter("comment") + " #MyTunesRSS");
            }
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer("bT0hPOaFk7acCBPBaY0HA", "QnTazEP8JxjpQkMSpQZwZjqYTWgW5yNlq0FgRxbyKsU");
            User user = getAuthUser();
            String backUrl = (String) getSession().getAttribute("twitterBackUrl");
            try {
                if (getSession().getAttribute("twitterRequestToken") != null) {
                    saveAccessToken(twitter);
                }
                if (StringUtils.isNotBlank(user.getTwitterAuthAccessToken()) && StringUtils.isNotBlank(user.getTwitterAuthTokenSecret())) {
                    twitter.setOAuthAccessToken(new AccessToken(user.getTwitterAuthAccessToken(), user.getTwitterAuthTokenSecret()));
                    twitter.updateStatus((String) getSession().getAttribute("twitterComment"));
                    addMessage(new BundleError("twitter.success"));
                    redirect(backUrl);
                } else {
                    requestAccessToken(twitter);
                }
            } catch (TwitterException e) {
                addError(new BundleError("error.twitter", e.getErrorMessage()));
                redirect(backUrl);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

    private void requestAccessToken(Twitter twitter) throws TwitterException, IOException {
        RequestToken requestToken = twitter.getOAuthRequestToken(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ShareTwitter));
        getSession().setAttribute("twitterRequestToken", requestToken);
        redirect(requestToken.getAuthenticationURL());
    }

    private void saveAccessToken(Twitter twitter) throws TwitterException {
        RequestToken requestToken = (RequestToken) getSession().getAttribute("twitterRequestToken");
        String verifier = getRequest().getParameter("oauth_verifier");
        AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
        getAuthUser().setTwitterAuth(accessToken.getToken(), accessToken.getTokenSecret());
        getSession().removeAttribute("twitterRequestToken");
    }
}
