package de.codewave.mytunesrss;

import org.apache.commons.lang.StringUtils;

public class AdminNotifier {
    public void notifyDatabaseUpdate() {
        if (MyTunesRss.CONFIG.isNotifyOnDatabaseUpdate()
                && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            // todo
        }
    }

    public void notifyEmailChange(User user, String oldEmail) {
        if (MyTunesRss.CONFIG.isNotifyOnEmailChange()
                && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "User has changed his email address";
            String body = "User \"" + user.getName()
                    + "\" has changed his email " + "address from \""
                    + oldEmail + "\" to \"" + user.getEmail() + "\".";
            sendAdminMail(subject, body);
        }
    }

    public void notifyInternalError() {
        if (MyTunesRss.CONFIG.isNotifyOnInternalError()
                && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            // todo
        }
    }

    public void notifyLoginFailure() {
        if (MyTunesRss.CONFIG.isNotifyOnLoginFailure()
                && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            // todo
        }
    }

    public void notifyPasswordChange(User user) {
        if (MyTunesRss.CONFIG.isNotifyOnPasswordChange()
                && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "User has changed his password";
            String body = "User \"" + user.getName()
                    + "\" has changed his password.";
            sendAdminMail(subject, body);
        }
    }

    public void notifyQuotaExceeded() {
        if (MyTunesRss.CONFIG.isNotifyOnQuotaExceeded()
                && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            // todo
        }
    }

    public void notifyTranscodingFailure() {
        if (MyTunesRss.CONFIG.isNotifyOnTranscodingFailure()
                && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            // todo
        }
    }

    public void notifyWebUpload() {
        if (MyTunesRss.CONFIG.isNotifyOnWebUpload()
                && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            // todo
        }
    }

    private void sendAdminMail(String subject, String body) {
        MyTunesRss.MAILER.sendMail(MyTunesRss.CONFIG.getAdminEmail(),
                "MyTunesRSS: " + subject, body);
    }
}
