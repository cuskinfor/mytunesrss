/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * de.codewave.utils.servlet.CommandServlet
 */
public class CommandServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        doCommand(servletRequest, servletResponse);
    }

    @Override
    protected void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        doCommand(servletRequest, servletResponse);
    }

    private void doCommand(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
        String command = servletRequest.getParameter("command");
        if (StringUtils.isEmpty(command)) {
            command = servletRequest.getPathInfo();
            if (StringUtils.isNotEmpty(command)) {
                command = command.substring(1);// remove leading slash
                if (command.indexOf('/') > -1) {
                    command = command.substring(0, command.indexOf('/'));// command is until the first slash
                }
            }
        }
        CommandHandler commandHandler = CommandHandlerRegistry.getInstance(servletRequest).getCommandHandler(command);
        commandHandler.execute(servletRequest, servletResponse);
    }
}
