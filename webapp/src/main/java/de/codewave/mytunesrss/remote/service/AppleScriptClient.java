package de.codewave.mytunesrss.remote.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.service.QtPlayerClient
 */
public class AppleScriptClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppleScriptClient.class);
    private static final String DUMMY_COMMAND = "thisdummycommanddoesnotexist";

    public void executeAppleScript(String... commands) throws IOException {
        String[] processCommands = new String[commands.length * 2 + 1];
        processCommands[0] = "osascript";
        for (int i = 0; i < commands.length; i++) {
            processCommands[1 + (i * 2)] = "-e";
            processCommands[2 + (i * 2)] = commands[i];
        }
        new ProcessBuilder().command(processCommands).start();
    }
}