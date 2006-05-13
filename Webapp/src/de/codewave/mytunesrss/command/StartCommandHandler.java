/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.StartCommandHandler
 */
public class StartCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws IOException, ServletException {
        forward(MyTunesRssCommand.ShowPortal);
    }
}
