/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.utils.servlet.*;

public enum MyTunesRssCommand implements Command {
    DoLogin, ShowPortal;

    public String getName() {
        switch (this) {
            case DoLogin:
                return "doLogin";
            case ShowPortal:
                return "showPortal";
            default:
                throw new IllegalArgumentException("Illegal command!");
        }
    }

    public Class<? extends CommandHandler> getCommandHandlerClass() {
        switch (this) {
            case DoLogin:
                return DoLoginCommandHandler.class;
            case ShowPortal:
                return ShowPortalCommandHandler.class;
            default:
                throw new IllegalArgumentException("Illegal command!");
        }
    }
}
