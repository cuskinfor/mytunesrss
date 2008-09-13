package de.codewave.mytunesrss;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * de.codewave.mytunesrss.MailSender
 */
public class MailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailSender.class);

    /**
     * Send an email to the specified user.
     *
     * @param to      The user to receive the email.
     * @param subject The subject.
     * @param body    The mail body.
     */
    public void sendMail(User to, String subject, String body) {
        sendMail(to.getName() + "<" + to.getEmail() + ">", subject, body);
    }

    /**
     * Send an email to the specified address.
     *
     * @param to      The to address.
     * @param subject The subject.
     * @param body    The mail body.
     */
    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setFrom(MyTunesRss.CONFIG.getMailSender());
        message.setTo(to);
        message.setText(body);
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties mailProperties = new Properties();
        mailSender.setHost(MyTunesRss.CONFIG.getMailHost());
        if (MyTunesRss.CONFIG.getMailPort() > 0) {
            mailSender.setPort(MyTunesRss.CONFIG.getMailPort());
        }
        mailProperties.setProperty("mail.smtp.localhost", "localhost");
        if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getMailLogin()) && StringUtils.isNotEmpty(MyTunesRss.CONFIG.getMailPassword())) {
            mailProperties.setProperty("mail.smtp.auth", "true");
            mailSender.setUsername(MyTunesRss.CONFIG.getMailLogin());
            mailSender.setPassword(MyTunesRss.CONFIG.getMailPassword());
        }
        mailSender.setJavaMailProperties(mailProperties);
        mailSender.send(message);
    }
}