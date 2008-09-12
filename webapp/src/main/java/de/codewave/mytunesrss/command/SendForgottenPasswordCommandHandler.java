package de.codewave.mytunesrss.command;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.commanDoLoginCommandHandlerer
 */
public class SendForgottenPasswordCommandHandler extends
        MyTunesRssCommandHandler {
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
                                    .append((char) ((byte) 'a' + (byte) myRandom
                                            .nextInt(26)));
                        }
                        user.setPasswordHash(MyTunesRss.SHA1_DIGEST
                                .digest(password.toString().getBytes("UTF-8")));
                        de.codewave.mytunesrss.jsp.Error error = sendPasswordToUser(
                                user, password);
                        if (error != null) {
                            addError(error);
                        } else {
                            addMessage(new BundleError("info.passwordSent"));
                        }
                    } else {
                        addError(new BundleError(
                                "error.sendPasswordNoUserEmail"));
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

    private de.codewave.mytunesrss.jsp.Error sendPasswordToUser(User user,
            StringBuilder password) {
        de.codewave.mytunesrss.jsp.Error error = null;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("Your new MyTunesRSS password"); // todo
        message.setFrom("MyTunesRSS"); // todo
        message.setTo(user.getEmail());
        message
                .setText("Your new MyTunesRSS password is (without the quotes): \""
                        + password + "\"."); // todo
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
            error = new BundleError("error.sendPasswordMailException");
        }
        return error;
    }
}