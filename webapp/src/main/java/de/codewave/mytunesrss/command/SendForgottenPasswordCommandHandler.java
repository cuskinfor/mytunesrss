package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Random;

/**
 * Command handler for sending a new password if the user forgot his current one.
 */
public class SendForgottenPasswordCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendForgottenPasswordCommandHandler.class);

    private Random myRandom = new Random(System.currentTimeMillis());

    public void execute() throws IOException, ServletException {
        String userName = getRequest().getParameter("username");
        if (StringUtils.isNotEmpty(userName)) {
            User user = getMyTunesRssConfig().getUser(userName);
            if (user != null) {
                if (user.isChangePassword() && !user.isEmptyPassword()) {
                    if (StringUtils.isNotEmpty(user.getEmail())) {
                        StringBuilder password = new StringBuilder();
                        for (int i = 0; i < 10; i++) {
                            password.append((char)((byte)'a' + (byte)myRandom.nextInt(26)));
                        }
                        user.setPasswordHash(MyTunesRss.SHA1_DIGEST.digest(password.toString().getBytes("UTF-8")));
                        MyTunesRss.ADMIN_NOTIFY.notifyPasswordChange(user);
                        de.codewave.mytunesrss.jsp.Error error = sendPasswordToUser(user, password);
                        if (error != null) {
                            addError(error);
                        } else {
                            addMessage(new BundleError("info.passwordSent"));
                        }
                    } else {
                        addError(new BundleError("error.sendPasswordNoUserEmail"));
                    }
                } else {
                    addError(new BundleError("error.sendPasswordNoPermission"));
                }
            } else {
                addError(new BundleError("error.sendPasswordNoSuchUser"));
            }
        } else {
            addError(new BundleError("error.sendPasswordNoUserName"));
        }
        forward(MyTunesRssResource.Login);
    }

    /**
     * Send a mail with the new password to the specified user.
     *
     * @param user     The user.
     * @param password The new password.
     *
     * @return Either <code>null</code> or an error object.
     */
    private de.codewave.mytunesrss.jsp.Error sendPasswordToUser(User user, StringBuilder password) {
        de.codewave.mytunesrss.jsp.Error error = null;
        try {
            MyTunesRss.MAILER.sendMail(user, getBundleString("mail.forgottenPassword.subject"), getBundleString("mail.forgottenPassword.body",
                                                                                                                password));
        } catch (MailException e) {
            LOGGER.error("Could not send email.", e);
            error = new BundleError("error.sendPasswordMailException");
        }
        return error;
    }
}