package de.codewave.mytunesrss.remote.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * de.codewave.mytunesrss.remote.service.QtPlayerClient
 */
public class AppleScriptClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppleScriptClient.class);
    private String myApplication;

    public AppleScriptClient() {
        myApplication = null;
    }

    public AppleScriptClient(String comonLinePrefix) {
        myApplication = StringUtils.trimToNull(comonLinePrefix);
    }

    public String executeAppleScript(String... commands) throws IOException {
        String[] finalCommands;
        if (myApplication != null) {
            finalCommands = new String[commands.length + 2];
            finalCommands[0] = "tell application \"" + myApplication + "\"";
            finalCommands[finalCommands.length - 1] = "end tell";
            System.arraycopy(commands, 0, finalCommands, 1, commands.length);
        } else {
            finalCommands = commands;
        }
        String[] processCommands = new String[finalCommands.length * 2 + 1];
        processCommands[0] = "osascript";
        for (int i = 0; i < finalCommands.length; i++) {
            processCommands[1 + (i * 2)] = "-e";
            processCommands[2 + (i * 2)] = finalCommands[i];
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("AppleScript: " + StringUtils.join(finalCommands, "\n"));
        }
        Process p = new ProcessBuilder().command(processCommands).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {
            p.waitFor();
            String response = reader.readLine();
            LOGGER.debug("Apple script response: " + response);
            return response;
        } catch (Exception e) {
            IOUtils.closeQuietly(reader);
        }
        return null;
    }
}