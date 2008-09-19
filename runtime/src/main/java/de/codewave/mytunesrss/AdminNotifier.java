package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AdminNotifier {
    public void notifyDatabaseUpdate(long time, SystemInformation systemInformation) {
        if (MyTunesRss.CONFIG.isNotifyOnDatabaseUpdate() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Database has been updated";
            String body = "The database has been updated. Update took " + (time / 1000L) + " seconds.\n\n" + "Tracks: " +
                    systemInformation.getTrackCount() + "\n" + "Albums: " + systemInformation.getAlbumCount() + "\n" + "Artists: " +
                    systemInformation.getArtistCount() + "\n" + "Genres:" + systemInformation.getGenreCount();
            sendAdminMail(subject, body);
        }
    }

    public void notifyEmailChange(User user, String oldEmail) {
        if (MyTunesRss.CONFIG.isNotifyOnEmailChange() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "User has changed his email address";
            String body =
                    "User \"" + user.getName() + "\" has changed his email " + "address from \"" + oldEmail + "\" to \"" + user.getEmail() + "\".";
            sendAdminMail(subject, body);
        }
    }

    public void notifyInternalError(Throwable t) {
        if (MyTunesRss.CONFIG.isNotifyOnInternalError() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Internal error";
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            String body = "Please go to the configuration panel of your MyTunesRSS server and send a support request to Codewave Software. " +
                    "Alternatively forward this mail to support@codewave.de if you like.\n\n" + sw.toString();
            sendAdminMail(subject, body);
        }
    }

    public void notifyLoginFailure(String username, String remoteAddress) {
        if (MyTunesRss.CONFIG.isNotifyOnLoginFailure() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Login failure";
            String body = "There was an unsuccessful login attempt for user name \"" + username + "\" from remote address \"" + remoteAddress + "\".";
            sendAdminMail(subject, body);
        }
    }

    public void notifyPasswordChange(User user) {
        if (MyTunesRss.CONFIG.isNotifyOnPasswordChange() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "User has changed his password";
            String body = "User \"" + user.getName() + "\" has changed his password.";
            sendAdminMail(subject, body);
        }
    }

    public void notifyQuotaExceeded(User user) {
        if (MyTunesRss.CONFIG.isNotifyOnQuotaExceeded() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "User download quota has been exceeded";
            String body = "User download quota of " + user.getQuotaDownBytes() + " bytes has been exceeded.";
            sendAdminMail(subject, body);
        }
    }

    public void notifyTranscodingFailure(String[] sourceCommand, String[] targetCommand, Exception e) {
        if (MyTunesRss.CONFIG.isNotifyOnTranscodingFailure() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String subject = "Transcoding failure";
            String body = "Transcoding failed.\n\n" + "Source command was \"" + StringUtils.join(sourceCommand, ' ') + "\"\n\n" +
                    "Target command was \"" + StringUtils.join(targetCommand, ' ') + "\"\n\n" + sw.toString();
            sendAdminMail(subject, body);

        }
    }

    public void notifyWebUpload(User user, String fileInfos) {
        if (MyTunesRss.CONFIG.isNotifyOnWebUpload() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Web interface file upload";
            String body = "User " + user.getName() + " has uploaded the following files to your server:\n\n" + fileInfos;
            sendAdminMail(subject, body);
        }
    }

    private void sendAdminMail(String subject, String body) {
        MyTunesRss.MAILER.sendMail(MyTunesRss.CONFIG.getAdminEmail(), "MyTunesRSS: " + subject, body);
    }
}
