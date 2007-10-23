/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.utils.servlet.*;

/**
 * de.codewave.mytunesrss.command.MyTunesRssCommandHandlerRegistry
 */
public class MyTunesRssCommandHandlerRegistry extends CommandHandlerRegistry {
    protected void init() {
        registerCommands(MyTunesRssCommand.values());
        setDefaultCommandHandler(StartCommandHandler.class);
    }
}