package de.codewave.mytunesrss.command;

import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

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