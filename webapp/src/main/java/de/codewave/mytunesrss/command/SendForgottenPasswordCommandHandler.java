package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                if (user.isChangePassword()) {
                    if (StringUtils.isNotEmpty(user.getEmail())) {
                        StringBuilder password = new StringBuilder();
                        for (int i = 0; i < 10; i++) {
                            password
                                    .append((char)((byte)'a' + (byte)myRandom
                                            .nextInt(26)));
                        }
                        user.setPasswordHash(MyTunesRss.SHA1_DIGEST
                                .digest(password.toString().getBytes("UTF-8")));
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

    private de.codewave.mytunesrss.jsp.Error sendPasswordToUser(User user, StringBuilder password) {
        de.codewave.mytunesrss.jsp.Error error = null;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(getBundleString("mail.forgottenPassword.subject"));
        message.setFrom(MyTunesRss.CONFIG.getMailSender());
        message.setTo(user.getEmail());
        message
                .setText(getBundleString("mail.forgottenPassword.body", password));
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(getMyTunesRssConfig().getMailHost());
            if (getMyTunesRssConfig().getMailPort() > 0) {
                mailSender.setPort(getMyTunesRssConfig().getMailPort());
            }
            mailSender.setUsername(getMyTunesRssConfig().getMailLogin());
            mailSender.setPassword(getMyTunesRssConfig().getMailPassword());
            mailSender.send(message);
        } catch (MailException ex) {
            LOGGER.error("Could not send email.", ex);
            error = new BundleError("error.sendPasswordMailException");
        }
        return error;
    }
}