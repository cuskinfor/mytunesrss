/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.StartCommandHandler
 */
public class StartCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        forward(MyTunesRssCommand.ShowPortal);
    }
}
