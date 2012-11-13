package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.itunes.MissingItunesFiles;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Map;

public class AdminNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminNotifier.class);
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public void notifyDatabaseUpdate(long time, Map<String, MissingItunesFiles> missingItunesFiles, SystemInformation systemInformation) {
        if (MyTunesRss.CONFIG.isNotifyOnDatabaseUpdate() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Database has been updated";
            StringBuilder body = new StringBuilder();
            body.append("The database has been updated. Update took ").append(time / 1000L).append(" seconds.\n\nTracks: ").append(systemInformation.getTrackCount());
            body.append("\nAlbums: ").append(systemInformation.getAlbumCount()).append("\nArtists: ").append(systemInformation.getArtistCount()).append("\nGenres: ");
            body.append(systemInformation.getGenreCount());
            if (!missingItunesFiles.isEmpty()) {
                body.append("\n\nMissing files from iTunes libraries:\n====================================\n");
                for (Map.Entry<String, MissingItunesFiles> entry : missingItunesFiles.entrySet()) {
                    body.append("\n").append(entry.getKey()).append(": ").append(entry.getValue().getCount()).append("\n\n");
                    body.append("The following files were missing (max. ").append(MissingItunesFiles.MAX_MISSING_FILE_PATHS).append(" are listed):\n");
                    for (String path : entry.getValue().getPaths()) {
                        body.append(path).append("\n");
                    }
                }
            }
            sendAdminMail(subject, body.toString());
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

    public void notifyOutdatedItunesXml(File iTunesMaster, File iTunesXml) {
        if (MyTunesRss.CONFIG.isNotifyOnOutdatedItunesXml() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Outdated iTunes XML file";
            String body = "The iTunes XML file \"" + iTunesXml.getAbsolutePath() + "\" is older than the corresponding iTunes master file \"" + iTunesMaster.getAbsolutePath() + "\". Deleting the XML file, then starting and stopping iTunes should correct the issue. The problem are most likely characters in the name/author/album/etc information in your iTunes library which cause iTunes to stop updating the XML file. Unless you find and change the offending character(s) in your library, deleting the XML file will fix the problem only once but not permanently.";
            sendAdminMail(subject, body);
        }
    }

    public void notifyLoginExpired(String username, String remoteAddress) {
        if (MyTunesRss.CONFIG.isNotifyOnLoginFailure() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Expired login failure";
            String body = "There was login attempt for expired user name \"" + username + "\" from remote address \"" + remoteAddress + "\".";
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

    public void notifyMissingFile(Track track) {
        if (MyTunesRss.CONFIG.isNotifyOnMissingFile() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Missing track file";
            String body = "The file \"" + track.getFile() + "\" was requested but is missing.";
            sendAdminMail(subject, body);
        }
    }

    public void notifySkippedDatabaseUpdate(JobExecutionContext jobExecutionContext) {
        if (MyTunesRss.CONFIG.isNotifyOnSkippedDatabaseUpdate() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            String subject = "Skipped database update";
            String body = "The database update scheduled for " + DATE_FORMAT.get().format(jobExecutionContext.getFireTime()) + " has not been started since another database task was active.\n" +
                    "The next update is scheduled for " + DATE_FORMAT.get().format(jobExecutionContext.getNextFireTime()) + ".";
            sendAdminMail(subject, body);
        }
    }

    private void sendAdminMail(String subject, String body) {
        try {
            MyTunesRss.MAILER.sendMail(MyTunesRss.CONFIG.getAdminEmail(), "MyTunesRSS: " + subject, body);
        } catch (MailException e) {
            LOGGER.error("Could not send admin email.", e);
        }
    }
}
