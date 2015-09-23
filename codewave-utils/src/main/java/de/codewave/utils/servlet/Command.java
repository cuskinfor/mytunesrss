/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

/**
 * de.codewave.utils.servlet.Command
 */
public interface Command {
    String getName();
    Class<? extends CommandHandler> getCommandHandlerClass();
}