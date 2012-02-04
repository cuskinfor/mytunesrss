package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

public class DoSelfRegistrationCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoSelfRegistrationCommandHandler.class);

    @Override
    public void execute() throws Exception {
        String username = StringUtils.trimToEmpty(getRequestParameter("reg_username", null));
        String email = StringUtils.trimToEmpty(getRequestParameter("reg_email", null));
        String password = StringUtils.trimToEmpty(getRequestParameter("reg_password", null));
        String retypePassword = StringUtils.trimToEmpty(getRequestParameter("reg_retypepassword", null));
        if (StringUtils.isBlank(username)) {
            addError(new BundleError("error.registration.emptyUsername"));
        }
        if (StringUtils.isBlank(password)) {
            addError(new BundleError("error.registration.emptyPassword"));
        } else if (!StringUtils.equals(password, retypePassword)) {
            addError(new BundleError("error.registration.retypeFailure"));
        }
        if (StringUtils.isBlank(email)) {
            addError(new BundleError("error.registration.emptyEmail"));
        }
        if (!isError()) {
            User user = (User) MyTunesRss.CONFIG.getUser(MyTunesRss.CONFIG.getSelfRegisterTemplateUser()).clone();
            user.setName(username);
            user.setPasswordHash(MyTunesRss.SHA1_DIGEST.digest(password.getBytes("UTF-8")));
            user.setEmptyPassword(false);
            user.setEmail(email);
            if (!MyTunesRss.CONFIG.addUser(user)) {
                addError(new BundleError("error.registration.duplicateUsername"));
                forward(MyTunesRssResource.SelfRegistration);
            } else if (MyTunesRss.CONFIG.isSelfRegAdminEmail() && MyTunesRss.CONFIG.isValidMailConfig() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
                String subject = "New user account registration";
                String body = "A new account has been created in the user interface.\n\nUsername: " + username + "\nEmail: " + email + "\n\n" +
                        (user.isActive() ? "User is active (edit your template user to change this)." : "User needs to be activated (edit your template user to change this).\n");
                sendAdminMail(subject, body);
                addMessage(new BundleError("info.registration." + (user.isActive() ? "done" : "needsActivation")));
                forward(MyTunesRssResource.Login);
            } else {
                forward(MyTunesRssResource.Login);
            }
        } else {
            forward(MyTunesRssResource.SelfRegistration);
        }
    }

    private void sendAdminMail(String subject, String body) {
        try {
            MyTunesRss.MAILER.sendMail(MyTunesRss.CONFIG.getAdminEmail(), "MyTunesRSS: " + subject, body);
        } catch (MailException e) {
            LOGGER.error("Could not send admin email for user registration.", e);
        }
    }
}
