package de.codewave.mytunesrss;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.net.ssl.SSLSocketFactory;
import java.util.ArrayList;
import java.util.List;
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
     * @throws MailException Any exception while sending the mail.
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
        mailProperties.setProperty("mail.debug", "true");
        List<Integer> ports = new ArrayList<Integer>();
        if (MyTunesRss.CONFIG.getMailPort() > 0) {
            ports.add(MyTunesRss.CONFIG.getMailPort());
        } else {
            ports.add(25);
            ports.add(465);
            ports.add(587);
        }
        mailProperties.setProperty("mail.smtp.localhost", "localhost");
        if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getMailLogin()) && StringUtils.isNotEmpty(MyTunesRss.CONFIG.getMailPassword())) {
            mailProperties.setProperty("mail.smtp.auth", "true");
            mailSender.setUsername(MyTunesRss.CONFIG.getMailLogin());
            mailSender.setPassword(MyTunesRss.CONFIG.getMailPassword());
        }
        if (MyTunesRss.CONFIG.isMailTls()) {
            mailProperties.setProperty("mail.smtp.starttls.enable", "true");
        }
        mailProperties.setProperty("mail.smtp.connectiontimeout", "10000");
        mailSender.setJavaMailProperties(mailProperties);
        MailException lastException = null;
        for (Integer port : ports) {
            mailSender.setPort(port);
            try {
                LOGGER.debug("Trying to send mail using host \"" + mailSender.getHost() + "\" and port \"" + mailSender.getPort() + "\".");
                mailSender.send(message);
                lastException = null;
                break;
            } catch (MailException e) {
                LOGGER.debug("Could not send mail using port " + port + ".", e);
                lastException = e;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }
}