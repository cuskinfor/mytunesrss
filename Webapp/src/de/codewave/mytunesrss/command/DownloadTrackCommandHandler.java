package de.codewave.mytunesrss.command;

import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.command.DownloadTrackCommandHandler
 */
public class DownloadTrackCommandHandler extends PlayTrackCommandHandler {
    @Override
    public void execute() throws IOException, SQLException {
        if (needsAuthorization() || !getAuthUser().isDownload()) {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            super.execute();
        }
    }
}