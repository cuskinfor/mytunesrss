package de.codewave.mytunesrss.command;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.command.DownloadTrackCommandHandler
 */
public class DownloadTrackCommandHandler extends PlayTrackCommandHandler {
    @Override
    public void executeAuthorized() throws IOException, SQLException {
        if (!isRequestAuthorized() || !getAuthUser().isDownload()) {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            super.executeAuthorized();
        }
    }
}