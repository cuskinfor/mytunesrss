/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.utils.servlet.CommandHandlerRegistry
 */
public abstract class CommandHandlerRegistry implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandlerRegistry.class);

    public static CommandHandlerRegistry getInstance(HttpServletRequest servletRequest) {
        return (CommandHandlerRegistry)servletRequest.getSession().getServletContext().getAttribute(CommandHandlerRegistry.class.getName());
    }

    private Map<String, CommandHandler> myCommandHandler = new HashMap<String, CommandHandler>();
    private CommandHandler myDefaultCommandHandler = new DefaultCommandHandler();

    public CommandHandler getCommandHandler(String command) {
        CommandHandler commandHandler = myCommandHandler.get(command);
        return commandHandler != null ? commandHandler : myDefaultCommandHandler;
    }

    protected void registerCommands(Command... commands) {
        if (commands != null && commands.length > 0) {
            for (int i = 0; i < commands.length; i++) {
                try {
                    myCommandHandler.put(commands[i].getName(), commands[i].getCommandHandlerClass().newInstance());
                } catch (Exception e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could not create command handler for class \"" + commands[i].getCommandHandlerClass() + "\".", e);
                    }
                }
            }
        }
    }

    protected void setDefaultCommandHandler(Class<? extends CommandHandler> commandHandlerClass) {
        try {
            myDefaultCommandHandler= commandHandlerClass.newInstance();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not create default command handler for class \"" + commandHandlerClass + "\".", e);
            }
        }
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setAttribute(CommandHandlerRegistry.class.getName(), this);
        init();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        myCommandHandler = null; // unload command handlers
    }

    protected abstract void init();

    public static class DefaultCommandHandler extends CommandHandler {
        public void execute() throws IOException, ServletException {
            // intentionally left blank
        }
    }
}