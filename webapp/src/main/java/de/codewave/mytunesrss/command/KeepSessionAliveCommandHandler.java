package de.codewave.mytunesrss.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * de.codewave.mytunesrss.command.KeepSessionAliveCommandHandler
 */
public class KeepSessionAliveCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeepSessionAliveCommandHandler.class);

    @Override
    public void execute() throws Exception {
        HttpSession session = getRequest().getSession(false);
        String sessionId = session != null ? session.getId() : null;
        if (sessionId != null) {
            LOGGER.debug("Keeping session with ID \"" + sessionId + "\" alive.");
        }
        getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}